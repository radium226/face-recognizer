package radium226.akka

import org.opencv.core.{Mat, Rect, Scalar}
import org.opencv.imgproc.Imgproc
import radium226._

object MatAlterer {

  def rectangle(r: Rect): MatAlterer = { mat: Mat =>
    Imgproc.rectangle(mat, r.tl, r.br, new Scalar(255, 255, 255))
  }

  def rectangle(o: Point, s: Size): MatAlterer = rectangle(new Rect(o.x, o.y, o.x + s.width, o.y + s.height))

}
