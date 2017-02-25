package radium226.rx

import java.io.{EOFException, IOException, InputStream}

import monix.execution.{Ack, Cancelable}
import monix.reactive.{Observable, OverflowStrategy}
import radium226.BGRByteArray

/**
  * Created by adrien on 1/27/17.
  */
object BGRByteArrayObservable {

  def apply(inputStream: InputStream, width: Int, height: Int): Observable[BGRByteArray] = {
    ByteArrayObservable(inputStream, width * height * 3)
  }

}
