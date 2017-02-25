package radium226

import org.opencv.core.Mat

package object models {

  case class Face(mat: Mat)

  case class Frame(mat: Mat, faces: Seq[Face])

}
