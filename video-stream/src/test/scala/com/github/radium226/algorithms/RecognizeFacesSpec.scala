package com.github.radium226.algorithms

import java.nio.file.{Path, Paths}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.radium226.dlib.DLib
import com.github.radium226.opencv.OpenCV
import com.github.radium226.scalatest.AbstractSpec
import com.github.radium266.dlib.swig.ShapePredictor
import org.opencv.core.{CvType, Mat, MatOfRect, Point, Rect}
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.objdetect.CascadeClassifier

import scala.collection.JavaConverters._

class RecognizeFacesSpec extends AbstractSpec {

  behavior of "RecognizeFaces"

  val testResourcesFolderPath = Paths.get("src/test/resources")

  val faceCascadeClassifier = new CascadeClassifier("/usr/share/opencv/haarcascades/haarcascade_frontalface_default.xml")

  val faceLandmarksShapePredictor = new ShapePredictor("/home/adrien/Personal/Projects/dlib-java/src/test/resources/shape_predictor_68_face_landmarks.dat")

  override def beforeAll(): Unit = {
    super.beforeAll()

    info("Loading native libraries")
    OpenCV.loadLibraries()
    DLib.loadLibraries()
  }

  def openImage(imageFileName: Path): Mat = {
    Imgcodecs.imread(testResourcesFolderPath.resolve(imageFileName).toString, CvType.CV_8SC3)
  }

  def detectFaces(image: Mat): Seq[Rect] = {
    val detectedRects = new MatOfRect()
    faceCascadeClassifier.detectMultiScale(image, detectedRects)
    detectedRects.toArray
  }

  def predictFaceLandmarks(faceImage: Mat): Seq[Point] = {
    faceLandmarksShapePredictor.predictShape(faceImage).asScala.map({ point =>
      new Point(point.x, point.y)
    })
  }

  // https://github.com/davisking/dlib/blob/master/tools/python/src/face_recognition.cpp

  it should "be able to use DLib and OpenCV in order to find faces" in {


  }

}
