package com.github.radium226.examples

import akka._
import akka.actor._
import akka.stream._
import akka.stream.scaladsl._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.Duration

object SubFlowExample extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val graph = Source(0 to 100)
    .splitWhen({ index =>
      index > 0 && index % 10 == 0
    })
    .statefulMapConcat[Int]({ () =>
      var previousIndexOption: Option[Int] = None

      { index: Int =>
        val nextIndex = previousIndexOption match {
          case Some(previousIndex) if previousIndex % 2 == 0 =>
            previousIndex
          case _ =>
            None
        }
        previousIndexOption = Some(index)
        List(index, index)
      }
    })
    .map({ index =>
      println(s"index=${index}")
      index
    })
    .fold(0)(_ + _)
    .alsoTo(Sink.foreach(println))
    .splitWhen({ _ => true })
    .mergeSubstreams
    .mergeSubstreams
    .toMat(Sink.fold(Seq.empty[Int])(_ :+ _))(Keep.right)

    val future = graph.run()

    val seq = Await.result(future, Duration.Inf)
    println(seq)

}
