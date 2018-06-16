package com.github.radium226.video

import akka._
import akka.stream._
import akka.stream.scaladsl._
import com.github.radium226.logging.Logging
import org.opencv.core.{Mat, Rect, Size}
import com.github.radium226.opencv.OpenCVImplicits._
import org.opencv.imgproc.Imgproc

object Flows extends Logging {

  def focusOnFaces: Flow[Mat, Mat, NotUsed] = {
    Flow[Mat]
      .map({ mat =>
        info("Detecting known-faces... ")
        (mat, mat.detectFaces())
      })
      .map({ case (mat, rects) =>
        (mat, rects.sortBy({ rect => rect.height * rect.width }).reverse)
      })
      .collect({
        case (mat, rects) if rects.size == 1 =>
          info("Face detected! ")
          (mat, rects.head)
      })
      .map({ case (mat, rect) =>
        mat.submat(rect)
      })
      .map({ mat =>
        val positions = mat.predictFaceLandmarks(new Rect(0, 0, mat.width(), mat.height()))
        (mat, positions)
      })
        .map({ case (mat, positions) =>
          mat.drawMarkers(positions)
          mat
        })
      .map({ mat =>
        val resizedMat = new Mat()
        Imgproc.resize(mat, resizedMat, new Size(500, 500))
        resizedMat
      })
  }

}
