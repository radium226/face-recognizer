package radium226.sandbox

import javafx.application.Application

import radium226.VideoApplication

/**
  * Created by adrien on 1/27/17.
  */
object TryToDetectFaces {

  def main(arguments: Array[String]): Unit = {
    System.load("/usr/share/opencv/java/libopencv_java320.so")
    Application.launch(classOf[VideoApplication], arguments: _*)
  }

}
