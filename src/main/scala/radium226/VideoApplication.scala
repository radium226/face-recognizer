package radium226

import java.io.DataInputStream
import java.nio.file.Paths
import java.util.concurrent.{ExecutorService, Executors}
import javafx.application.{Application, Platform}
import javafx.scene.Scene
import javafx.scene.image.{ImageView, WritableImage}
import javafx.scene.layout.Pane
import javafx.stage.Stage

import monix.execution.Scheduler.Implicits.global
import Implicits._
import com.github.radium226.YouTube
import com.google.common.util.concurrent.RateLimiter
import monix.execution.Scheduler
import monix.reactive.Observable
import org.opencv.core.{Mat, MatOfRect, Rect, Scalar}
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import radium226.rx.MatObservable

class VideoApplication extends Application {

  val PalmashowURL = "https://www.youtube.com/watch?v=6Jyes8Hzwn4"

  val HaarCascadeParentFolderPath = Paths.get("/usr/share/opencv/haarcascades")

  override def start(stage: Stage): Unit = {
    System.load("/usr/share/opencv/java/libopencv_java310.so")

    //val width = 500
    //val height = 200
    val imageView = new ImageView()

    val pane = new Pane(imageView)
    val scene = new Scene(pane)
    stage.setScene(scene)
    //stage.setMinWidth(writableImage.getWidth)
    //stage.setMinHeight(writableImage.getHeight)

    stage.show()

    val executor = Executors.newSingleThreadExecutor()

    executor.submit({ () =>

      val rateLimiter = RateLimiter.create(24)
      val poolSize = 5
      val fixedPoolScheduler = Scheduler.fixedPool("detect-faces", poolSize)

      val mats = MatObservable(PalmashowURL)
          .map(_.resize(50 percent))
        .publish


      val haarCascades = Seq(("haarcascade_frontalface_default.xml", new Scalar(255, 0, 0)), ("haarcascade_profileface.xml", new Scalar(0, 255, 0)), ("haarcascade_lowerbody.xml", new Scalar(0, 0, 255)))
        .map({ case (fileName, color) => (HaarCascadeParentFolderPath.resolve(fileName), color) })
        .map({ case (filePath, color) => (new CascadeClassifier(filePath.toString), color) })

      val rectsAndColors = mats
        .map({ mat =>
          haarCascades
            .map({ case (cascadeClassifier, color) =>
              (mat.detectMultiScale(cascadeClassifier).toSeq, color)
            })
            .flatMap({ case (rects, color) => rects.map((_, color)) })
        })


      val taggedMats = Observable.zip2(mats, rectsAndColors)
        .map({ case (mat, rectsAndColors) =>
          rectsAndColors.foldLeft[Mat](mat.clone())({ case (oldMat: Mat, (rect: Rect, color: Scalar)) =>
            val newMat = oldMat.clone()
            Imgproc.rectangle(newMat, rect.tl(), rect.br(), color, 3)
            newMat
          })
        })
        .map({ mat: Mat => mat.toImage })
        .foreach(imageView.setImage(_))

      Thread.sleep(5000);
      mats.connect()

    })

    println("Here I am")

    stage.setOnCloseRequest(event => Platform.exit())
    stage.show()
  }

  def detectFacesInBatch(mats: Seq[Mat], cascadeClassifier: CascadeClassifier, scheduler: Scheduler): Observable[Mat] = {
    Observable.fromIterable(mats)
      .flatMap(matWithoutFaces => {
          Observable.defer[Mat]({
            println(Thread.currentThread())
            val facesAsMatOfRect = new MatOfRect()
            val matWithFaces = matWithoutFaces.clone()
            cascadeClassifier.detectMultiScale(matWithoutFaces, facesAsMatOfRect)
            val facesAsRectArray = facesAsMatOfRect.toArray
            for (faceAsRect <- facesAsRectArray) {
              Imgproc.rectangle(matWithFaces, faceAsRect.tl, faceAsRect.br, new Scalar(255, 255, 255))
            }
            Observable(matWithFaces)
          }).subscribeOn(scheduler)
      })
  }

}
