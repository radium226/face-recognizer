package radium226

import javafx.application.{Application, Platform}
import javafx.stage.Stage

/**
  * Created by adrien on 1/27/17.
  */
object TryToDetectFaces {

  def main(arguments: Array[String]): Unit = {
    Application.launch(classOf[VideoApplication], arguments: _*)
  }

}
