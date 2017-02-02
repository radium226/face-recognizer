package radium226.sandbox

import monix.reactive.Observable
import org.slf4j.LoggerFactory

import scala.util.Random


object RandomDoubleObservable {

  lazy val Logger = LoggerFactory.getLogger(getClass)

  def apply(maxDouble: Double = 1000): Observable[Double] = {
    val randomDoubleIterable = new Iterable[Double] {

      def iterator = {
        val random = new Random()
        val doubleIterator = new Iterator[Double] {

          override def hasNext: Boolean = true

          override def next(): Double = {
            val nextRandomDouble = random.nextDouble() * maxDouble
            Logger.info("Next random double is {}", nextRandomDouble)
            nextRandomDouble
          }
        }
        doubleIterator
      }

    }

    val observable = Observable.fromIterable[Double](randomDoubleIterable)
    observable
  }

}
