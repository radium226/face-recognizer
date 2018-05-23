package radium226.akka

import akka.stream.scaladsl._
import org.opencv.core.Mat
import org.opencv.objdetect.CascadeClassifier

import radium226.Implicits._

object MatFlow {

  def detectMultiscale(cascadeClassifier: CascadeClassifier) = Flow[Mat].map(_.detectMultiScale(cascadeClassifier)).map(_.toSeq)

}
