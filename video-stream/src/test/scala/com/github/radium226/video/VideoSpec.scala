package com.github.radium226.video

import java.nio.file.{Files, Paths}

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import com.github.radium226.scalatest.AbstractSpec
import squants.time.Minutes
import squants.time.TimeConversions._
import akka.stream._
import akka.stream.scaladsl._
import com.github.radium226.dlib.DLib
import com.github.radium226.opencv.OpenCV

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import com.github.radium226.opencv.OpenCVImplicits._
import com.github.radium226.youtube.YouTube
import com.github.radium226.youtube.YouTubeSpec.{info, tempFolder}
import org.opencv.core.{Mat, Point}
import java.net.URL

import scala.collection.JavaConverters._
import akka.NotUsed
import akka.util.{ByteString, Timeout}
import com.github.radium226.commons.io.pipe.{PipeFlow, PipeSource}
import com.github.radium226.commons.io.pipe.Pipes._

import scala.compat.java8.FutureConverters._

import scala.concurrent.duration._

import com.github.radium226.opencv.stream.StreamImplicits._

class VideoSpec extends AbstractSpec {

  OpenCV.loadLibraries()
  DLib.loadLibraries()
  implicit val actorSystem = ActorSystem()
  implicit val actorMaterializer = ActorMaterializer()

  behavior of "Video"

  it should "work fine" in {
    info("Initializing Akka... ")

    info("Creating a source of Mat... ")
    val source = Video.fromInputStream({ () => MockVideo.openInputStream(5.minutes) })

    info("Printing each second... ")
    val done = source
      .zipWithIndex
      .runForeach({ case (mat, index) =>
        if ((index + 1) % 25 == 0) {
          info("We are at {}", ((index + 1) / 25).seconds)
        }
      })

    info("Waiting for the source to be done... ")
    Await.result(done, Duration.Inf)
    info("Done! ")
  }

  it should "work properly with DLib and OpenCV" in {
    val tempFolder = Files.createTempDirectory("VideoSpec")

    info("Creating video source... ")
    val graph = YouTube.source(new URL("https://www.youtube.com/watch?v=I6-hIpwCPQ8"))//MockVideo.source(5.minutes)
      .zipWithIndex
      /*.collect({
        case (mat, index) if index % 25 == 0 =>
          mat
      })*/
      .map({ case (mat, index) =>
        val faceRects = mat.detectFaces()
        (mat, faceRects.headOption)
      })
      .map({ case (mat, faceRectOption) =>
        val positions = faceRectOption.map({ faceRect =>
          mat.predictFaceLandmarks(faceRect)
        }).getOrElse(Seq())
        (mat, positions)
      })
      .map({ case (mat, positions) =>
        mat.drawMarkers(positions)
        mat
      })
      .zipWithIndex
      .map({ case (mat, index) =>
        val jpgFileName = f"${index}%07d.jpg"
        val jpgFilePath = tempFolder.resolve(jpgFileName)
        info(s"Writing ${jpgFilePath}... ")
        mat.saveTo(jpgFilePath)
        mat
      })
      .mapMaterializedValue({ _ =>
        VideoMetaData.builder()
            .width(320)
            .height(180)
          .build()
      })
      //.toMat(ffplay())(Keep.right)

    val future = play(graph)

    info("Waiting for video... ")
    Await.result(future, Duration.Inf)
    info("Done! ")
  }

  def ffplay(): Sink[Mat, Future[IOResult]] = {
    Flow[Mat]
      .map({ mat =>
        val bytes = Array.ofDim[Byte](mat.width() * mat.height() * mat.channels())
        mat.get(0, 0, bytes)
        bytes
      })
      .map({ bytes =>
        ByteString(bytes)
      })
      .toMat(StreamConverters.fromOutputStream({ () =>
        val ffplayProcess = new ProcessBuilder()
            .command("ffplay", "-f", "rawvideo", "-pixel_format", "bgr24", "-video_size", "320x180", "-")
          .start()

        ffplayProcess.getOutputStream
      }, true)
    )(Keep.right)
  }

  def play(source: Source[Mat, VideoMetaData])(implicit actorMaterializer: ActorMaterializer): Future[_] = {
    val graph = source
      .map({ mat =>
        val bytes = Array.ofDim[Byte](mat.width() * mat.height() * mat.channels())
        mat.get(0, 0, bytes)
        bytes
      })
      .map({ bytes =>
        ByteString(bytes)
      })
      .toMat(StreamConverters.asInputStream(Int.MaxValue.seconds))(Keep.both)

    val (videoMetaData, inputStream) = graph.run()

    val width = videoMetaData.getWidth
    val height = videoMetaData.getHeight

    val ffplayProcess = new ProcessBuilder()
          .command("ffplay", "-f", "rawvideo", "-pixel_format", "bgr24", "-video_size", s"${width}x${height}", "-")
        .start()

    pipe(PipeSource.existingInputStream(inputStream), PipeFlow.existingProcess(ffplayProcess)).toScala
  }

  it should "be able to play a video" in {
    val source = Video.fromInputStream({ () => MockVideo.openInputStream(10.seconds) })
    val future = source.runWith(Video.play(Player.ffplay))
    Await.result(future, Duration.Inf)
  }

  it should "be able to save a video" in {
    val url = new URL("https://www.youtube.com/watch?v=93hq0YU3Gqk")
    val tempFilePath = Files.createTempFile("VideoSpec", ".webm")

    info(s"Saving video to ${tempFilePath}")
    val doneFuture = YouTube.source(url)
      .zipWithIndex
      .collect({
        case (mat, index) if index <= 25 * 2 =>
          mat
      })
      .runWith(Video.save(tempFilePath, Format.webM))

    info("Waiting for video save... ")
    Await.result(doneFuture, Duration.Inf)
    info("Done! ")
  }

}
