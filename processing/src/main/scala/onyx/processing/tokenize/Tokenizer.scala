package onyx.processing.tokenize

import onyx._
import onyx.OnyxTags._

/**
 * Tokenizer interface
 *
 * @author Kumar Ishan (@kumarishan)
 */
abstract class Tokenizer[T <% Tokenizable[T], R] extends (T => Array[R]) with Serializable {

  def apply(s: T): Array[R] = tokenize(s)

  def tokenize(s: T): Array[R]

  def batch_tokenize(s: Array[T])(implicit ev: ClassManifest[Array[R]]): Array[Array[R]] =
    s.map(tokenize(_)).toArray

}

trait Tokenizable[T] {
  def text: String
}

trait Tokenized[T, R] {
  def tokens: Array[R]
}

object implicits {

  implicit def arrayOfString2Tokenized(s: Array[String]) = new Tokenized[Array[String], String] {
    def tokens = s
  }

  implicit def doubleArrOfStr2Tokenized(s: Array[Array[String]]) = new Tokenized[Array[Array[String]], String] {
    def tokens = s.flatten
  }
}