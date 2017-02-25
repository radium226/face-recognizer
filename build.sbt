import sbt.Keys.{libraryDependencies, scalaVersion}

name := "video-miner"

lazy val commonDependencies = Seq(
  "com.google.guava" % "guava" % "21.0"
)

lazy val commonSettings = Seq(
  version := "1.0",
  scalaVersion := "2.12.1",
  libraryDependencies ++= commonDependencies
)

lazy val rxDependencies = Seq(
  "io.reactivex" % "rxjava" % "1.2.5",
  "com.github.davidmoten" % "rxjava-extras" % "0.8.0.6"
)

lazy val monixDependencies = Seq(
  "io.monix" %% "monix" % "2.1.2"
)

lazy val loggingDependencies = Seq(
  "ch.qos.logback" % "logback-core" % "1.1.9",
  "ch.qos.logback" % "logback-classic" % "1.1.9",
  "org.slf4j" % "slf4j-api" % "1.7.22"
)

lazy val httpClientDependencies = Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.5.3",
  "org.apache.httpcomponents" % "httpmime" % "4.5.3"
)

lazy val htmlUnitDepdendencies = Seq(
  "net.sourceforge.htmlunit" % "htmlunit" % "2.24"
)

lazy val openCvUnmanagedJars = Seq(
  file("/usr/share/java/opencv.jar")
)

lazy val root = (project in file("."))
  .aggregate(dlib, examples)

lazy val examples = (project in file("examples"))
  .settings(
    name := "examples",
    commonSettings,
    libraryDependencies ++= rxDependencies,
    libraryDependencies ++= monixDependencies,
    libraryDependencies ++= loggingDependencies,
    libraryDependencies ++= httpClientDependencies,
    libraryDependencies ++= htmlUnitDepdendencies,
    unmanagedJars in Compile ++= openCvUnmanagedJars
  )

lazy val dlib = (project in file("dlib"))
  .enablePlugins(JniNative)
  .settings(
    name := "dlib",
    commonSettings,
    target in javah := (sourceDirectory in nativeCompile).value / "include",
    unmanagedJars in Compile ++= openCvUnmanagedJars
  )