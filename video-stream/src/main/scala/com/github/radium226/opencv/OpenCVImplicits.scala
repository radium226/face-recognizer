package com.github.radium226.opencv

import java.nio.file.Path

import org.opencv.core.{Mat, MatOfRect, Point, Rect, Scalar}
import com.github.radium266.dlib.swig.ShapePredictor
import org.opencv.core.Point
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier

import scala.collection.JavaConverters._

trait OpenCVImplicits {

  object PimpedMat {

    val faceLandmarksShapePredictor = new ShapePredictor("/home/adrien/Personal/Projects/dlib-java/src/test/resources/shape_predictor_68_face_landmarks.dat")

    val faceCascadeClassifier = new CascadeClassifier("/usr/share/opencv/haarcascades/haarcascade_frontalface_default.xml")

  }

  implicit class PimpedMat(mat: Mat) {

    def detectFaces(): Seq[Rect] = {
      val detectedRects = new MatOfRect()
      PimpedMat.faceCascadeClassifier.detectMultiScale(mat, detectedRects)
      detectedRects.toArray
    }

    def predictFaceLandmarks(rect: Rect): Seq[Point] = {
      PimpedMat.faceLandmarksShapePredictor.predictShape(mat.submat(rect)).asScala.map({ position =>

        new Point(rect.x + position.x, rect.y + position.y)
      })
    }

    def drawMarkers(positions: Seq[Point]): Unit = {
      positions.foreach({ position =>
        Imgproc.drawMarker(mat, position, new Scalar(255, 0, 0))
        mat
      })
    }

    def saveTo(filePath: Path): Unit = {
      Imgcodecs.imwrite(filePath.toString, mat)
    }

  }

}

object OpenCVImplicits extends OpenCVImplicits
