package com.github.radium226.video

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import com.github.radium226.scalatest.AbstractSpec
import squants.time.Minutes
import squants.time.TimeConversions._
import akka.stream._
import akka.stream.scaladsl._
import com.github.radium226.opencv.OpenCV

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class VideoSpec extends AbstractSpec {

  OpenCV.loadLibraries()

  behavior of "Video"

  it should "work fine" in {
    info("Initializing Akka... ")
    implicit val actorSystem = ActorSystem()
    implicit val actorMaterializer = ActorMaterializer()

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

}
