package onyx.core.doc

import java.io.{BufferedInputStream, InputStream}
import org.xml.sax.ContentHandler

import org.apache.tika.metadata.{TikaCoreProperties, Metadata, HttpHeaders}
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.html.{HtmlParser, BoilerpipeContentHandler}
import org.apache.tika.sax.{LinkContentHandler, ToHTMLContentHandler, TeeContentHandler}

class Webpage extends Doc with Serializable {
  var title: Option[String] = None
  var createdDate: Option[String] = None
  var lastModifiedDate: Option[String] = None
  var language: Option[String] = None
  var location: Option[String] = None
  var contentType: Option[String] = None
  var contentLength: Option[String] = None

  var extractedMainContent: Option[String] = None
  var sourceHtml: Option[String] = None

  var error: Boolean = false
  var errorMessage: String = ""
}

object webpage {

  def parse[V](extract_main_content: Boolean = true, keep_source_html: Boolean = true)
    (implicit ev: V => InputStream): V => Webpage = {

    (source: V) => {
      var bis: InputStream = null

      try {

        bis = new BufferedInputStream(source)

        val linkHandler = new LinkContentHandler
        val handlers = List(linkHandler)

        val toHTMLHandler = new ToHTMLContentHandler

        if(keep_source_html) toHTMLHandler :: handlers
        var handler: ContentHandler = new TeeContentHandler(handlers: _*)

        var boilerpipeHandler: ContentHandler = null
        if(extract_main_content) {
          boilerpipeHandler = new BoilerpipeContentHandler(handler)
          handler = boilerpipeHandler
        }

        val parseContext = new ParseContext
        val parser = new HtmlParser

        val metadata = new Metadata

        parser.parse(bis, handler, metadata, parseContext)

        val w = new Webpage
        w.title = Some(metadata.get(TikaCoreProperties.TITLE.getName))
        w.createdDate = Some(metadata.get(TikaCoreProperties.CREATED.getName))
        w.lastModifiedDate = Some(metadata.get(HttpHeaders.LAST_MODIFIED.getName))
        w.language = Some(metadata.get(HttpHeaders.CONTENT_LANGUAGE))
        w.location = Some(metadata.get(HttpHeaders.CONTENT_LOCATION))
        w.contentType = Some(metadata.get(HttpHeaders.CONTENT_TYPE))
        w.contentLength = Some(metadata.get(HttpHeaders.CONTENT_LENGTH))

        w.sourceHtml = Some(toHTMLHandler.toString)
        w.extractedMainContent = Some(boilerpipeHandler.toString)

        w
      } catch {
        case e: Exception => {
          val w = new Webpage
          w.error = true
          w.errorMessage = e.getMessage
          w
        }
      } finally {
        if(bis != null) bis.close
      }
    }
  }
}