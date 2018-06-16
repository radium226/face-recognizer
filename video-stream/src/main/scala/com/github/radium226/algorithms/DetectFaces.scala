package com.github.radium226.algorithms

import akka.NotUsed
import akka.stream.scaladsl.Flow
import org.opencv.core.{Mat, Point}

case class Face(landmarks: Seq[Point])

object DetectFaces {

  def apply(): Flow[Mat, Seq[Face], NotUsed] = {
    Flow[Mat]
      .map({ _ =>
        Seq.empty[Face]
      })
  }

}
