package radium226.opencv

import java.util

import org.opencv.core.{Core, CvType, Mat, MatOfDouble, MatOfInt, MatOfPoint, MatOfPoint2f, MatOfRect, Point, Rect, Scalar, Size => OpenCVSize}
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import radium226.Size
import Implicits._
import org.opencv.core
import org.opencv.objdetect.CascadeClassifier

import scala.math._

object TryGabor {

  type Sigma = Double // Rayon
  type Theta = Double // Angle
  type Lambda = Double // Longueur d'onde
  type Gamma = Double // Aspect
  type Psi = Double // Phase

  type Percent = Double

  case class GaborParams(lambda: Lambda, theta: Theta, psi: Psi, gamma: Gamma, sigma: Sigma)

  val SomeGaborParams = GaborParams(4.000000000d, 0.000000000d, 0.785398163d, 1.000000000d, 3.00000000d)
  val SomeSize = Size(19, 19)

  val GregoireFilePath = "/home/adrien/Downloads/233404959.jpg" ///home/adrien/vlcsnap-2017-02-10-18h01m39s731.png" //"/home/adrien/Personal/Projects/video-miner/src/main/resources/original.jpg" // "/home/adrien/Downloads/sad.png" // vlcsnap-2017-02-10-17h55m39s964.png" //vlcsnap-2017-02-10-18h01m39s731.png"

  def fixMat(mat: Mat): Mat = {
    val fixedMat = new Mat()

    // https://gist.github.com/larrykite/1340099
    Imgproc.Sobel(mat, fixedMat, CvType.CV_32FC1, 0, 1)

    val minMax = Core.minMaxLoc(fixedMat)

    val (min, max) = (minMax.minVal, minMax.maxVal)
    println(s"min=$min, max=$max")

    //mat.convertTo(fixedMat, CvType.CV_8UC1, 255d / (minMax.maxVal - minMax.minVal), - minMax.minVal)
    mat.convertTo(fixedMat, CvType.CV_8UC1, 255d / (minMax.maxVal - minMax.minVal), -255d * minMax.minVal / (minMax.maxVal - minMax.minVal))
    //mat.convertTo(fixedMat, 1, +255)
    fixedMat
  }

  def main(arguments: Array[String]): Unit = {
    System.load("/usr/share/opencv/java/libopencv_java320.so")

    val mat = Imgcodecs.imread(GregoireFilePath)

    //val faceMat = mat
    val faceMat = detectFace(mat)

    val rows = faceMat.rows()
    val cols = faceMat.cols()

    val grayMat = new Mat()
    Imgproc.cvtColor(faceMat, grayMat, Imgproc.COLOR_BGRA2GRAY)

    var normalizedMat = normalizeMat(grayMat)

    val watchableMat = watchMat(normalizedMat)
    Imgcodecs.imwrite("/tmp/gregoire.jpg", watchableMat)
  }

  def detectEyes(mat: Mat): (Point, Point) = {
    val classifier = new CascadeClassifier("/usr/share/opencv/haarcascades/haarcascade_eye.xml")
    val matOfRect = new MatOfRect()
    classifier.detectMultiScale(mat, matOfRect)

    val array = matOfRect.toArray()

    val (otherEyeAsRect, oneEyeAsRect) = (array(0), array(1))

    val (oneEyeAsPoint, otherPointAsEye) = (rectCenter(oneEyeAsRect), rectCenter(otherEyeAsRect))

    if (oneEyeAsPoint.x < otherPointAsEye.x) (oneEyeAsPoint, otherPointAsEye) else (otherPointAsEye, oneEyeAsPoint)
  }

  def rectCenter(rect: Rect): Point = {
    new Point(
      rect.x + rect.width / 2,
      rect.y + rect.height / 2
    )
  }

  def translationMat(dx: Double, dy: Double): Mat = {
    val mat = Mat.eye(2, 3, CvType.CV_32FC1)
    mat.put(0, 2, dx)
    mat.put(1, 2, dy)
    mat
  }

