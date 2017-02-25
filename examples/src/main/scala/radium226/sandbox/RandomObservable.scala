package radium226.sandbox

import monix.reactive.Observable
import org.slf4j.LoggerFactory

import scala.util.Random


object RandomObservable {

  lazy val Logger = LoggerFactory.getLogger(getClass)

  def apply[A](generator: Random => A): Observable[A] = {
    val randomIterable = new Iterable[A] {

      def iterator = {
        val random = new Random()
        new Iterator[A] {

          override def hasNext: Boolean = true

          override def next(): A = generator(random)
        }
      }

    }

    val observable = Observable.fromIterable[A](randomIterable)
    observable
  }

}
