package radium226

import java.io.DataInputStream
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
import org.opencv.core.{Mat, MatOfRect, Scalar}
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import radium226.rx.MatObservable

class VideoApplication extends Application {

  val PalmashowURL = "https://www.youtube.com/watch?v=6Jyes8Hzwn4"

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

    executor.submit(() => {

      val rateLimiter = RateLimiter.create(24)
      val inputStream = YouTube.rawOpen(PalmashowURL)

      val cascadeClassifier = new CascadeClassifier("/usr/share/opencv/haarcascades/haarcascade_frontalface_alt.xml")
      val poolSize = 5
      val fixedPoolScheduler = Scheduler.fixedPool("detect-faces", poolSize)

      MatObservable(inputStream)
        .map(_.resize(50 percent))
        .zipWithIndex
        .mergeMap(matWithoutFacesWithIndex => {
            Observable.defer[Mat]({
              val (matWithoutFaces, index) = matWithoutFacesWithIndex
              val facesAsMatOfRect = new MatOfRect()
              val matWithFaces = matWithoutFaces.clone()
              cascadeClassifier.detectMultiScale(matWithoutFaces, facesAsMatOfRect)
              val facesAsRectArray = facesAsMatOfRect.toArray
              for (faceAsRect <- facesAsRectArray) {
                Imgproc.rectangle(matWithFaces, faceAsRect.tl, faceAsRect.br, new Scalar(255, 255, 255))
              }
              Observable((matWithFaces, index))
            }).subscribeOn(fixedPoolScheduler)
        })
        .map(_.toImage())
        .foreach(image => {
          //rateLimiter.acquire()
          imageView.setImage(image)
        })
    });

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
