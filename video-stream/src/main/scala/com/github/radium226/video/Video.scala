package com.github.radium226.video

import java.io.InputStream
import java.nio.file.{Files, Path}

import scala.concurrent.Future
import akka._
import akka.stream._
import akka.stream.scaladsl._
import com.github.radium226.video.stage._
import org.opencv.core.Mat
import java.io.OutputStream
import java.lang.ProcessBuilder.Redirect
import java.net.URL

import com.github.radium226.logging.Logging
import com.github.radium226.video.io.{VideoFileInputStream, VideoInputStream}

import scala.concurrent.ExecutionContext.Implicits.global

object Player extends Logging {

  def ffplay = { (width: Int, height: Int) =>
    info("Starting player process... ")
    new ProcessBuilder()
        .command("ffplay", "-f", "rawvideo", "-pixel_format", "bgr24", "-video_size", s"${width}x${height}", "-")
        .redirectError(Redirect.INHERIT)
      .start()
  }

}

object Format extends Logging {

  def webM: Format = new Format {

    override def createProcess(width: Int, height: Int): Process = {
      new ProcessBuilder()
          .command("ffmpeg",
            "-f", "rawvideo",
            "-pixel_format", "bgr24",
            "-video_size", s"${width}x${height}",
            "-i", "-",
            "-c:v", "libvpx",
            "-pixel_format", "yuv420p",
            "-f", "matroska",
            "-"
          )
          .redirectError(Redirect.INHERIT)
        .start()
    }

  }

}

trait Format {

  def createProcess(width: Int, height: Int): Process

}

object Video extends Logging{

  def fromInputStream(openInputStream: () => InputStream): Source[Mat, NotUsed] = {
    Source.fromGraph(new MatSourceGraphStage({ () => new VideoInputStream(openInputStream()) }))
  }

  def play(createProcess: (Int, Int) => Process = Player.ffplay): Sink[Mat, Future[Done]] = {
    Sink.fromGraph(new ThroughProcessMatSinkGraphStage(createProcess))
  }

  def save(outputStream: OutputStream, format: Format): Sink[Mat, Future[Done]] = {
    Sink.fromGraph(new ThroughProcessMatSinkGraphStage(format.createProcess, Some(outputStream)))
  }

  def save(filePath: Path, format: Format): Sink[Mat, Future[Done]] = {
    val outputStream = Files.newOutputStream(filePath)
    save(outputStream, format).mapMaterializedValue({ future =>
      future.onComplete({ _ =>
        outputStream.close()
      })
      future
    })
  }

  def file(filePath: Path): Source[Mat, NotUsed] = fromInputStream({ () =>
    info(s"Opening ${filePath}")
    Files.newInputStream(filePath)
  })

  def open(url: URL): Source[Mat, NotUsed] = {
    fromInputStream({ () => url.openStream() })
  }

  def open(videoFilePath: Path): Source[Mat, NotUsed] = {
    Source.fromGraph(new MatSourceGraphStage({ () => new VideoFileInputStream(videoFilePath) }))
  }

}
