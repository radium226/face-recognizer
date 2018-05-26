package com.github.radium226.opencv.stream

import akka.stream.scaladsl.{Flow, FlowOps}
import org.opencv.core.{Mat, Rect, Size}
import com.github.radium226.opencv.OpenCVImplicits._
import org.opencv.imgproc.Imgproc

trait StreamImplicits {

  implicit class MatFlowOps[T](flow: FlowOps[Mat, T]) {

    def subMat(rect: Rect): FlowOps[Mat, T] = {
      flow.map({ mat =>
        mat.submat(rect)
      })
    }

  }

  implicit class MatRectFlowOps[T](flow: FlowOps[(Mat, Rect), T]) {

    def subMat: FlowOps[Mat, T] = {
      flow.map({ case (mat, rect) =>
        mat.submat(rect)
      })
    }

  }

}

object StreamImplicits extends StreamImplicits
