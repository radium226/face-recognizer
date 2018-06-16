package com.github.radium226.algorithms

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import breeze.linalg.DenseVector
import com.github.radium226.opencv.OpenCV
import com.github.radium226.scalatest.AbstractSpec

import scala.collection.immutable.Queue
import scala.util.Random

import scala.concurrent.duration._

class InterpolateSpec extends AbstractSpec {

  implicit var system: ActorSystem = _
  implicit var materializer: ActorMaterializer = _

  override def beforeAll(): Unit = {
    super.beforeAll()

    info("Starting system and materializer... ")
    system = ActorSystem()
    materializer = ActorMaterializer()
  }

  behavior of "Interpolate"

  object Interpolator {

    val PresentElementsMinimalCount = 2
    val AbsentElementsMaximalCount = 5

    def interpolate[T:CanBeInterpolated]: Flow[Option[T], Option[T], NotUsed] = {
      Flow[Option[T]]
        .statefulMapConcat[Option[T]]({ () =>
          var ring: Ring[T] = Ring.empty[T](RingConfig(PresentElementsMinimalCount, AbsentElementsMaximalCount))

          { element: Option[T] =>
            val (newRing, elementsToFlush) = ring.append(element)
            ring = newRing
            elementsToFlush
          }
        })

    }

  }

  it should "interpolate missing data" in {
    val tick = Source.tick(1 second, 0.250 second, 'tick)
    val doubles = Source.fromIterator({ () => Iterator.iterate(Random.nextDouble())({ _ => Random.nextDouble() })})

    doubles.zip(tick)
      .map({ case (double, _) => double })
      .map({ double =>
        if (Random.nextBoolean()) Some(double)
        else None
      })
      .via(Interpolator.interpolate)
      .runForeach(println)

    await(1 minute)

  }

}
