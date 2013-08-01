package onyx.processing.tokenize

import onyx._
import onyx.OnyxTags._

/**
 *
 * @author Kumar Ishan (@kumarishan)
 */
trait Tokenizer[T] {
  def tokenize(s: T)(implicit tokenizable: T => Tokenizable[T]): Array[Array[String] @@ Tokenized]
}