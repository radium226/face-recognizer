lazy val videoStream = (project in file("video-stream"))
  .settings(Settings.commons)
  .settings(name := "video-stream")
  .settings(libraryDependencies ++= Dependencies.akkaActor)
  .settings(libraryDependencies ++= Dependencies.akkaStream)
  .settings(libraryDependencies ++= Dependencies.openCV)
  .settings(libraryDependencies ++= Dependencies.dlib) // FIXME We should put the Libraries class in a commons project
  .settings(libraryDependencies ++= Dependencies.nuProcess)
  .settings(libraryDependencies ++= Dependencies.breeze)

lazy val root = (project in file("."))
  .settings(Settings.commons)
  .settings(name := "video-miner")
  .aggregate(videoStream)
