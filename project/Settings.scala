import sbt._
import sbt.Keys._

object Settings {

  def commons = Seq(
    organization := "com.github.radium226",
    version := "1.0-SNAPSHOT",

    scalaVersion := "2.12.5",

    resolvers += Resolver.mavenLocal,

    libraryDependencies ++= Dependencies.logback ++ Dependencies.slf4j,
    libraryDependencies ++= Dependencies.guava,
    libraryDependencies ++= Dependencies.scalaTest,
    libraryDependencies ++= Dependencies.squants,
    libraryDependencies ++= Dependencies.java8Compat
  )

}
