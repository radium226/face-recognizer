package radium226

import java.io.InputStream

import scala.util.{ Try, Success }

/**
  * Created by adrien on 2/1/17.
  */
trait YoutubeDL extends VideoOpener {

  val url: String

  def openVideo(): Try[InputStream] = {
    val youtubeDLProcessBuilder = new ProcessBuilder("youtube-dl", url, "-o", "-");
    youtubeDLProcessBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT)
    youtubeDLProcessBuilder.redirectError(ProcessBuilder.Redirect.INHERIT)
    val youtubeDLProcess = youtubeDLProcessBuilder.start
    return Success(youtubeDLProcess.getInputStream)
  }

}
