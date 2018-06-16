package com.github.radium226.algorithms

import java.lang.ProcessBuilder.Redirect
import java.nio.file.{Files, Path, Paths}
import java.util
import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Attributes, FlowShape, Inlet, SinkShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Sink, Source, Zip}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler}
import akka.util.ByteString
import com.github.radium226.opencv.OpenCV
import com.github.radium226.scalatest.AbstractSpec
import com.github.radium226.video.Video
import org.opencv.core.{Core, Mat, Point, Scalar, Size}
import org.opencv.imgproc.Imgproc

import scala.collection.JavaConverters._
import scala.concurrent.duration._

class DetectScenesSpec extends AbstractSpec {

  implicit var system: ActorSystem = _
  implicit var materializer: ActorMaterializer = _

  override def beforeAll(): Unit = {
    super.beforeAll()

    OpenCV.loadLibraries()

    info("Starting system and materializer... ")
    system = ActorSystem()
    materializer = ActorMaterializer()
  }

  behavior of "Scene detection"

  def zipWithPrevious[T] = {
    Flow[T]
      .statefulMapConcat[(T, Option[T])]({ () =>
        var previous: Option[T] = None
        val factory = { current: T =>
          val iterable = List((current, previous))
          previous = Some(current)
          iterable
        }
        factory
      })
  }

  def zipWithChanged[T](hasChanged: (T, T) => Boolean): Flow[T, (T, Boolean), NotUsed] = {
    Flow[T]
      .via(zipWithPrevious)
      .map({
        case (current, None) =>
          (current, false)

        case (current, Some(previous)) =>
          (current, hasChanged(previous, current))
      })
  }

  it should "be able to keep the previous elements" in {
    Source(1 to 10)
      .via(zipWithPrevious)
      .runForeach(println)
  }

  case class HSVDelta(hueAverage: Double, saturationAverage: Double, luminanceAverage: Double)

  // https://github.com/Breakthrough/PySceneDetect/blob/master/scenedetect/detectors.py
  def computeHSVDelta = {
    Flow[Mat]
      .map({ frameInBGR =>
        val frameInHSV = new Mat()
        Imgproc.cvtColor(frameInBGR, frameInHSV, Imgproc.COLOR_RGB2HSV);
        frameInHSV
      })
      .via(zipWithPrevious)
      .map(_.swap)
      .map({
        case (Some(previousFrameInHSV), currentFrameInHSV) =>
          val pixelCount = currentFrameInHSV.width() * currentFrameInHSV.height()

          val deltaMatInHSV = new Mat()
          Core.absdiff(currentFrameInHSV, previousFrameInHSV, deltaMatInHSV)

          val hsvChannelMatsAsJava = new util.ArrayList[Mat]
          Core.split(deltaMatInHSV, hsvChannelMatsAsJava)
          val hsvChannelMats = hsvChannelMatsAsJava.asScala

          val hsvAverages = hsvChannelMats.map({ hsvChannelMat =>
            Core.sumElems(hsvChannelMat).`val`(0) / pixelCount
          })

          HSVDelta(hsvAverages(0), hsvAverages(1), hsvAverages(2))

        case (None, _) =>
          HSVDelta(0, 0, 0)
      })
  }

  it should "be able to compute HSVDelta" in {
    val videoFilePath = Paths.get("/home/adrien/Downloads/THT6/The.Handmaids.Tale.S01E06.720p.WEBRip.x264-MOROSE[rarbg]/the.handmaids.tale.s01e06.720p.webrip.x264-morose.mkv")
    Video.open(videoFilePath)
      .via(computeHSVDelta)
      .runForeach(println)
  }

  def detectSceneIndexes(threshold: Double = 30.0d): Flow[Mat, (Int, Boolean), NotUsed] = {
    Flow[Mat]
      .via(computeHSVDelta)
      .map({ hsvDelta =>
        Seq(hsvDelta.hueAverage, hsvDelta.saturationAverage, hsvDelta.luminanceAverage).sum / 3
      })
      .map({ hsvDeltaAverage =>
        hsvDeltaAverage >= threshold
      })
      .statefulMapConcat[(Int, Boolean)]({ () =>
        var sceneIndex = 0
        val factory = { newScene: Boolean =>
          if (newScene) {
            sceneIndex = sceneIndex + 1
          }
          List((sceneIndex, newScene))
        }
        factory
      })
  }

  it should "be able to detect scene indexes" in {
    val videoFilePath = Paths.get("/home/adrien/Downloads/THT6/The.Handmaids.Tale.S01E06.720p.WEBRip.x264-MOROSE[rarbg]/the.handmaids.tale.s01e06.720p.webrip.x264-morose.mkv")
    Video.open(videoFilePath)
        .via(zipWithSceneIndexes())
        .runForeach(println)
  }

