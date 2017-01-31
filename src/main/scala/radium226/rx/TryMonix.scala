package radium226.rx

import monix.reactive.Observable

import scala.concurrent.Await

/**
  * Created by adrien on 1/31/17.
  */
object TryMonix {

  def main(arguments: Array[String]): Unit = {
    Await.result(Observable(3, 1, 2, 0, 5).toListL)
  }

}
