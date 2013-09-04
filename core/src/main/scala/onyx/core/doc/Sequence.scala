package onyx.core.doc

import scala.collection.mutable.ArrayBuffer


/**
 * to be implemented
 * @author Kumar Ishan (@kumarishan)
 */
trait SeqLabel
case object Token extends SeqLabel
case object Sentence extends SeqLabel

trait Sequence

/**
 * It store an iterable data structure. Each item in the iterator is of type I.
 * Every implementation of Sequence has their own internal data structure to store
 *
 * @author Kumar Ishan (@kumarishan)
 */
abstract class Sequence0[D <: Sequence0[D, V], V] extends Sequence {

  self: D =>

  def iterator: Iterator[V]
  def +(value: V): D = updated0(value)
  def ++[U <: V](values: Iterator[U]): D = values.foldLeft(this)((s, t) => s + t)

  def +=(value: V): D = {
    this.append(value)
    return this
  }

  def ++=(values: V*): D = {
    this.append(values: _*)
    return this
  }

  def append(values: V*): Unit = values.foreach(t => this.append(t))
  def append(value: V): Unit

  @deprecated("use iterator", "0.1.0")
  def text: Array[String]

  protected def updated0(value: V): D
}

object Sequence0 {}


abstract class ArrayBufferSequence[A <: ArrayBufferSequence[A, V], V] protected (ds: ArrayBuffer[V]) extends Sequence0[A, V]{
  self: A =>

  def this(initSize: Int) = this(new ArrayBuffer[V](initSize))
  def this() = this(ArrayBufferSequence.DEFAULT_INITIALSIZE)

  def iterator = ds.iterator
  def append(value: V): Unit = ds.append(value)
}

object ArrayBufferSequence {
  val DEFAULT_INITIALSIZE = 100
}


abstract class MapSequence[A <: MapSequence[A, K, V], K, V] protected (ds: scala.collection.mutable.HashMap[K, V]) extends Sequence0[A, (K, V)]{
  self: A =>

  def this() = this(new scala.collection.mutable.HashMap[K, V]())

  def iterator = ds.iterator
  def append(value: (K, V)): Unit = ds.put(value._1, value._2)

}

/**
 * TokenSequence for storing word tokens(string)
 *
 * @author Kumar Ishan (@kumarishan)
 */
class TokenSequence private (ds: ArrayBuffer[String]) extends ArrayBufferSequence[TokenSequence, String](ds){
  override protected def updated0(value: String) = new TokenSequence(ds.clone() += value)

  @deprecated("use iterator", "0.1.0")
  def text = ds.toArray
}

/**
 * Sentence Sequence to store sentences (string)
 *
 * @author Kumar Ishan (@kumarishan)
 */
class SentenceSequence private(ds: ArrayBuffer[String]) extends ArrayBufferSequence[SentenceSequence, String]{
  override protected def updated0(value: String) = new SentenceSequence(ds.clone() += value)

  @deprecated("use iterator", "0.1.0")
  def text = ds.toArray
}

/**
 * Content Sequence store the complete content in one string
 *
 * @author Kumar Ishan (@kumarishan)
 */
class ContentSequence (var ds: String) extends Sequence0[ContentSequence, String]{
  def iterator = Array[String](ds).iterator
  def append(value: String): Unit = {ds = ds + value}

  override protected def updated0(value: String) = new ContentSequence(ds + value)

  @deprecated("use iterator", "0.1.0")
  def text = Array[String](ds)
}