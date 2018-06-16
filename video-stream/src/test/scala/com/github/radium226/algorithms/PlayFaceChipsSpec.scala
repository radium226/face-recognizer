package com.github.radium226.algorithms

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.radium226.dlib.{DLib, DLibModel}
import com.github.radium226.opencv.OpenCV
import com.github.radium226.scalatest.AbstractSpec
import com.github.radium226.video.Video
import com.github.radium266.dlib.swig.FaceDescriptorComputer
import org.opencv.objdetect.CascadeClassifier

import com.github.radium226.opencv.OpenCVImplicits._

import scala.concurrent.duration._

class PlayFaceChipsSpec extends AbstractSpec {

  implicit var system: ActorSystem = _
  implicit var materializer: ActorMaterializer = _

  override def beforeAll(): Unit = {
    super.beforeAll()

    info("Loading native libraries")
    OpenCV.loadLibraries()
    DLib.loadLibraries()

    info("Starting system and materializer... ")
    system = ActorSystem()
    materializer = ActorMaterializer()
  }

  behavior of "Face Chips"

  it should "be able to play face chips" in {
    info("Creating face descriptor computer... ")
    val resnetModelFilePath = DLibModel.DLIB_FACE_RECOGNITION_RESNET_MODEL_V1.extract()
    val shapePredictorModelFilePath = DLibModel.SHAPE_PREDICTOR_68_FACE_LANDMARKS.extract()
    val faceDescriptorComputer = new FaceDescriptorComputer(shapePredictorModelFilePath.toString, resnetModelFilePath.toString)

    info("Creating face cascade classifier... ")
    val cascadeClassifierModelFilePath = Paths.get("/usr/share/opencv/haarcascades/haarcascade_frontalface_default.xml")
    val faceCascadeClassifier = new CascadeClassifier(cascadeClassifierModelFilePath.toString)

    info("Creating graph...")
    val videoFilePath = Paths.get("/home/adrien/Personal/Projects/video-miner/video-stream/src/test/resources/aspen.webm") //Paths.get("/home/adrien/Personal/Media/Videos/TV Series/The Handmaid's Tale [2017-]/Se01/Ep06/the.handmaids.tale.s01e06.720p.webrip.x264-morose.mkv"); //"/home/adrien/Personal/Projects/video-miner/video-stream/src/test/resources/aspen.webm")
    val graph = Video.open(videoFilePath)
      .map({ frame =>
        (frame, frame.detectFaces().headOption)
      })
      .collect({
        case (frame, Some(faceBoudaries)) =>
          faceDescriptorComputer.extractFaceChip(frame.submat(faceBoudaries))
      })
      .to(Video.play())

    info("Running graph...")
    graph.run()

    await(1 minute)
  }

  override def afterAll(): Unit = {
    info("Stopping materializer and system... ")
    materializer.shutdown()
    system.terminate()

    super.afterAll()
  }

}
