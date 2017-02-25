package radium226.sandbox

import monix.reactive.Observable

/**
  * Created by adrien on 2/2/17.
  */
object RandomDoubleObservable {

  val MaxDouble = 1000

  def apply(maxDouble: Double = MaxDouble): Observable[Double] = RandomObservable(_.nextDouble() * maxDouble)

}
