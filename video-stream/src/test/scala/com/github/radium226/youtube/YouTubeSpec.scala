package com.github.radium226.youtube

import java.net.URL
import java.nio.file.Files

import akka._
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import com.github.radium226.logging.Logging
import org.opencv.imgcodecs.Imgcodecs

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

object YouTubeSpec extends App with Logging {

  val url = new URL("https://www.youtube.com/watch?v=Dtj2lpVEqQg")
  //val url = new URL("https://www.youtube.com/watch?v=mCGCyzRW1Zc")

  implicit val actorSystem = ActorSystem()
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher

  val tempFolder = Files.createTempDirectory("YouTubeSpec")

  val future = YouTube.source(url)
    .zipWithIndex
    .filter({ case (_, index) =>
      index % 24 == 0
    })
    .mapAsync(10)({ case (mat, index) =>
      val width = mat.width()
      val height = mat.height();
      val jpgFileName = f"${index}%07d.jpg"
      val jpgFilePath = tempFolder.resolve(jpgFileName)
      info(s"Writing ${jpgFilePath}... ")
      Future {
        //Imgcodecs.imwrite(jpgFilePath.toString, mat)
      }
    })
    .runWith(Sink.ignore)

  println(" --> Waiting for source to be done... ")
  println(Await.result(future, Duration.Inf))
  println(" --> Done! ")

  println(tempFolder)

  actorMaterializer.shutdown()
  actorSystem.terminate()
}
