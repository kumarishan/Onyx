package onyx.core.doc

import java.io.{BufferedInputStream, InputStream}
import java.io.{OutputStream, BufferedOutputStream, ByteArrayOutputStream}

import org.apache.tika.metadata.{TikaCoreProperties, Metadata}
import org.apache.tika.parser.{AutoDetectParser, ParseContext, Parser}
import org.apache.tika.sax.BodyContentHandler

class Book extends Doc with Serializable {
  var authors = Array[String]()
  var title: String = ""
  var publisher: String = ""

  var content = Array[Byte]()

  var error: Boolean = false
  var errorMessage: String = ""
  override def toString: String = title + " " + authors.mkString(" ") + " " + publisher + " " + content.size
}

object book {

  def parse[V](implicit ev: V => InputStream): V => Book = {

    (source: V) => {
      var bis: InputStream = null
      var bas: ByteArrayOutputStream = null
      var bos: OutputStream = null

      try {

        bis = new BufferedInputStream(source)
        bas = new ByteArrayOutputStream()
        bos = new BufferedOutputStream(bas)

        val parser = new AutoDetectParser
        val handler = new BodyContentHandler(bos)

        val metadata = new Metadata

        parser.parse(bis, handler, metadata, new ParseContext())

        val b = new Book
        b.title = metadata.get(TikaCoreProperties.TITLE.getName)
        b.authors = Array[String](metadata.get(TikaCoreProperties.CONTRIBUTOR.getName))
        b.publisher = metadata.get(TikaCoreProperties.PUBLISHER.getName)
        b.content = bas.toByteArray

        b
      } catch {
        case e: Exception => {
          val b = new Book
          b.error = true
          b.errorMessage = e.getMessage
          b
        }
      } finally {
        if(bis != null) bis.close
        if(bas != null) bas.close
        if(bos != null) bos.close
      }
    }
  }
}