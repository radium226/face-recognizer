name := "marvin-example"

version := "1.0"
scalaVersion := "2.12.1"

libraryDependencies += "com.google.guava" % "guava" % "21.0"

libraryDependencies += "io.reactivex" % "rxjava" % "1.2.5"
libraryDependencies += "com.github.davidmoten" % "rxjava-extras" % "0.8.0.6"

libraryDependencies += "io.monix" %% "monix" % "2.1.2"

libraryDependencies += "ch.qos.logback" % "logback-core" % "1.1.9"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.9"
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.22"

unmanagedJars in Compile += file("/usr/share/java/opencv.jar")
