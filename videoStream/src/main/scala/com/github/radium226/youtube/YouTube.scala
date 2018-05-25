package com.github.radium226.youtube

import java.io.InputStream
import java.net.URL
import java.nio.file.Path

import akka._
import akka.stream._
import akka.stream.scaladsl._
import com.github.radium226.logging.Logging
import com.github.radium226.opencv.OpenCV
import com.github.radium226.video.Video
import org.opencv.core.Mat
import org.opencv.highgui.HighGui
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.ximgproc.Ximgproc

object YouTube extends Logging {

  OpenCV.loadLibraries()

  def openInputStream(url: URL): InputStream = {
    val youtubeProcessBuilder = new ProcessBuilder("youtube-dl", url.toString, "-o", "-")
    youtubeProcessBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT)
    youtubeProcessBuilder.redirectError(ProcessBuilder.Redirect.INHERIT)

    val youtubeProcess = youtubeProcessBuilder.start
    val inputStream = youtubeProcess.getInputStream

    new Thread({ () =>
      youtubeProcess.waitFor()
      debug("The youtube-dl process is finished! ")
    }).start

    inputStream
  }

  def source(url: URL): Source[Mat, NotUsed] = {
    Video.fromInputStream({ () => openInputStream(url) })
  }

}
