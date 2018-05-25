import sbt._
import sbt.Keys._

object Dependencies {

  def akkaActor = Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.5.12",
    "com.typesafe.akka" %% "akka-testkit" % "2.5.12" % Test
  )

  def akkaStream = Seq(
    "com.typesafe.akka" %% "akka-stream" % "2.5.12",
    "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.12" % Test
  )

  def dlib = Seq(
    "com.github.radium226" % "dlib-java" % "1.0-SNAPSHOT"
  )

  def openCV = Seq(
    "opencv" % "opencv" % "3.4.1" // FIXME It should be org.opencv
  )

  def logback = Seq(
    "ch.qos.logback" % "logback-core" % "1.2.3",
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )

  def slf4j = Seq(
    "org.slf4j" % "slf4j-api" % "1.7.25"
  )

  def guava = Seq(
    "com.google.guava" % "guava" % "25.0-jre"
  )

  def scalaTest = Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % "test"
  )

  def squants = Seq(
    "org.typelevel"  %% "squants"  % "1.3.0"
  )

  def java8Compat = Seq(
    "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0"
  )

}