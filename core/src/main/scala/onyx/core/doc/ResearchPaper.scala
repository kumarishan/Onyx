package onyx.core.doc

import java.io.InputStream

import scala.collection.JavaConversions._

import Document._
import onyx.core.util._
import onyx.core.parser.ResearchPaperParser

import org.apache.pdfbox.util.TextPosition

/**
 * Research Document Type
 *
 * @author Kumar Ishan (@kumarishan)
 */
class ResearchPaper(fieldTypes: FieldTypes, store: StoreType) extends DefaultDocument[ResearchPaper](fieldTypes, store){
  def this() = this(Map[FieldType, Set[String]](), Map[String, Sequence]())
  override protected def mkInstance(fieldTypes: FieldTypes, store: StoreType): ResearchPaper = new ResearchPaper(fieldTypes, store)
}

object researchPaper {

  def parse[V](implicit ev: V => InputStream): V => ResearchPaper = {
    (source: V) => {
      val parser = new ResearchPaperParser()

      var title = ""
      var author = ""

      val content = parser.parse(source)

      new ResearchPaper +
        ("content" -> new ContentSequence(content)) +
        ("title" -> new ContentSequence(parser.title)) +
        ("abstract" -> new ContentSequence(parser.abs)) +
        ("authors" -> new ContentSequence(parser.author)) ++
        ("authors", Set[FieldType](IncludeInFeature, Store, Indexable)) ++
        ("title", Set[FieldType](Id, IncludeInFeature, Store, Indexable)) ++
        ("content", Set[FieldType](IncludeInFeature, Indexable)) ++
        ("content", Set[FieldType](IncludeInFeature, Indexable))
    }
  }
}