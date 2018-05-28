package com.github.radium226.video.stage

import java.util.concurrent.TimeUnit

import akka.Done
import akka.stream.{Attributes, Inlet, SinkShape, SourceShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, GraphStageWithMaterializedValue, InHandler}
import com.github.radium226.logging.Logging
import org.opencv.core.Mat

import scala.concurrent.{Future, Promise}
import java.io.OutputStream

import com.github.radium226.commons.io.pipe.Pipes._

import scala.concurrent.ExecutionContext.Implicits.global

class ThroughProcessMatSinkGraphStage(createProcess: (Int, Int) => Process, outputStream: Option[OutputStream] = None) extends GraphStageWithMaterializedValue[SinkShape[Mat], Future[Done]] with Logging {

  val in: Inlet[Mat] = Inlet("mat")

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[Done]) = {

    info("Initializing stuff... ")
    var processOutputStream: OutputStream = null
    var bytes: Array[Byte] = null
    val processPromise = Promise[Process]

    val graphStageLogic = new GraphStageLogic(shape) {

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          info("Grabing mat... ")
          val mat = grab(in)
          if (processOutputStream == null) {
            val width = mat.width()
            val height = mat.height()
            val process = createProcess(width, height)
            processPromise.success(process)
            processOutputStream = process.getOutputStream
            outputStream.foreach({ outputStream =>
              pipe(process.getInputStream, outputStream)
            })
            bytes = Array.ofDim(width * height * 3)
          }
          mat.get(0, 0, bytes)
          processOutputStream.write(bytes)
          processOutputStream.flush()
          pull(in)
        }

        override def onUpstreamFinish(): Unit = {
          processOutputStream.close()
          super.onUpstreamFinish()
        }

      })

      override def preStart(): Unit = {
        pull(in)
      }

    }

    val doneFuture = processPromise.future.flatMap({ process =>
      Future({
        process.waitFor(Int.MaxValue, TimeUnit.SECONDS)
        Done
      })
    })

    info("Returning GraphStageLogic and materialized value... ")
    (graphStageLogic, doneFuture)
  }

  override def shape: SinkShape[Mat] = SinkShape(in)

}
