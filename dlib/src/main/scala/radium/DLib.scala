package radium

import ch.jodersky.jni.nativeLoader
import org.opencv.core.Mat

import scala.collection.JavaConverters._

@nativeLoader("dlibjni0")
object DLib {

  type BGRByteArray = Array[Byte]

  val ShapeModelFilePath = "/home/adrien/Personal/Projects/video-miner/dlib/src/main/resources/shape_predictor_68_face_landmarks.dat"

  @native
  private[DLib] def landmarks0(shapeModelFilePath: String, width: Int, height: Int, imageAsBGRIntArray: BGRByteArray): java.util.List[Point]

  def convertMatToBGRByteArray(mat: Mat): Array[Byte] = {
    val bgrByteArray: BGRByteArray = Array.fill[Byte](mat.width() * mat.height() * mat.channels()) { 0 }
    mat.get(0, 0, bgrByteArray)
    bgrByteArray
  }

  def landmarks(width: Int, height: Int, imageAsBGRByteArray: BGRByteArray): Seq[Point] = landmarks0(ShapeModelFilePath, width, height, imageAsBGRByteArray).asScala

  def landmarks(mat: Mat): Seq[Point] = landmarks(mat.width(), mat.height(), convertMatToBGRByteArray(mat))


}
