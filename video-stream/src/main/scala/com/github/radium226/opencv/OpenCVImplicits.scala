package com.github.radium226.opencv

import java.nio.file.Path

import org.opencv.core.{Mat, MatOfRect, Point, Rect, Scalar, Size}
import com.github.radium266.dlib.swig.{FaceDetector, ShapePredictor}
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier

import scala.collection.JavaConverters._

trait OpenCVImplicits {

  object PimpedMat {

    val faceLandmarksShapePredictor = new ShapePredictor("/home/adrien/Personal/Projects/dlib-java/src/test/resources/shape_predictor_68_face_landmarks.dat")

    val faceCascadeClassifier = new CascadeClassifier("/usr/share/opencv/haarcascades/haarcascade_frontalface_default.xml")


    val faceDetector = new FaceDetector()
  }

  implicit class PimpedMat(mat: Mat) {

    def detectFaces(): Seq[Rect] = {
      val detectedRects = new MatOfRect()
      PimpedMat.faceCascadeClassifier.detectMultiScale(mat, detectedRects)
      detectedRects.toArray

      //PimpedMat.faceDetector.detectFaces(mat).asScala
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

    def drawRect(rect: Rect): Mat = {
      Imgproc.rectangle(mat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 255, 0))
      mat
    }

    def drawRects(rects: Seq[Rect]): Mat = {
      rects.foldLeft(mat)({ (mat, rect) => mat.drawRect(rect) })
    }

    def saveTo(filePath: Path): Unit = {
      Imgcodecs.imwrite(filePath.toString, mat)
    }

    def resize(width: Int, height: Int): Mat = {
      val newMat = new Mat()
      Imgproc.resize(mat, newMat, new Size(width, height))
      newMat
    }

  }

}

object OpenCVImplicits extends OpenCVImplicits
