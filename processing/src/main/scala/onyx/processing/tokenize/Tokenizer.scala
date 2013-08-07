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