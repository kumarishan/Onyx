package onyx.core.doc

import scala.collection.immutable.{Map, Iterable}

import java.io.{BufferedInputStream, InputStream, ByteArrayInputStream}

import org.apache.hadoop.io.BytesWritable

import Document._

/**
 * a container that stores each data segment in an apropriate data structure
 * and also set of property defined for each Sequence.
 * A segment can also optional store the original segment that its derived from.
 *
 * @author Kumar Ishan (@kumarishan)
 * @since 0.1.0
 */
trait Segment {
  type T
  def text: Array[String]
}

/**
 * Field like Include in feature, tokenize, text
 *
 * @author Kumar Ishan (@kumarishan)
 * @since 0.1.0
 */
trait FieldType
case object Text extends FieldType
case object Indexable extends FieldType
case object Id extends FieldType
case object Store extends FieldType
case object IncludeInFeature extends FieldType

/**
 * Document has to be immutable. Its implementation resembles that of immutable HashMap
 *
 * usage example
 * {{{
 *   val tokenizedDoc = doc.get(key) match {
 *     case Some(s) => doc + (key -> tokenize(s))
 *     case None => doc
 *   }
 *  // or
 *  val tokenizedDoc = doc(key, tokenize)
 *  // or
 *  val tokenizedDoc = doc(Text, tokenize)
 *  // or
 *  val tokenizedDoc = doc ++ doc.iterator(fieldType).map(s => s._1 -> tokenize(s._2))
 * }}}
 *
 * @author Kumar Ishan (@kumarishan)
 * @since 0.1.0
 */
abstract private[doc] class Document0[D <: Document0[D]] extends Iterable[(String, Sequence)] with Serializable {

  self: D =>

  override def size: Int = 0
  def apply(key: String, f: (Sequence => Sequence)): D =
    this.get(key) match {
      case Some(s) => this + (key -> f(s))
      case None => this
    }
  def apply(fieldType: FieldType, f: Sequence => Sequence): D = this ++ iterator(fieldType).map(s => s._1 -> f(s._2))
  def get(key: String): Option[Sequence] = None
  def iterator: Iterator[(String, Sequence)] = Iterator.empty
  def iterator(fieldType: FieldType): Iterator[(String, Sequence)] = Iterator.empty
  def +[B <: Sequence](kv: (String, B)): D = updated0(kv, null, null)
  def +[B <: Sequence](kv: (String, B), fieldType: FieldType): D = updated0(kv, null, fieldType)
  def ++[B <: Sequence](ikv: Iterator[(String, Sequence)]): D = ikv.foldLeft(this)((d, s) => d + s)
  def +(key: String, fieldType: FieldType): D = updated0(null, key, fieldType)
  def ++(key: String, fieldTypes: Set[FieldType]): D = fieldTypes.foldLeft(this)((d, f) => d.updated0(null, key, f))

  def -(key: String) = removed0(key, null)
  def -(key: String, fieldType: FieldType) = removed0(key, fieldType)

  protected def mkInstance(fieldTypes: FieldTypes, store: StoreType): D
  protected def removed0(key: String, fieldType: FieldType): D
  protected def updated0[B <: Sequence](kv: (String, B), key: String, fieldType: FieldType): D

}

/**
 * Implements everything except mkInstance
 *
 * @author Kumar Ishan (@kumarishan)
 * @since 0.1.0
 */
abstract class DefaultDocument[D <: Document0[D]](
  fieldTypes: FieldTypes,
  store: StoreType
) extends Document0[D] {

  self: D =>

  override def size: Int = store.size
  override def get(key: String): Option[Sequence] = store.get(key)
  override def iterator: Iterator[(String, Sequence)] = store.iterator
  override def iterator(fieldType: FieldType) = store.iterator

  protected override def updated0[B <: Sequence](kv: (String, B), key: String, fieldType: FieldType): D = {
    if(kv != null){
      val _store = store + kv
      if(fieldType != null){
        val _fieldTypes = {fieldTypes.get(fieldType) match {
          case Some(s) => fieldTypes + (fieldType -> (s + key))
          case None => fieldTypes + (fieldType -> Set[String](key))
        }}: FieldTypes
        mkInstance(_fieldTypes, _store)
      }else mkInstance(fieldTypes, _store)
    } else if(key != null && fieldType != null){
      val _fieldTypes = fieldTypes + (fieldType -> Set[String](key))
      mkInstance(_fieldTypes, store)
    } else this
  }

  protected override def removed0(key: String, fieldType: FieldType): D = {
    if(key != null){
      val _store = store - key
      val _fieldTypes = fieldTypes.map({s: (FieldType, Set[String]) => s._1 -> s._2.filter({x => key.compareTo(x) < 0})})
      mkInstance(_fieldTypes, _store)
    } else if(fieldType != null) {
      val _fieldTypes = fieldTypes - fieldType
      mkInstance(_fieldTypes, store)
    } else this
  }

}

/**
 * @author Kumar Ishan (@kumarishan)
 * @since 0.1.0
 */
class Document extends DefaultDocument[Document](Map[FieldType, Set[String]](), Map[String, Sequence]()) {
  protected def mkInstance(fieldTypes: FieldTypes, store: StoreType): Document =
    new Document
}

/**
 *
 * @author Kumar Ishan (@kumarishan)
 * @since 0.1.0
 */
object Document {

  type FieldTypes = Map[FieldType, Set[String]]
  type StoreType = Map[String, Sequence]

  def apply() = new Document

  // def empty: Document = new Document[Document]

  // implicit def iterator2Document(s: Iterator[(String, Sequence)]) =
  //   s.foldLeft(empty)((d, s) => d + s)

}

@deprecated("use Document", "0.1.0")
trait Doc

trait HadoopTypeImplicits {

  implicit def text2InputStream(s: org.apache.hadoop.io.Text): InputStream =
    new ByteArrayInputStream(s.getBytes)

  implicit def bytesWritable2InputStream(s: BytesWritable) =
    new ByteArrayInputStream(s.getBytes)

}
