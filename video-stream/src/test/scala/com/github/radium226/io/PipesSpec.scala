package com.github.radium226.io

import language.postfixOps
import java.io.{InputStream, OutputStream}
import java.nio.file.{Files, Paths}
import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.github.radium226.commons.io.pipe.Pipes
import com.github.radium226.scalatest.AbstractSpec

import scala.concurrent.duration._
import squants.time.TimeConversions._
import squants.information.{DataRate, Gigabytes, Information}
import squants.information.InformationConversions._

import scala.compat.java8.FutureConverters._
import scala.concurrent.Await

object PipesSpec {

  val LargeSize: Information = 500.megabytes
  val SmallSize: Information = 1.megabytes

}

class PipesSpec extends AbstractSpec {

  import PipesSpec._

  behavior of "Pipes"
  import Pipes._

  it should "be able to pipe a large input stream to an output stream" in {
    info("Creating streams... ")
    val inputStream = MockInputStream.open(LargeSize)
    val outputStream = MockOutputStream.open(LargeSize)

    Await.result(pipe(inputStream, outputStream).toScala, Duration.Inf)
    info("Pipe ended! ")
  }

  it should "be able to pipe a large input stream to an output stream through a process" in {
    info("Creating process... ")
    val process = startProcess("cat")

    info("Creating streams... ")
    val inputStream = MockInputStream.open(LargeSize)
    val outputStream = MockOutputStream.open(LargeSize)

    Await.result(pipe(inputStream, process, outputStream).toScala, Duration.Inf)
    info("Pipe ended! ")

  }

  it should "be able to pipe through a slow process" in {
    info("Creating slow process... ")
    val slowProcess = startSlowProcess(SmallSize / 100 / 1.seconds)

    info("Creating streams... ")
    val inputStream = MockInputStream.open(SmallSize)
    val outputStream = MockOutputStream.open(SmallSize)

    Await.result(pipe(inputStream, slowProcess, outputStream).toScala, Duration.Inf)
    info("Pipe ended! ")
  }


  def startProcess(command: String*): Process = {
    new ProcessBuilder()
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .command(command:_*)
      .start()
  }

  def startSlowProcess(rate: DataRate): Process = {
    val command = Seq("pv", "-L", s"${rate.toKilobytesPerSecond.toInt.toString}k")
    info("Starting {} process", command)
    startProcess(command:_*)
  }

}
