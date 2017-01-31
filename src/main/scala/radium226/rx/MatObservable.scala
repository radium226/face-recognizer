package radium226.rx

import java.io.InputStream

import com.github.radium226.io.VideoInputStream
import monix.reactive.Observable
import org.opencv.core.Mat
import radium226.Implicits._

/**
  * Created by adrien on 1/30/17.
  */
object MatObservable {

  def apply(inputStream: InputStream): Observable[Mat] = {
    val videoInputStream = new VideoInputStream(inputStream)
    val videoMetaData = videoInputStream.getMetaData()
    val (width, height) = (videoMetaData.getWidth, videoMetaData.getHeight)

    BGRByteArrayObservable(videoInputStream, width, height).map(_.toMat(width, height))
  }

}
