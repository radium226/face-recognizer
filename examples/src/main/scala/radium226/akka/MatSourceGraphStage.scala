package radium226.akka

import java.io.InputStream

import akka.stream.{Attributes, Outlet, SourceShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import com.github.radium226.io.VideoInputStream
import org.opencv.core.Mat
import radium226.BGRByteArray
import radium226.rx.ByteArrayObservable.Logger
import radium226.Implicits._

// http://doc.akka.io/docs/akka/2.5.2/scala/stream/stream-customize.html
class MatSourceGraphStage(inputStream: InputStream) extends GraphStage[SourceShape[Mat]] {

  val out: Outlet[Mat] = Outlet("mat")

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    val videoInputStream = new VideoInputStream(inputStream)

    val width = videoInputStream.getMetaData.getWidth
    val height = videoInputStream.getMetaData.getHeight
    val byteArraySize = width * height * 3
    val byteArray = Array.ofDim[Byte](byteArraySize)


    setHandler(out, new OutHandler {
      override def onDownstreamFinish(): Unit = {
        videoInputStream.close()
        super.onDownstreamFinish()
      }

      override def onPull(): Unit = {
        var readByteTotalCount = 0
        while (readByteTotalCount < byteArraySize) {
          val readByteCount = videoInputStream.read(byteArray, readByteTotalCount, byteArraySize - readByteTotalCount)
          if (readByteCount <= 0) {
            complete(out)
            videoInputStream.close()
          } else {
            readByteTotalCount = readByteTotalCount + readByteCount
          }
        }
        push(out, byteArray.toMat(width, height))
      }
    })
  }

  override def shape: SourceShape[Mat] = SourceShape(out)
}