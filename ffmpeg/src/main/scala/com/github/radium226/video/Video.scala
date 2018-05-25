package com.github.radium226.video

import java.io.InputStream
import java.nio.file.{Files, Path}

import akka._
import akka.stream._
import akka.stream.scaladsl._

import com.github.radium226.video.stage.MatSourceGraphStage
import org.opencv.core.Mat

object Video {

  def fromInputStream(openInputStream: () => InputStream): Source[Mat, NotUsed] = {
    Source.fromGraph(new MatSourceGraphStage(openInputStream))
  }

  def file(filePath: Path): Source[Mat, NotUsed] = fromInputStream({ () => Files.newInputStream(filePath) })

}
