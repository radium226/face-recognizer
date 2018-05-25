package com.github.radium226.scalatest

import com.github.radium226.logging.Logging
import org.scalatest.FlatSpec

import scala.concurrent.duration.Duration

abstract class AbstractSpec extends FlatSpec with Logging {

  def sleep(duration: Duration): Unit = {
    Thread.sleep(duration.toMillis)
  }

}
