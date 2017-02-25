package radium226.sandbox

import java.io.File
import java.nio.file.{Files, Paths}

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import org.apache.http.client.{HttpClient, RedirectStrategy}
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.impl.client.{HttpClientBuilder, HttpClients, LaxRedirectStrategy}
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.{FileBody, InputStreamBody, StringBody}
import org.apache.http.util.EntityUtils

import scala.collection.JavaConverters._

// https://h4hammad.wordpress.com/2013/10/07/google-reverse-image-upload-and-search-using-java-desktop/

object TryGoogleImage {

  def main(arrguments: Array[String]): Unit = {
    val url = uploadImage()
    val text = scrape(url)
    val pattern = "Hypothèse la plus probable pour cette image : (.+)$".r("name")
    val name = text.split("\n")
        .map({ line => pattern.findFirstMatchIn(line) })
        .collectFirst({ case Some(matchData) => matchData.group("name") })

    println(name)
  }

  def uploadImage(): String = {
    val url = "https://www.google.com/searchbyimage/upload"

    val httpClient = HttpClientBuilder.create().build()
    val httpRequest = new HttpPost(url)
    val entity = MultipartEntityBuilder.create()
      .addPart("image_url", new StringBody("", ContentType.TEXT_PLAIN))
      .addPart("encoded_image", new FileBody(new File("/home/adrien/Personal/Projects/video-miner/src/main/resources/john_doe.jpg"), ContentType.create("image/jpeg"), "unknown.jpg"))
      .addPart("image_content", new StringBody("", ContentType.TEXT_PLAIN))
      .addPart("filename", new StringBody("", ContentType.TEXT_PLAIN))
      .addPart("h1", new StringBody("en", ContentType.TEXT_PLAIN))
      .build()
    httpRequest.setEntity(entity)

    val callbackURL = httpClient.execute(httpRequest, { httpResponse =>
      val entity = httpResponse.getHeaders("Location")(0).getValue
      entity
    })

    callbackURL
  }

  def scrape(url: String): String = {
    val webClient = new WebClient()
    val htmlPage: HtmlPage = webClient.getPage(url)
    val text = htmlPage.asText()
    webClient.close()
    text
  }

}
