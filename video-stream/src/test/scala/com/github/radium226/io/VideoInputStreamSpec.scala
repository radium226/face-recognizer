package com.github.radium226.io

import java.io.{InputStream, OutputStream}
import java.net.URL

import com.github.radium226.scalatest.AbstractSpec
import com.github.radium226.video.io.VideoInputStream
import com.github.radium226.youtube.YouTube

import scala.concurrent.Await
import com.github.radium226.commons.io.pipe.Pipes._
import com.github.radium226.commons.io.InputStreams._

import scala.compat.java8.FutureConverters._
import scala.concurrent.duration.Duration

class VideoInputStreamSpec extends AbstractSpec {

  behavior of "VideoInputStream"

  it should "work fine even for a large video" in {
    val largeYouTubeVideoURL = new URL("https://www.youtube.com/watch?v=93hq0YU3Gqk")

    info("Creating streams... ")
    val inputStream = YouTube.openInputStream(largeYouTubeVideoURL)
    val outputStream = MockOutputStream.open()

    info("Converting inputStream to videoInputStream... ")
    val videoInputStream = new VideoInputStream(inputStream)

    info("Waiting for end of pipe... ")
    Await.result(pipe(videoInputStream, outputStream).toScala, Duration.Inf)

    info("Done! ")
  }

}
