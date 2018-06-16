package com.github.radium226.scalatest

import com.github.radium226.logging.Logging
import org.scalatest.{BeforeAndAfterAll, FlatSpec}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

abstract class AbstractSpec extends FlatSpec with Logging with BeforeAndAfterAll {

  def sleep(duration: Duration): Unit = {
    Thread.sleep(duration.toMillis)
  }

  def await(future: Future[_]): Unit = {
    Await.result(future, Duration.Inf)
  }

  def await(duration: Duration): Unit = {
    Thread.sleep(duration.toMillis)
  }

}
