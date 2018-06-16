package com.github.radium226.algorithms

import com.github.radium226.scalatest.AbstractSpec

import scala.util.Random

trait CanBeInterpolated[T] {

  def interpolate(elements: List[Option[T]]): List[T]

}

object CanBeInterpolated {

  implicit val doubleCanBeInterpolated = new CanBeInterpolated[Double] {

    override def interpolate(elements: List[Option[Double]]): List[Double] = {
      import breeze.linalg._
      import breeze.interpolation._
      val e = elements
        .zipWithIndex
        .collect({
          case (Some(y), x) =>
            (x.toDouble, y.toDouble)
        })

      val x = DenseVector(e.map({ case (x, y) => x }): _*)
      val y = DenseVector(e.map({ case (x, y) => y }): _*)

      val f = LinearInterpolator(x, y)

      elements
        .zipWithIndex
        .map({
          case (Some(y), x) =>
            y

          case (None, x) =>
            f(x)
        })
    }

  }

  def interpolate[T:CanBeInterpolated](elements: List[Option[T]]): List[T] = {
    implicitly[CanBeInterpolated[T]].interpolate(elements)
  }

}

import CanBeInterpolated._

sealed trait Action

case object InterpolateElements extends Action

case class FlushElements[T](element: List[Option[T]]) extends Action

case object AppendAndReturnElement extends Action

case object AppendButDoNotReturnElement extends Action

sealed trait Status

object Status {

  def of[T](elements: List[Option[T]]): Status = {
    elements.headOption match {
      case Some(Some(_)) =>
        PresentElements

      case Some(None) =>
        AbsentElements

      case None =>
        Empty
    }
  }

}

case object PresentElements extends Status

case object AbsentElements extends Status

case object Empty extends Status

case class RingConfig(presentElementsMinimalCount: Int, absentElementsMaximalCount: Int)

object Ring {

  def empty[T:CanBeInterpolated](config: RingConfig): Ring[T] = {
    new Ring(List.empty, config)
  }

  def splitElements[T](elements: List[Option[T]]): List[(List[Option[T]], Status)] = {
    val previousElements: List[Option[Option[T]]] = List[Option[Option[T]]](None) ++ elements.dropRight(1).map({ element => Some(element) })
    val splitIndex = elements.zip(previousElements)
      .map(_.swap)
      //.map({ t => // print(t) ; t})
      .indexWhere({
        // First element
        case (None, _) =>
          false

        case (Some(previousElement), element) =>
          previousElement.isDefined != element.isDefined
      })

    if (splitIndex < 0) {
      List((elements, Status.of(elements)))
    }
    else {
      val (firstElements, remainingElements) = elements.splitAt(splitIndex)
      (firstElements, Status.of(firstElements)) +: splitElements(remainingElements)
    }
  }

}

class Ring[T:CanBeInterpolated](val elements: List[Option[T]], config: RingConfig) {

  def maxSize: Int = {
    config.presentElementsMinimalCount * 2 + config.absentElementsMaximalCount
  }

  def size: Int = {
    val size = elements.size
    assert(size <= maxSize)
    size
  }

  def append(element: Option[T]): (Ring[T], List[Option[T]]) = {
    val ring = if (size == maxSize) new Ring(elements.drop(1) :+ element, config) else new Ring(elements :+ element, config)
    ring.act()
  }

  def splittedElements: List[(List[Option[T]], Status)] = {
    Ring.splitElements(elements)
  }

  def act(): (Ring[T], List[Option[T]]) = {
    splittedElements match {
      case (beforePresentElements, PresentElements) :: (absentElements, AbsentElements) :: (afterPresentElements, PresentElements) :: Nil if beforePresentElements.size >= config.presentElementsMinimalCount && absentElements.size <= config.absentElementsMaximalCount && afterPresentElements.size >= config.presentElementsMinimalCount =>
        // print("A")
        (new Ring[T](afterPresentElements, config), CanBeInterpolated.interpolate(beforePresentElements ++ absentElements ++ afterPresentElements).map({ value => Some(value) }).drop(beforePresentElements.size))

      case (beforePresentElements, PresentElements) :: (absentElements, AbsentElements) :: (afterPresentElements, PresentElements) :: Nil if beforePresentElements.size >= config.presentElementsMinimalCount && absentElements.size <= config.absentElementsMaximalCount && afterPresentElements.size < config.presentElementsMinimalCount =>
        // print("B")
        (this, List.empty)

      case (beforePresentElements, PresentElements) :: (absentElements, AbsentElements) :: (afterPresentElements, PresentElements) :: Nil if beforePresentElements.size < config.presentElementsMinimalCount =>
        // print("C")
        (new Ring[T](afterPresentElements, config), absentElements ++ afterPresentElements)

      case (beforePresentElements, PresentElements) :: (absentElements, AbsentElements) :: Nil if absentElements.size < config.absentElementsMaximalCount =>
        // print("D")
        (new Ring[T](beforePresentElements ++ absentElements, config), List.empty)

      case (_, PresentElements) :: (absentElements, AbsentElements) :: Nil if absentElements.size == config.absentElementsMaximalCount =>
        // print("E")
        (Ring.empty(config), absentElements)

      case (_, PresentElements) :: (absentElements, AbsentElements) :: Nil if absentElements.size > config.absentElementsMaximalCount =>
        // print("F")
        (new Ring[T](absentElements, config), List(None))

      case (absentElements, AbsentElements) :: Nil if absentElements.size < config.absentElementsMaximalCount =>
        // print("G")
        (this, List.empty)

      case (absentElements, AbsentElements) :: Nil if absentElements.size == config.absentElementsMaximalCount =>
        // print("H")
        (Ring.empty(config), absentElements)

      case (_, AbsentElements) :: (afterPresentElements, PresentElements) :: Nil =>
        // print("I")
        (new Ring[T](afterPresentElements, config), elements)

      case (beforePresentElements, PresentElements) :: Nil =>
        // print("J")
        (this, List(beforePresentElements.last))

      case (p1, PresentElements) :: (a1, AbsentElements) :: (p2, PresentElements) :: (a2, AbsentElements) :: Nil =>
        // print("K")
        (new Ring[T](a2, config), a1 ++ p2)

      case (a1, AbsentElements) :: (p1, PresentElements) :: (a2, AbsentElements) :: (p2, PresentElements) :: Nil =>
        // print("L")
        (new Ring[T](p2, config), a1 ++ p1 ++ a2 ++ p2)

      case (noElements, Empty) :: Nil =>
        // print("M")
        (this, List.empty)

    }
  }

}

class RingSpec extends AbstractSpec {

  behavior of "Ring"

  it should "split elements correctly" in {
    val elements = List(Some(1), None, Some(2))
    // print(Ring.splitElements(elements))
  }

  it should "work fine" in {
    val ringConfig = RingConfig(2, 5)

    val elements = (1 to 100)
      .map(_.toDouble)
      .map({ d =>
        if (Random.nextBoolean()) Some(d)
        else None
      })

    val interpolatedElements = elements
      .foldLeft((Ring.empty[Double](ringConfig), List.empty[Option[Double]]))({ case ((oldRing, oldElements), element) =>
        val (newRing, newElements) = oldRing.append(element)
        // print(" ==> " + newElements.size)
        (newRing, oldElements ++ newElements)
      })._2

    elements.zip(interpolatedElements).foreach({ case (e1, e2) =>
      // print(s"${e1} <-> ${e2}")
    })
    //})


  }

}
