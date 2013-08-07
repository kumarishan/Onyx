package onyx.processing.pos

import onyx._

/**
 * trait POSTagger
 * parameter to posTag needs to be tokenized first hence its tagged by Tokenized
 *
 * @todo look for type abstracting Array[String] once there is a better type for
 * returning tokens from tokenizer instead of Array[String]
 *
 * @todo also instead of just returning Array[String] need to have a bit more
 * complex yet memory efficient way to store the tags and original tokens
 *
 * @author Kumar Ishan (@kumarishan)
 */
trait POSTagger[T, R] extends (T => Array[R]) with Serializable {
  def apply(s: T) = posTag(s)

  def posTag(s: T): Array[R]
  def batch_posTage(s: Array[T])(implicit ev: ClassManifest[Array[R]]): Array[Array[R]] =
    s.map(posTag(_)).toArray
}