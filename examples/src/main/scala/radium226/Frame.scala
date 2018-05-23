package radium226

import javafx.scene.image.Image

import org.opencv.core.Mat
import Implicits._

case class Frame(size: Size, byteArray: BGRByteArray) {

  def toMat: Mat = byteArray.toMat(size.width, size.height)

  def toImage: Image = {
    byteArray.toARGBIntArray.toImage(size.width, size.height)
  }

}
