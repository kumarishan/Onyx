package onyx.core.doc

import java.io.{BufferedInputStream, InputStream}
import java.io.{OutputStream, BufferedOutputStream, ByteArrayOutputStream}

import org.apache.tika.metadata.{TikaCoreProperties, Metadata}
import org.apache.tika.parser.{AutoDetectParser, ParseContext, Parser}
import org.apache.tika.sax.BodyContentHandler

import Document._

/**
 * Book Document Type
 *
 * @author Kumar Ishan (@kumarishan)
 */
class Book(fieldTypes: FieldTypes, store: StoreType) extends DefaultDocument[Book](fieldTypes, store){
  def this() = this(Map[FieldType, Set[String]](), Map[String, ContentSequence]())
  override protected def mkInstance(fieldTypes: FieldTypes, store: StoreType): Book = new Book(fieldTypes, store)

  @deprecated("0.1.0", "")
  def content: Array[Byte] = {get("content") match {
    case Some(s) => s.asInstanceOf[ContentSequence].text.map(s => s.toByte)
    case None => Array[Byte]()
  }}

  @deprecated("0.1.0", "")
  var title = ""

  @deprecated("0.1.0", "")
  def authors: String = {get("authors") match {
    case Some(s) => s.asInstanceOf[ContentSequence].text.mkString(" ")
    case None => ""
  }}
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

        new Book +
          ("title" -> new ContentSequence(metadata.get(TikaCoreProperties.TITLE.getName)) ) +
          ("authors" -> new ContentSequence(metadata.get(TikaCoreProperties.CONTRIBUTOR.getName)) ) +
          ("publisher" -> new ContentSequence(metadata.get(TikaCoreProperties.PUBLISHER.getName)) ) +
          ("content" -> new ContentSequence(bas.toString)) ++
          ("title", Set[FieldType](Id, Store, Indexable)) ++
          ("authors", Set[FieldType](IncludeInFeature, Store, Indexable)) ++
          ("publisher", Set[FieldType](IncludeInFeature, Store, Indexable)) ++
          ("content", Set[FieldType](IncludeInFeature, Indexable))
      } catch {
        case e: Exception => {
          new Book
        }
      } finally {
        if(bis != null) bis.close
        if(bas != null) bas.close
        if(bos != null) bos.close
      }
    }
  }
}