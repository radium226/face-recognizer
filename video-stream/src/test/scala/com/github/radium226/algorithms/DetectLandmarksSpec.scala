package com.github.radium226.algorithms

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.radium226.dlib.DLibModel
import com.github.radium226.opencv.OpenCV
import com.github.radium226.scalatest.AbstractSpec
import com.github.radium226.video.Video
import com.github.radium266.dlib.swig.{FaceDescriptorComputer, ShapePredictor}
import org.opencv.objdetect.CascadeClassifier
import com.github.radium226.opencv.OpenCVImplicits._
import org.opencv.core.Point

import scala.concurrent.duration._
import scala.collection.JavaConverters._

class DetectLandmarksSpec extends AbstractSpec {

  implicit var system: ActorSystem = _
  implicit var materializer: ActorMaterializer = _

  override def beforeAll(): Unit = {
    super.beforeAll()

    OpenCV.loadLibraries()

    info("Starting system and materializer... ")
    system = ActorSystem()
    materializer = ActorMaterializer()
  }

  behavior of "Landmark"

  it should "be able to detect landmarks" in {
    info("Creating face descriptor computer... ")
    val shapePredictorModelFilePath = DLibModel.SHAPE_PREDICTOR_68_FACE_LANDMARKS.extract()
    val shapePredictor = new ShapePredictor(shapePredictorModelFilePath.toString)

    info("Creating face cascade classifier... ")
    val cascadeClassifierModelFilePath = Paths.get("/usr/share/opencv/haarcascades/haarcascade_frontalface_default.xml")
    val faceCascadeClassifier = new CascadeClassifier(cascadeClassifierModelFilePath.toString)

    info("Creating graph...")
    val videoFilePath = Paths.get("/home/adrien/Personal/Projects/video-miner/video-stream/src/test/resources/Macron.mkv") //Paths.get("/home/adrien/Personal/Media/Videos/TV Series/The Handmaid's Tale [2017-]/Se01/Ep06/the.handmaids.tale.s01e06.720p.webrip.x264-morose.mkv"); //"/home/adrien/Personal/Projects/video-miner/video-stream/src/test/resources/aspen.webm")
    val graph = Video.open(videoFilePath)
        .map(_.resize(320 * 3, 180 * 3))
        .map({ frame =>
          (frame, frame.detectFaces().headOption)
        })
        .collect({
          case (frame, Some(faceBoudaries)) =>
            (frame, shapePredictor.predictShape(frame.submat(faceBoudaries)).asScala.map({ point =>
              new Point(faceBoudaries.x + point.x, faceBoudaries.y + point.y)
            }))
        })
        .map({ case (frame, marks) =>
          frame.drawMarkers(marks)
          frame
        })
        .to(Video.play())

    info("Running graph...")
    graph.run()

    await(1 minute)
  }

}
