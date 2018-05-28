package com.github.radium226.video

import java.net.URL
import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import com.github.radium226.dlib.DLib
import com.github.radium226.opencv.OpenCV
import com.github.radium226.scalatest.AbstractSpec
import com.github.radium226.youtube.YouTube
import org.opencv.core.{CvType, Mat, Point, Scalar, Size}
import org.opencv.imgproc.Imgproc
import com.github.radium226.opencv.OpenCVImplicits._

class FlowsSpec extends AbstractSpec {

  behavior of "Flow"
  import Flows._

  implicit var actorSystem: ActorSystem = _
  implicit var actorMaterializer: ActorMaterializer = _

  override def beforeAll(): Unit = {
    super.beforeAll()

    OpenCV.loadLibraries()
    DLib.loadLibraries()

    actorSystem = ActorSystem()
    actorMaterializer = ActorMaterializer()
  }

  it should "be able to focus on faces" in {
    val source = Video.open(Paths.get("/home/adrien/Personal/Vault/How to Train the Female Figure Competitor _ Workout Motivation at Enterprise Fitness-121101340.mp4"))//YouTube.source(new URL("https://www.youtube.com/watch?v=I6-hIpwCPQ8"))//Video.open(Paths.get("/home/adrien/Personal/Projects/video-miner/video-stream/src/test/resources/aspen.webm"))

    val graph = source
        .map({ mat =>
          val resizedMat = new Mat()
          Imgproc.resize(mat, resizedMat, new Size(mat.width / 2, mat.height / 2))
          resizedMat
        })
        .map({ mat =>
          val rects = mat.detectFaces()
          info(s"Detecting faces (rects=${rects}... ")
          (mat, rects)
        })
        .map({ case (mat, rects) =>
          mat.drawRects(rects)
          rects.foreach({ rect =>
            val faceLandmarks = mat.predictFaceLandmarks(rect)
            faceLandmarks.foreach({ faceLandmark =>
              Imgproc.circle(mat, faceLandmark, 1, new Scalar(255, 0, 0))
            })
          })
          (mat, rects.sortBy({ rect => rect.width * rect.height }).reverse.headOption)
        })
      //.via(focusOnFaces)
        /*.collect({
          case (mat, Some(rect)) =>
            //mat.submat(rect).resize(500, 500)
            mat
        })*/
        .map({ case (mat, _) => mat })
      .toMat(Video.play())(Keep.right)

    val done = graph.run()

    await(done)
  }

  override def afterAll(): Unit = {
    actorMaterializer.shutdown()
    actorSystem.terminate()
    super.afterAll()
  }

}
