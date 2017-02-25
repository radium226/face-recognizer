package radium

import org.opencv.core.{Core, Mat, Scalar}
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.core.{ Point => OpenCVPoint }

/**
  * Created by adrien on 2/21/17.
  */
object TryAdder {

  type BGRByteArray = Array[Byte]

  val FaceImageFilePath = "/home/adrien/Personal/Projects/video-miner/examples/src/main/resources/john_doe.jpg"



  def main(arguments: Array[String]): Unit = {
    System.load("/home/adrien/Downloads/dlib/dlib-19.2/build/dlib/libdlib.so")
    System.load("/usr/share/opencv/java/libopencv_java320.so")

    val mat = Imgcodecs.imread(FaceImageFilePath, Imgcodecs.CV_LOAD_IMAGE_COLOR)

    println(DLib.convertMatToBGRByteArray(mat).length)

    DLib.landmarks(mat).foreach({ p =>

      Imgproc.circle(mat, new OpenCVPoint(p.x, p.y), 2, new Scalar(255, 0, 255))
    })

    Imgcodecs.imwrite("/tmp/test.png", mat)
  }

}
