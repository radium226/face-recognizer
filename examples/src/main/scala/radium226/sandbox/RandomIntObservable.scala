package radium226.sandbox

object RandomIntObservable {

  val MaxInt = 1000

  def apply(maxInt: Int = MaxInt) = RandomObservable(_.nextInt(maxInt))

}
