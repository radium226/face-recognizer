package radium226.sandbox

import java.nio.file.{Files, Paths}
import java.util

import monix.execution.Scheduler.Implicits.global
import radium226.rx.ByteArrayObservable

object TryByteArrayIterator {

  def main(arguments: Array[String]): Unit = {
    val inputStream = Files.newInputStream(Paths.get("/home/adrien/Personal/Projects/marvin-example/src/main/resources/palmashow.webm"))
    ByteArrayObservable(inputStream, 2)
      .foreach((byteArray) => {
        println("----")
        println(util.Arrays.toString(byteArray))
      })


    Thread.sleep(10000000)
  }

}
