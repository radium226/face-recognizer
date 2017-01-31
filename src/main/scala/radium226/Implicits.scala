package radium226

import java.io.{DataInputStream, EOFException, InputStream}
import javafx.scene.image.{Image, PixelFormat, WritableImage}

import org.opencv.core._
import org.opencv.core.{ Size => OpenCVSize }
import org.opencv.imgproc.Imgproc

import scala.collection.JavaConverters._

object Implicits {

  implicit class BGRByteArrayWithMoreMethods(bgrByteArray: BGRByteArray) {

    def toMat(width: Int, height: Int): Mat = {
      val mat = new Mat(height, width, CvType.CV_8UC3)
      mat.put(0, 0, bgrByteArray)
      mat
    }

    def toARGBIntArray(): ARGBIntArray = {
      val argbIntArrayLength = bgrByteArray.length / 3
      var offset = 0
      val argbIntArray: ARGBIntArray = Array.fill[Int](argbIntArrayLength) {
        val b = bgrByteArray((3 * offset) + 0) & 255
        val g = bgrByteArray((3 * offset) + 1) & 255
        val r = bgrByteArray((3 * offset) + 2) & 255
        val a = 255 & 255
        val argbInt = a << 24 | r << 16 | g << 8 | b
        offset = offset + 1
        argbInt
      }
      argbIntArray
    }

  }

  implicit class ARGBIntArrayWithMoreMethods(argbIntArray: ARGBIntArray) {

    def toImage(width: Int, height: Int): Image = {
      val writableImage = new WritableImage(width, height)
      val pixelWriter = writableImage.getPixelWriter
      pixelWriter.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance, argbIntArray, 0, width)
      writableImage
    }

  }

  implicit class MatWithMoreMethods(mat: Mat) {

    def toBGRByteArray(): BGRByteArray = {
      val bgrByteArray: BGRByteArray = Array.fill[Byte](mat.width() * mat.height() * mat.channels()) { 0 }
      mat.get(0, 0, bgrByteArray)
      bgrByteArray
    }

    def toImage(): Image = {
      mat.toARGBIntArray().toImage(mat.width(), mat.height())
    }

    def toARGBIntArray(): ARGBIntArray = {
      mat.toBGRByteArray().toARGBIntArray()
    }

    def resize(ratio: Ratio): Mat = {
      val resizedMat = new Mat()
      Imgproc.resize(mat, resizedMat, new OpenCVSize(), ratio.width, ratio.height, Imgproc.INTER_CUBIC)
      resizedMat
    }

    def resize(size: Size): Mat = {
      val resizedMat = new Mat()
      Imgproc.resize(mat, resizedMat, size, 0d, 0d, Imgproc.INTER_CUBIC)
      resizedMat
    }

    def grayscale(): Mat = {
      val m = new Mat()
      Imgproc.cvtColor(mat, m, Imgproc.COLOR_BGR2GRAY)
      Imgproc.cvtColor(m, m, Imgproc.COLOR_GRAY2BGR)
      m
    }

  }

  implicit class IntSeqToMatOfInt(intSeq: Seq[Int]) {

    def toMatOfInt(): MatOfInt = {
      val matOfInt = new MatOfInt(intSeq: _*)
      matOfInt
    }

  }

  implicit class InputStreamWithMoreMethods(inputStream: InputStream) {

    def readBGRByteArray(width: Int, height: Int): BGRByteArray = {
      val bgrByteArrayLength = width * height * 3;
      val bgrByteArray = Array.ofDim[Byte](bgrByteArrayLength)

      var n = 0;
      while (n < bgrByteArrayLength) {
        val readByteCount = inputStream.read(bgrByteArray, n, bgrByteArrayLength - n);
        if (readByteCount < 0)
          throw new EOFException();
        n += readByteCount;
      }

      bgrByteArray
    }

  }

  implicit class DoubleWithMoreMethods(double: Double) {

    def percent(): Ratio = {
      Ratio(double / 100d, double / 100d)
    }

  }

  implicit class PairOfIntWithMoreMethods(pair: (Int, Int)) {

    def px(): Size = {
      val (width, height) = pair
      Size(width, height)
    }

  }

  implicit def sizeToOpenCVSize(size: Size): OpenCVSize = {
    new OpenCVSize(size.width, size.height)
  }

}
