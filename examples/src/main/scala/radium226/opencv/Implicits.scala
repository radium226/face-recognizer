package radium226.opencv

import org.opencv.core.{Core, CvType, Mat, Size => OpenCVSize }
import org.opencv.imgproc.Imgproc
import radium226.Size

/**
  * Created by adrien on 2/13/17.
  */
object Implicits {

  implicit class MatWithMoreMethods(mat: Mat) {

    def filter2D(kernel: Mat): Mat = {
      val filteredMat = new Mat()
      Imgproc.filter2D(mat, filteredMat, CvType.CV_32F, kernel)
      filteredMat.convertTo(filteredMat, CvType.CV_8UC3)
      filteredMat
    }

    def gaborFilter(size: Size, sigma: Double, theta: Double, lambda: Double, gamma: Double): Mat = {
      val kernel = Imgproc.getGaborKernel(size, sigma, theta, lambda, gamma)
      mat.filter2D(kernel)
    }

  }

  implicit def sizeToOpenCVSize(size: Size): OpenCVSize = {
    new OpenCVSize(size.width, size.height)
  }

}
