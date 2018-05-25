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
    implicit val actorSystem = ActorSystem()
    implicit val actorMaterializer = ActorMaterializer()

    val source = Video.fromInputStream({ () => MockVideo.openInputStream(5.minutes) })

    val done = source
      .zipWithIndex
      .runForeach({ case (mat, index) =>
        if ((index + 1) % 25 == 0) {
          info("We are at {} s", (index + 1) / 25)
        }
      })

    Await.result(done, Duration.Inf)
    info("Done! ")
  }

}
