package radium226.akka

import java.util.concurrent.Executors
import javafx.application.{Application, Platform}
import javafx.scene.Scene
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.Pane
import javafx.stage.Stage

import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl._
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.StreamConverters
import com.google.common.util.concurrent.RateLimiter
import org.opencv.core.{Mat, Scalar}
import org.opencv.objdetect.CascadeClassifier
import radium226.Implicits._
import radium226._
import radium226.opener.VideoOpener

import scala.sys.process._

object VideoApplication {

  def main(arguments: Array[String]): Unit = {
    System.load("/usr/share/opencv/java/libopencv_java320.so")
    Application.launch(classOf[VideoApplication], arguments: _*)
  }

}

class VideoApplication extends Application {

  val PalmashowURL = "https://www.youtube.com/watch?v=6Jyes8Hzwn4"

  override def start(stage: Stage): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val imageView = new ImageView()
    val pane = new Pane(imageView)
    val scene = new Scene(pane)
    stage.setScene(scene)

    val rateLimiter = RateLimiter.create(24)

    val cascadeClassifier = new CascadeClassifier("/usr/share/opencv/haarcascades/haarcascade_frontalface_default.xml")

    val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val mat = Source.fromGraph(FrameSourceGraphStage({ () => VideoOpener(PalmashowURL).openVideo().get })).map(_.toMat)
      val view = Sink.foreach[Image]({ image =>
        imageView.setImage(image)
      })
      //val alterer = Source.fromIterator { () => Iterator.continually { Seq(MatAlterer.rectangle(Point(10, 10), Size(100, 100))) } }

      val zip = builder.add(Zip[Mat, Seq[MatAlterer]]())
      val broadcast = builder.add(Broadcast[Mat](2))

      mat.map(_.resize(50 percents)) ~> broadcast.in

      val alterer = broadcast.out(0) ~> MatFlow.detectMultiscale(cascadeClassifier).async/*.addAttributes(Attributes.inputBuffer(1, 1))*/.map(_.map(MatAlterer.rectangle(_)))


      alterer ~> zip.in1
      //Source.repeat(Seq[MatAlterer]()) ~> zip.in1
      broadcast.out(1) ~> zip.in0

      val alteredMat = zip.out
        .map({ case (mat, alterers) =>
          val matCopy = mat.clone()
          alterers.foreach({ alterer => alterer(matCopy) })
          matCopy
        })
        .map(_.toImage)

      alteredMat ~> view

      ClosedShape
    })

    graph.run()


    /*val c = Source.fromGraph(FrameSourceGraphStage({ () => VideoOpener(PalmashowURL).openVideo().get }))
        .map(_.toMat)
        .map(_.resize(50 percents))
        .map(_.toImage)
        .to(Sink.foreach({ image =>
          imageView.setImage(image)
        }))
      .run()*/

    stage.setOnCloseRequest(event => {
      Platform.exit()
    })
    stage.show()
  }

}
