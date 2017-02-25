package radium226.sandbox

import com.google.common.util.concurrent.RateLimiter
import monix.eval.Task

import scala.concurrent.duration._
import scala.concurrent.duration.Duration.{Inf => Forever}
import scala.math._
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import scala.collection.mutable.{ Map => MutableMap }
import scala.collection.immutable.Seq

// Computation Status
abstract class Status[I, O]

case class Idle[I, O]() extends Status[I, O]

case class InProgress[I, O](key: Int) extends Status[I, O]

case class Done[I, O](key: Int, value: O) extends Status[I, O]

//case class Failed[I, O](key: Int, exception: Throwable) extends Status[I, O]

//case class Succeed[I, O](key: Int, output: O) extends Status[I, O]

object ComputationObservable {

  def apply[I, O](task: Task[O]): Observable[Status[I, O]] = {
    Observable.fromIterable(new Iterable[Status[I, O]] {

      def iterator: Iterator[Status[I, O]] = {
        val future: Future[O] = task.runAsync
        new Iterator[Status[I, O]] {

          def hasNext: Boolean = true

          def next: Status[I, O] = future.value match {
            //case None => InProgress()
            //case Some(Success(o)) => Succeed(o)
            //case Some(Failure(e)) => Failed(e)
            case _ => null
          }

        }
      }

    })

  }

}
/*object Invoker {

  val futuresByID = MutableMap()

  def invoke[I, O](func: I => Future[O]): Status[I, O] => Status[I, O] = {

  }

}*/

object TryToInvokeService extends SandboxApp {

  type StatusKey = Int

  abstract class Status[I, O]

  case class Idle[I, O]() extends Status[I, O]

  case class InProgress[I, O](key: StatusKey) extends Status[I, O]

  case class Done[I, O](value: O) extends Status[I, O]

  type EvensAndOdds = (Seq[Int], Seq[Int]) // Even = Pair / Odd = Impair

  type GroupSum = Int

  type GroupSumStatus = Status[Group, GroupSum]

  type Group = Seq[Int]

  var futuresByStatusKey = MutableMap[StatusKey, Future[GroupSum]]()

  def storeFuture(future: Future[GroupSum]): StatusKey = {
    val statusKey = futuresByStatusKey.size
    futuresByStatusKey += (statusKey -> future)
    statusKey
  }

  def sumGroup(group: Group): Future[GroupSum] = {
    val task = Task {
      sleep(5 seconds)
      group.sum
    }
    task.runAsync
  }

  def updateGroupSumStatus(group: Group, oldGroupSumStatus: GroupSumStatus): GroupSumStatus = oldGroupSumStatus match {
    case Idle() => {
      if (group.size >= 5) {
        val future = sumGroup(group)
        val statusKey = storeFuture(future)
        InProgress(statusKey)
      } else Idle()
    }
    case InProgress(statusKey) => {
      val future = futuresByStatusKey.get(statusKey).get
      if (future.isCompleted) Done(future.value.get.get) else InProgress(statusKey) // Bad bad bad
    }
    case Done(value) => Idle()
  }

  def powOfTwo(double: Double): Task[Double] = Task {
    sleep(5 seconds)
    pow(double, 2)
  }

  val rateLimiter = RateLimiter.create(1)

  val randomIntObservable = RandomIntObservable(100).publish

  val evensAndOdds = randomIntObservable
    .scan[(EvensAndOdds, Int)](((Seq.empty[Int], Seq.empty[Int]), 0))({ case (((evenInts: Seq[Int], oddInts: Seq[Int]), _), int: Int) =>
      if (int % 2 == 0) ((evenInts :+ int, oddInts), int) else ((evenInts, oddInts :+ int), int)
    })

  val evensAndOddWithCounts = evensAndOdds
    .scan[(((Group, GroupSumStatus), (Group, GroupSumStatus)), Int)]((((Seq.empty[Int], Idle[Seq[Int], Int]), (Seq.empty[Int], Idle[Seq[Int], Int])), 0))({ case ((((_, oldEvenGroupSumStatus), (_, oldOddGroupSumStatus)), _), ((evenGroup, oddGroup), int)) =>

    val newEvenGroupSumStatus = updateGroupSumStatus(evenGroup, oldEvenGroupSumStatus)
    val newOddGroupSumStatus = updateGroupSumStatus(oddGroup, oldOddGroupSumStatus)

      (((evenGroup, newEvenGroupSumStatus), (oddGroup, newOddGroupSumStatus)), int)
    })
  val cancelableFuture = evensAndOddWithCounts
    .foreach({ o =>
      rateLimiter.acquire()
      println(o)
    })

  randomIntObservable.connect()

  Await.ready(cancelableFuture, Forever)

}
