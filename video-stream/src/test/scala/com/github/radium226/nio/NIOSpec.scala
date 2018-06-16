package com.github.radium226.nio

import java.nio.ByteBuffer

import akka.actor.ActorSystem
import akka.stream.javadsl.Flow
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{AsyncCallback, GraphStageLogic, GraphStageWithMaterializedValue, InHandler, OutHandler}
import akka.util.ByteString
import com.github.radium226.dlib.DLib
import com.github.radium226.logging.Logging
import com.github.radium226.opencv.OpenCV
import com.github.radium226.scalatest.AbstractSpec
import com.zaxxer.nuprocess.{NuAbstractProcessHandler, NuProcess, NuProcessBuilder, NuProcessHandler}

import scala.{util => u}
import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, Promise}

import scala.concurrent.ExecutionContext.Implicits.global

sealed trait ProcessStatus

object ProcessStatus {

  def apply(code: Int) = {
    if (code >= 0) Success(code)
    else Failure(code)
  }

}

case class Success(code: Int) extends ProcessStatus

case class Failure(code: Int) extends ProcessStatus


object PassThroughProcess {

  def apply(processCommand: Seq[String]): Flow[ByteString, ByteString, Future[ProcessStatus]] = {
    Flow.fromGraph(new PassThroughProcessGraphStage(processCommand))
  }

}

class PassThroughProcessGraphStage(processCommand: Seq[String]) extends GraphStageWithMaterializedValue[FlowShape[ByteString, ByteString], Future[ProcessStatus]] with Logging {

  val in = Inlet[ByteString]("in")
  val out = Outlet[ByteString]("out")

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[ProcessStatus]) = {
    val promise = Promise[ProcessStatus]()

    var process: NuProcess = null

    var onStdoutCallback: AsyncCallback[(ByteBuffer, Boolean)] = null
    var onStdinReadyCallback: AsyncCallback[ByteBuffer] = null

    val logic = new GraphStageLogic(shape) {

      setHandler(in, new InHandler {

        override def onPush(): Unit = {
          info("onPush() has been invoked... ")

          info("Grabbing... ")
          val byteString = grab(in)
          info(s"Grabbed! byteString=${byteString.utf8String}")

          info("Writing to stdin... ")
          val byteBuffer = byteString.asByteBuffer
          process.writeStdin(byteBuffer)
        }

        override def onUpstreamFinish(): Unit = {
          info("onUpstreamFinish() invoked! ")
        }

      })

      setHandler(out, new OutHandler {

        override def onPull(): Unit = {
          info("onPull() has been invoked")
          pull(in)
        }

        override def onDownstreamFinish(): Unit = {
          info("onDownstreamFinish invoked! ")
        }

      })

      override def preStart(): Unit = {
        super.preStart()

        val processBuilder = new NuProcessBuilder(processCommand:_*)
        processBuilder.setProcessListener(new NuAbstractProcessHandler {

          override def onStdout(byteBuffer: ByteBuffer, closed: Boolean): Unit = {
            info("onStdout invoked with closed={}! ", closed)
            Await.result(onStdoutCallback.invokeWithFeedback((byteBuffer, closed)), Duration.Inf)
          }

          override def onStderr(byteBuffer: ByteBuffer, closed: Boolean): Unit = {
            info("onStderr invoked! ")
            Console.err.write(byteBuffer.array())
          }

          override def onExit(statusCode: Int): Unit = {
            info(s"onExit invoked with statusCode=${statusCode}")
            completeStage()
            promise.complete(u.Success(ProcessStatus(statusCode)))
          }

        })

        onStdoutCallback = getAsyncCallback[(ByteBuffer, Boolean)]({ case (byteBuffer, closed) =>
          info(s"onStdoutCallback invoked (closed=${closed})! ")

          info("Creating byteString from byteBuffer... ")
          val bytes = new Array[Byte](byteBuffer.remaining())
          byteBuffer.get(bytes)

          val byteString = ByteString(bytes)
          info(s"Created! byteString=${byteString.utf8String}")

          info("Pushing byteString...")
          push(out, byteString)

            //pull(in)
        })

        onStdinReadyCallback = getAsyncCallback[ByteBuffer]({ byteBuffer =>
            info("onStdinReadyCallback invoked! ")

            val byteString = grab(in)
            info("{} has been grabbed", byteString.utf8String)

            info("Writing byteString... ")
            byteBuffer.put(byteString.toByteBuffer)
            info("Written! ")

            byteBuffer.flip()
        })

        info("Starting process... ")
        process = processBuilder.start()

        //info("Pulling in from preStart! ")
        //pull(in)
      }


    }
    (logic, promise.future)
  }

  override def shape: FlowShape[ByteString, ByteString] = FlowShape.of(in, out)
}



class NIOSpec extends AbstractSpec {

  behavior of "Non-blocking IO"

  implicit var system: ActorSystem = _
  implicit var materializer: ActorMaterializer = _

  override def beforeAll(): Unit = {
    super.beforeAll()

    info("Starting system and materializer... ")
    system = ActorSystem()
    materializer = ActorMaterializer()
  }

  it should "work well with NuProcess" in {
    val graph = Source(List("Hello", "World", "! "))
      .map({ word =>
        info("Converting {} to ByteString", word)
        ByteString(word)
      })
      .viaMat(PassThroughProcess(Seq("cat")))(Keep.right)
      .map({ wordAsByteString =>
        info("Converting {} to String", wordAsByteString.utf8String)
        new String(wordAsByteString.toByteBuffer.array())
      })
      .to(Sink.foreach({ word =>
        println()
        println("==========")
        println(word)
        println("==========")
        println()
      }))

    val future = graph.run()
    await(future)
  }

  override def afterAll(): Unit = {
    info("Stopping materializer and system... ")
    materializer.shutdown()
    system.terminate()

    super.afterAll()
  }


}