  def sqr(double: Double) = pow(double, 2)

  def mean(mat: Mat): Double = {
    val meanAsMatOfDouble = new MatOfDouble(0d)
    val stddevAsMatOfDouble = new MatOfDouble(0d)
    Core.meanStdDev(mat, meanAsMatOfDouble, stddevAsMatOfDouble)
    val mean: Double = meanAsMatOfDouble.toArray()(0)
    mean
  }

  def filterWithGaborKernel(mat: Mat): Mat = {
    val filteredMat = new Mat()



    filteredMat
  }



  def stdDev(mat: Mat): Double = {
    val meanAsMatOfDouble = new MatOfDouble()
    val stddevAsMatOfDouble = new MatOfDouble()
    Core.meanStdDev(mat, meanAsMatOfDouble, stddevAsMatOfDouble)
    val stdDev: Double = stddevAsMatOfDouble.toArray()(0)
    stdDev
  }

  def smoothEdge(mat: Mat, width: Int): Mat = {
    val matWithSmoothEdge = mat.clone()
    for (i <- 0 to (width - 1)) {
      val scale = i.toDouble / width ;

      for (y <- 0 to (mat.cols() - 1)) {
        matWithSmoothEdge.put(i, y, matWithSmoothEdge.get(i, y)(0) * scale)
        matWithSmoothEdge.put(mat.rows() - i - 1, y, matWithSmoothEdge.get(mat.rows() - i - 1, y)(0) * scale)
      }

      for (x <- 0 to (mat.rows() - 1)) {

        matWithSmoothEdge.put(x, i, matWithSmoothEdge.get(x, i)(0) * scale)
        matWithSmoothEdge.put(x, mat.cols() - i - 1, matWithSmoothEdge.get(x, mat.cols() - i - 1)(0) * scale)
      }


    }
    matWithSmoothEdge
  }

  def rotateMat(mat: Mat, center: Point, angle: Double): Mat = {
    val scale = 1
    val rotationMat = Imgproc.getRotationMatrix2D(center, angle, scale)
    val rotatedMat = new Mat()
    Imgproc.warpAffine(mat, rotatedMat, rotationMat, mat.size())
    rotatedMat
  }

  def angleBetween(p1: Point, p2: Point): Double = {
    val len1 = sqrt(sqr(p1.x) + sqr(p1.y))
    val len2 = sqrt(sqr(p2.x) + sqr(p2.y))
    val dot = p1.x * p2.x + p1.y * p2.y

    val a = dot / (len1 * len2)

    if (a >= 1.0) {
      0d
    } else if (a <= -1) {
      Pi
    } else {
      acos(a) * 180d / Pi
    }
  }

  def drawPoint(mat: Mat, point: Point, color: Double = 255d): Mat = {
    val matWithPoint = mat.clone()
    //Imgproc.line(matWithPoint, new Point(point.x, point.y - 3), new Point(point.x, point.y + 3), Scalar.all(255d))
    Imgproc.circle(matWithPoint, point, 5, Scalar.all(color))
    //Imgproc.line(matWithPoint, new Point(point.x - 3, point.y), new Point(point.x + 3, point.y), Scalar.all(255d))
    matWithPoint
  }

  def distanceBetween(p1: Point, p2: Point): Double = {
    sqrt(sqr(p2.x - p1.x) + sqr(p2.y - p1.y))
  }

