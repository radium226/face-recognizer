package radium226.sandbox

import java.nio.file.{Files, Paths, StandardOpenOption}

import com.google.common.util.concurrent.RateLimiter
import monix.execution.Scheduler
import monix.execution.Scheduler.Implicits._
import monix.reactive.Observable
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration._

object TryToProcessInParallel {

  val Logger = LoggerFactory.getLogger(getClass)

  def main(arguments: Array[String]): Unit = {
    /*val cancelable = doubles.foreach({ double =>
      println(s"double = ${double}")
    })map

    doubles.connect()

    Await.ready(cancelable, Duration.Inf)*/

    val rateLimiter = RateLimiter.create(1)

    val computationScheduler = Scheduler.computation(3)

    val outputFilePath = Paths.get("/home/adrien/Personal/Projects/video-miner/src/main/resources/output.txt")
    val outputFileWriter = Files.newBufferedWriter(outputFilePath, StandardOpenOption.TRUNCATE_EXISTING)

    val computations = Observable(Seq(("^", powOfTwo), ("*", multiplyByTwo), ("/", divideByTwo)): _*)

    val doubles = RandomDoubleObservable().publish

    val computedDoubles: Observable[(Double, Observable[(String, Double)])] = doubles
      .map({ double: Double =>
        (double, computations.mergeMap({ case (description: String, computation: (Double => Double)) =>
          Observable.defer[(String, Double)]({
            Observable((description, computation(double)))
          }).subscribeOn(computationScheduler)
        }))
      })

    val lines: Observable[String] = computedDoubles
      .flatMap({ case (double: Double, computedDoubles: Observable[(String, Double)]) =>
        computedDoubles
          .map({ case (description, computedDouble) =>
            s"$double $description 2 = $computedDouble"
          })
      })

    val cancelable = lines
      .foreach({ line =>
        rateLimiter.acquire()
        System.out.println(line)
        System.out.flush()
        //outputFileWriter.write(s"${line}\n")
      })

    println("Starting...")
    doubles.connect()
    println("Started! ")

    println("Waiting...")
    Await.ready(cancelable, Duration.Inf)
    println("Done!")

  }

  def compute(duration: Duration)(block: Double => Double): Double => Double = { double =>
    val computedDouble = block(double)
    sleep(duration)
    computedDouble
  }

  def powOfTwo = compute(1 second) { double =>
    Logger.info("Computing pow({}, 2)", double)
    math.pow(double, 2)
  }

  def multiplyByTwo = compute(2 seconds) { double =>
    Logger.info("Computing {} * 2", double)
    double * 2
  }

  def divideByTwo = compute(3 seconds) { double =>
    Logger.info("Computing {} / 2", double)
    double / 2
  }

  def sleep(duration: Duration): Unit = {
    Thread.sleep(duration.toMillis)
  }

}
