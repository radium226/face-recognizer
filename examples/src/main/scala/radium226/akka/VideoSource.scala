package radium226.akka

import java.io.InputStream

import akka.NotUsed
import akka.stream.IOResult
import com.github.radium226.io.VideoInputStream
import org.opencv.core.Mat
import radium226.BGRByteArray
import radium226.opener.VideoOpener
import radium226.rx.BGRByteArrayObservable
import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import scala.collection.Iterable

import scala.concurrent.ExecutionContext.Implicits._

import scala.concurrent.Future

import radium226.Implicits._

import scala.sys.process._

/*object VideoSource {

  def apply(url: String): Source[Mat, _] = {
    val youtubeDL = ""
    val ffmpeg = ""

    /*VideoOpener(url)
      .openVideo()
      .map({ inputStream: InputStream =>
        val videoInputStream = new VideoInputStream(inputStream)
        val videoMetaData = videoInputStream.getMetaData
        val (width, height) = (videoMetaData.getWidth, videoMetaData.getHeight)
        Thread.sleep(2000)
        StreamConverters.fromInputStream({ () => videoInputStream }, chunkSize = width * height * 3)
          .map(_.toArray.toMat(width, height))
      })
    .getOrElse(Source.empty[Mat])*/

  }

}*/
