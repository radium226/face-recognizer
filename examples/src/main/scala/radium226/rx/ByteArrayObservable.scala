package radium226.rx

import java.io.InputStream

import monix.reactive.Observable
import org.slf4j.LoggerFactory

import scala.concurrent.blocking


object ByteArrayObservable {

  val Logger = LoggerFactory.getLogger(getClass)

  def apply(inputStream: InputStream, byteArraySize: Int): Observable[Array[Byte]] = {
    val inputStreamIterator = new Iterator[Array[Byte]] {

      var nextByteArray: Array[Byte] = null

      override def hasNext(): Boolean = {
        blocking {
          val byteArray = Array.ofDim[Byte](byteArraySize)
          var readByteTotalCount = 0
          var shouldBreakWhile = false
          while (readByteTotalCount < byteArraySize && !shouldBreakWhile) {
            val readByteCount = inputStream.read(byteArray, readByteTotalCount, byteArraySize - readByteTotalCount)
            Logger.debug(s"readByteCount=${readByteCount}")
            if (readByteCount <= 0) {
              shouldBreakWhile = true
              return false
            } else {
              readByteTotalCount = readByteTotalCount + readByteCount
            }
          }
          nextByteArray = byteArray
          return true
        }
      }

      override def next(): Array[Byte] = nextByteArray

    }

    Observable.fromIterator[Array[Byte]](inputStreamIterator)
  }

}
