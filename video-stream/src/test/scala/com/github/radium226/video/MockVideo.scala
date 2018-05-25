package com.github.radium226.video

import squants.time.Time
import java.io.InputStream
import ProcessBuilder.Redirect
import java.nio.file.{Path, Paths}

import com.github.radium226.logging.Logging

object MockVideo extends Logging {

  def openInputStream(duration: Time, imagePath: Path = Paths.get("src/test/resources/lena.jpg")): InputStream = {
    val command = Seq(
      "ffmpeg",
      "-loop", "1",
      "-i", "/home/adrien/Personal/Projects/dlib-java/src/main/resources/lena.jpg",
      "-c:v", "libvpx",
      "-t", duration.toSeconds.toInt.toString,
      "-pix_fmt", "yuv420p",
      "-vf", "scale=320:240",
      "-f", "matroska",
      "-"
    )
    info("Starting {}", command.mkString(" "))
    val process = new ProcessBuilder()
        .redirectError(Redirect.INHERIT)
        .command(command:_*)
      .start()

    process.getInputStream
  }



}
