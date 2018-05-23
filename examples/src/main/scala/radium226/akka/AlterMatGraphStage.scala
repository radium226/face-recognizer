package radium226.akka

import java.util.concurrent.Executors
import javafx.application.{Application, Platform}
import javafx.scene.Scene
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.stage.Stage

import akka.stream.scaladsl._
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.StreamConverters
import akka.stream.stage._
import com.google.common.util.concurrent.RateLimiter
import org.opencv.core.Mat
import radium226.Implicits._
import radium226.{Frame, MatAlterer}
import radium226.akka.FrameSourceGraphStage.OutName
import radium226.opener.VideoOpener

import scala.sys.process._

case class AlterMatGraphStage() extends GraphStage[FanInShape[Mat]] {

  val out: Outlet[Mat] = Outlet("altered-mats")

  val in: Inlet[Mat] = Inlet("mats")
  val alterer: Inlet[Seq[MatAlterer]] = Inlet("mat-alterers")

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        Merge
      }
    })

    setHandler(out, new OutHandler {

      override def onPull(): Unit = {
        val mat = grab(in)
        val alterers = grab(alterer)
        alterers.foreach({ a => a(mat) })
        push(out, mat)
      }

    })


  }

  override def shape: FanInShape[Mat] = new FanInShape2(in, alterer, out)//FanInShape(matOut, Seq(matIn, matAltererIn))
}
