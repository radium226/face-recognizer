package com.github.radium226.algorithms

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl._
import com.github.radium226.scalatest.AbstractSpec
import org.opencv.core.{Mat, Rect}

object FaceID {

  private var nextValue = 0

  def random(): FaceID = {
    val id = FaceID(nextValue)
    nextValue += 1
    id
  }

}

case class FaceID(value: Int)

object FaceDetector {

  val PreviousFramesCount = 24

  def detectFaces(): Flow[Mat, List[(FaceID, Rect)], NotUsed] = {
    Flow[Mat]
      .map({ frame =>
        // Detect faces
        (frame, List.empty[Rect])
      })
      // We buffer in order to have some more history
      .statefulMapConcat[List[(Mat, List[Rect])]]({ () =>
        var previousFramesAndRects = List.empty[(Mat, List[Rect])]

        { currentFrameAndRects: (Mat, List[Rect]) =>
          val framesAndRects = previousFramesAndRects :+ currentFrameAndRects
          previousFramesAndRects = framesAndRects.takeRight(PreviousFramesCount)
          List(framesAndRects)
        }
      })
      .map({ t =>

      })
  }

}


class DetectFacesSpec extends AbstractSpec {

}
