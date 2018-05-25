lazy val ffmpeg = (project in file("ffmpeg"))
  .settings(Settings.commons)
  .settings(name := "ffmpeg")
  .settings(libraryDependencies ++= Dependencies.akkaActor)
  .settings(libraryDependencies ++= Dependencies.akkaStream)
  .settings(libraryDependencies ++= Dependencies.openCV)
  .settings(libraryDependencies ++= Dependencies.dlib) // FIXME We should put the Libraries class in a commons project

lazy val examples = (project in file("examples"))
  .settings(Settings.commons)
  .settings(name := "examples")
  .dependsOn(ffmpeg)

lazy val root = (project in file("."))
  .settings(Settings.commons)
  .settings(name := "video-miner")
  .aggregate(ffmpeg, examples)
