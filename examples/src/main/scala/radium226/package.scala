import java.io.InputStream

import org.opencv.core.Mat

import scala.concurrent.Future

/**
  * Created by adrien on 1/27/17.
  */
package object radium226 {

  type Width = Int

  type Height = Int

  type BGRByteArray = Array[Byte]

  type ARGBIntArray = Array[Int]

  type InputStreamCreator = () => InputStream

  type MatAlterer = Mat => Unit

}
