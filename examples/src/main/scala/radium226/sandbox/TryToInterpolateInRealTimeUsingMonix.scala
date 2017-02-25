package radium226.sandbox

import com.google.common.util.concurrent.RateLimiter
import monix.execution.Ack.Continue
import monix.execution.{Cancelable, Scheduler}
import monix.execution.atomic.Atomic
import monix.reactive.{Observable, Observer, OverflowStrategy}
import monix.execution.Scheduler.Implicits.global
import org.reactivestreams.Subscription

import scala.concurrent.{Await, TimeoutException}
import scala.concurrent.duration.Duration
import scala.util.Random
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import scala.math.pow

/**
  * Created by adrien on 1/31/17.
  */
object TryToInterpolateInRealTimeUsingMonix {

  val NoneProbability: Double = 1d / 2d
  val MaxNumber: Double = 1000

  def randomNumbers(): Observable[Option[Double]] = {
    val random = new Random()
    val maxInt = (1d / NoneProbability).toInt
    val fourPerSecondRateLimiter = RateLimiter.create(4)

    val randomNumberIterator = new Iterator[Option[Double]] {

      override def hasNext(): Boolean = true

      override def next(): Option[Double] = {
        fourPerSecondRateLimiter.acquire()
        random.nextInt(maxInt) match {
          case int if int == 0 => None
          case _ => Some(random.nextDouble() * MaxNumber)
        }
      }
    }

    Observable.fromIterator[Option[Double]](randomNumberIterator)
  }

  def main(arguments: Array[String]): Unit = {
    val observable = randomNumbers()
    val connectableObservable = observable.publish
    val onePerSecondRateLimiter = RateLimiter.create(1)
    val twoPerSecondRateLimiter = RateLimiter.create(2)

    println("First cancelable future")
    val firstObservable = connectableObservable
      .map(n => {
        twoPerSecondRateLimiter.acquire()
        println(s" -[ ${Thread.currentThread().getId} ]-> $n * 2")
        n.map(_ * 2)
      })

    println("Second cancelable future")
    val secondObservable = connectableObservable
       .map({ n =>
         onePerSecondRateLimiter.acquire()
         println(s" -[ ${Thread.currentThread().getId} ]-> pow($n, 2)")
         n.map(pow(_, 2))
       })

    val cancelableFuture = Observable.zip3(connectableObservable, firstObservable, secondObservable)
      .foreach({ case (d: Option[Double], s: Option[Double], p: Option[Double]) =>
          println(s" =[ ${Thread.currentThread().getId} ]=> $d + $d = $s")
          println(s" =[ ${Thread.currentThread().getId} ]=> $d * $d = $p")

          System.out.flush()
        })

    connectableObservable.connect()

    Await.ready(cancelableFuture, Duration.Inf)
  }

}
