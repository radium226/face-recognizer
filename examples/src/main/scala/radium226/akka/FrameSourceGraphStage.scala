package radium226.akka

import akka.stream.{Attributes, Outlet, SourceShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import com.github.radium226.io.VideoInputStream
import radium226._

object FrameSourceGraphStage {

  val ChannelCount = 3

  val OutName = "frames"

  def apply(inputStreamCreator: InputStreamCreator): FrameSourceGraphStage = {
    new FrameSourceGraphStage(inputStreamCreator)
  }

}

class FrameSourceGraphStage(inputStreamCreator: InputStreamCreator) extends GraphStage[SourceShape[Frame]] {

  import FrameSourceGraphStage._

  val out: Outlet[Frame] = Outlet(OutName)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    val inputStream = inputStreamCreator()
    val videoInputStream = new VideoInputStream(inputStream)

    val size = Size(videoInputStream.getMetaData.getWidth, videoInputStream.getMetaData.getHeight)
    val byteArraySize = size.width * size.height * ChannelCount

    val byteArray = Array.ofDim[Byte](byteArraySize)

    setHandler(out, new OutHandler {
      override def onDownstreamFinish(): Unit = {
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
        push(out, Frame(size, byteArray))
      }
    })
  }

  override def shape: SourceShape[Frame] = SourceShape(out)

}
