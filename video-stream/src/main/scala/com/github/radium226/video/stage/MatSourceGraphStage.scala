package com.github.radium226.video.stage

import java.io.InputStream

import akka.stream.{Attributes, Outlet, SourceShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import com.github.radium226.logging.Logging
import com.github.radium226.video.io.{AbstractVideoInputStream, VideoInputStream}
import org.opencv.core.{CvType, Mat}

class MatSourceGraphStage(openVideoInputStream: () => AbstractVideoInputStream, closeInputStream: Boolean = false) extends GraphStage[SourceShape[Mat]] with Logging {

  val out: Outlet[Mat] = Outlet("mat")

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    val videoInputStream = openVideoInputStream()

    val width = videoInputStream.getMetaData.getWidth
    val height = videoInputStream.getMetaData.getHeight

    info("The detected size is {}x{}", width, height)

    val byteArraySize = width * height * 3
    val byteArray = Array.ofDim[Byte](byteArraySize)

    setHandler(out, new OutHandler {
      override def onDownstreamFinish(): Unit = {
        videoInputStream.close()
        super.onDownstreamFinish()
      }

      override def onPull(): Unit = {
        debug("onPull()")
        var readByteTotalCount = 0
        var readByteCount = Int.MaxValue
        while (readByteCount > 0 && readByteTotalCount < byteArraySize) {
          readByteCount = videoInputStream.read(byteArray, readByteTotalCount, byteArraySize - readByteTotalCount)
          debug(s"readByteCount=${readByteCount}")
          if (readByteCount <= 0) {
            complete(out)
            debug("Completing MatSourceGraphStage")
            completeStage()
            videoInputStream.close()
          } else {
            debug("before assigning readByteTotalCount")
            readByteTotalCount = readByteTotalCount + readByteCount
            debug("after assigning readByteTotalCount")
          }
        }

        if (readByteCount > 0) {
          val mat = new Mat(height, width, CvType.CV_8UC3)
          mat.put(0, 0, byteArray)
          push(out, mat)
        }
      }
    })
  }

  override def shape: SourceShape[Mat] = SourceShape(out)
}