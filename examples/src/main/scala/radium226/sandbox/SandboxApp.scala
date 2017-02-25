package radium226.sandbox

import scala.concurrent.duration.Duration

trait SandboxApp extends App {


  def sleep(duration: Duration): Unit = {
    Thread.sleep(duration.toMillis)
  }

  def compute[I, O](duration: Duration)(block: I => O): I => O = { i =>
    val o = block(i)
    sleep(duration)
    o
  }


}