  def normalizeMat(mat: Mat): Mat = {
    var normalizedMat = mat.clone()

    val (oneEyeAsPoint, otherEyeAsPoint) = detectEyes(normalizedMat)
    normalizedMat = drawPoint(mat, oneEyeAsPoint)
    normalizedMat = drawPoint(normalizedMat, otherEyeAsPoint)

    normalizedMat.convertTo(normalizedMat, CvType.CV_32FC1)
    val m1 = mean(normalizedMat)
    Core.subtract(normalizedMat, Scalar.all(m1), normalizedMat)

    normalizedMat = smoothEdge(normalizedMat, 30)

    Imgproc.warpAffine(normalizedMat, normalizedMat, Imgproc.getRotationMatrix2D(oneEyeAsPoint, angleBetween(oneEyeAsPoint, otherEyeAsPoint) / 2, 1), mat.size())

    val referencePoints = (new Point(52, 64), new Point(76, 64))

    val rd = distanceBetween(oneEyeAsPoint, otherEyeAsPoint)
    val d = distanceBetween(referencePoints._1, referencePoints._2)
    val scale = d / rd

    Imgproc.resize(normalizedMat, normalizedMat, new Size((normalizedMat.rows() * scale).toInt, (normalizedMat.cols() * scale).toInt))

    val dx = referencePoints._1.x - (oneEyeAsPoint.x * scale)
    val dy = referencePoints._1.y - (oneEyeAsPoint.y * scale)

    Imgproc.warpAffine(normalizedMat, normalizedMat, translationMat(dx, dy), normalizedMat.size())

    normalizedMat = normalizedMat.submat(new Rect(new Point(0, 0), new Size(128, 128)))

    val m2 = mean(normalizedMat)
    Core.subtract(normalizedMat, Scalar.all(m2), normalizedMat)
    val s2 = stdDev(normalizedMat)
    Core.divide(normalizedMat, Scalar.all(s2), normalizedMat)


    //normalizedMat = drawPoint(normalizedMat, referencePoints._1, 0)
    //normalizedMat = drawPoint(normalizedMat, referencePoints._2, 0)

    normalizedMat = smoothEdge(normalizedMat, 30)

    normalizedMat
  }

  def detectFace(mat: Mat): Mat = {
    val faceCascade = "/usr/share/opencv/haarcascades/haarcascade_frontalface_default.xml"
    val cascadeClassifier = new CascadeClassifier(faceCascade)
    val matOfRect = new MatOfRect()
    cascadeClassifier.detectMultiScale(mat, matOfRect)

    val faceRect = matOfRect.toArray()(0)

    val normalizedFaceRect = increaseRect(faceRect, 1)

    val faceMat = mat.submat(normalizedFaceRect)
    faceMat
  }

  def increaseRect(rect: Rect, percent: Percent): Rect = {
    val (height, width) = ((1 + percent) * rect.height.toDouble, (1d + percent) * rect.width.toDouble)
    val (heightDiff, widthDiff) = ((height - rect.height) / 2d, (width - rect.width) / 2d)

    val (x, y) = (max(rect.x - widthDiff, 0), max(0, rect.y - heightDiff))

    new Rect(x.toInt, y.toInt, width.toInt, height.toInt)
  }

  def grayscale(mat: Mat): Mat = {
    val blackAndWhiteMat = new Mat()
    Imgproc.cvtColor(mat, blackAndWhiteMat, Imgproc.COLOR_BGR2GRAY)
    blackAndWhiteMat
  }

  def watchMat(mat: Mat): Mat = {
    printMat(mat)
    val watchableMat = new Mat()
    val r = Core.minMaxLoc(mat)
    val (maxVal, minVal) = (r.maxVal, r.minVal)
    //watchableMat.convertTo(watchableMat, CvType.CV_8UC1, 255d/(maxVal - minVal), -minVal)

    mat.convertTo(watchableMat, CvType.CV_8UC1, 255d / (maxVal - minVal), -255d * minVal / (maxVal - minVal))
    printMat(watchableMat)
    watchableMat
  }

  def printMat(mat: Mat): Unit = {
    for (i <- 0 to (mat.rows() - 1)) {
      for (j <- 0 to (mat.cols() - 1)) {
        print(mat.get(i, j)(0))
        print("\t")
      }
      println()
    }

  }

}
