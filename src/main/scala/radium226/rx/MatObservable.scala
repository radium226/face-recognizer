package radium226.rx

import java.io.InputStream

import com.github.radium226.io.VideoInputStream
import monix.reactive.Observable
import org.opencv.core.Mat
import radium226.Implicits._
import radium226.VideoOpener

/**
  * Created by adrien on 1/30/17.
  */
object MatObservable {

  /*def apply(inputStream: InputStream): Observable[Mat] = {
    val videoInputStream = new VideoInputStream(inputStream)
    val videoMetaData = videoInputStream.getMetaData()
    val (width, height) = (videoMetaData.getWidth, videoMetaData.getHeight)

    BGRByteArrayObservable(videoInputStream, width, height).map(_.toMat(width, height))
  }*/

  def apply(url: String): Observable[Mat] = {
    Observable.defer[Mat]({
      VideoOpener(url)
        .openVideo()
        .map[Observable[Mat]]({ inputStream: InputStream =>
          val videoInputStream = new VideoInputStream(inputStream)
          val videoMetaData = videoInputStream.getMetaData
          val (width, height) = (videoMetaData.getWidth, videoMetaData.getHeight)
          BGRByteArrayObservable(videoInputStream, width, height).doOnComplete(() => videoInputStream.close()).map(_.toMat(width, height))
        })
        .getOrElse[Observable[Mat]](Observable.empty[Mat])
    })
  }

}
