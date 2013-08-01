package onyx.processing.pos

import onyx._
import onyx.OnyxTags._

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
trait POSTagger {
  def posTag(s: Array[String] @@ Tokenized): Array[String]
}