  def zipWithSceneIndexes(threshold: Double = 30.0d): Flow[Mat, (Mat, Int), NotUsed] = {
    Flow.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val frameBroadcast = builder.add(Broadcast[Mat](2))
      val frameAndIndexZip = builder.add(Zip[Mat, Int]())

      frameBroadcast.out(0) ~> frameAndIndexZip.in0
      frameBroadcast.out(1) ~> detectSceneIndexes().map({ case (sceneIndex, _) => sceneIndex }) ~>frameAndIndexZip.in1

      FlowShape(frameBroadcast.in, frameAndIndexZip.out)
    })
  }

  def resize(width: Int, height: Int): Flow[Mat, Mat, NotUsed] = {
    Flow[Mat]
      .map({ frame =>
        val resizedFrame = new Mat()
        Imgproc.resize(frame, resizedFrame, new Size(width, height))
        resizedFrame
      })
  }

  it should "be able to split scenes and display it" in {
    val videoFilePath = Paths.get("/home/adrien/Downloads/THT6/The.Handmaids.Tale.S01E06.720p.WEBRip.x264-MOROSE[rarbg]/the.handmaids.tale.s01e06.720p.webrip.x264-morose.mkv")
    Video.open(videoFilePath)
      .via(resize(320, 180))
      .via(zipWithSceneIndexes())
      .map({ case (frame, sceneIndex) =>
        Imgproc.putText(
          frame,
          s"#${sceneIndex}",
          new Point(frame.rows() / 2, frame.cols() / 2),
          Core.FONT_ITALIC,
          1.0,
          new Scalar(255, 255, 255)
        )
        frame
      })
      .runWith(Video.play())

    await(30 minutes)
  }

  def saveScenes(folderPath: Path): Sink[(Mat, Int), NotUsed] = {
    case class State(encodingProcess: Process, sceneIndex: Int)

    Flow[(Mat, Int)]
      .to(Sink.fromGraph(new GraphStage[SinkShape[(Mat, Int)]] {

        val in: Inlet[(Mat, Int)] = Inlet("in")

        override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
          var currentStateOption: Option[State] = None
          setHandler(in, new InHandler {

            override def onPush(): Unit = {
              info("Grabbing in")
              val (frame, sceneIndex) = grab(in)

              info("Converting frame to byte array")
              val byteArray = toByteArray(frame)
              currentStateOption match {
                case None =>
                  info("Starting first encoding process... ")
                  val encodingProcess = startEncodingProcess(encodedFilePath(sceneIndex), frame.width(), frame.height())
                  currentStateOption = Some(State(encodingProcess, sceneIndex))

                case Some(state) if state.sceneIndex == sceneIndex =>
                  // It's the same scene, so there is nothing to do!

                case Some(state) if state.sceneIndex != sceneIndex =>
                  info("Switching encoding process... ")
                  // It's a new scene, so we close the old process...
                  state.encodingProcess.getOutputStream.close()
                  state.encodingProcess.waitFor()
                  // ...And we open a new one.
                  val encodingProcess = startEncodingProcess(encodedFilePath(sceneIndex), frame.width(), frame.height())
                  currentStateOption = Some(State(encodingProcess, sceneIndex))
              }

              currentStateOption.foreach({ case State(encodingProcess, _) =>
                encodingProcess.getOutputStream.write(byteArray)
                encodingProcess.getOutputStream.flush()
              })

              pull(in)
            }

            override def onUpstreamFinish(): Unit = {
              // Our job is done, here
              info("Waiting for last encoding process... ")
              currentStateOption.foreach({ case State(encodingProcess, _) =>
                encodingProcess.getOutputStream.close()
                encodingProcess.waitFor()
              })

              completeStage()

              super.onUpstreamFinish()
            }

          })

          override def preStart(): Unit = {
            info("Pulling in for the first time! ")
            pull(in)
          }

        }

        override def shape: SinkShape[(Mat, Int)] = SinkShape(in)

        def startEncodingProcess(encodedFilePath: Path, width: Int, height: Int): Process = {
          new ProcessBuilder()
              .command(
                "ffmpeg",
                "-f", "rawvideo",
                "-pixel_format", "bgr24",
                "-video_size", s"${width}x${height}",
                "-i", "-",
                "-c:v", "libvpx",
                "-pixel_format", "yuv420p",
                "-f", "matroska",
                encodedFilePath.toString
              )
              .redirectError(Redirect.INHERIT)
            .start()
        }

        def encodedFilePath(sceneIndex: Int) = {
          folderPath.resolve(f"${sceneIndex}%07d.webm")
        }

        def toByteArray(mat: Mat): Array[Byte] = {
          val byteArray = Array.ofDim[Byte](mat.height() * mat.width() * mat.channels())
          mat.get(0, 0, byteArray)
          byteArray
        }

      }))
  }

  it should "be able to split scenes and write them in a folder" in {
    val tempFolderPath = Files.createTempDirectory("SceneDetectionSpec")
    info(s"Writing scene to ${tempFolderPath}... ")

    val videoFilePath = Paths.get("/home/adrien/Downloads/THT6/The.Handmaids.Tale.S01E06.720p.WEBRip.x264-MOROSE[rarbg]/the.handmaids.tale.s01e06.720p.webrip.x264-morose.mkv")
    val splitSceneGraph = Video.open(videoFilePath)
      .via(resize(320, 180))
      .via(zipWithSceneIndexes())
      .map({ case (frame, sceneIndex) =>
        Imgproc.putText(
          frame,
          s"#${sceneIndex}",
          new Point(frame.rows() / 2, frame.cols() / 2),
          Core.FONT_ITALIC,
          1.0,
          new Scalar(255, 255, 255)
        )
        (frame, sceneIndex)
      })
      .to(saveScenes(tempFolderPath))

    splitSceneGraph.run()
    await(30 minutes)
  }

  override def afterAll(): Unit = {
    info("Stopping materializer and system... ")
    materializer.shutdown()
    system.terminate()

    super.afterAll()
  }

}
