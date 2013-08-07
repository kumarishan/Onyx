package onyx.processing.pos

import onyx.processing.tokenize._

import edu.stanford.nlp.tagger.maxent.MaxentTagger

/**
 *
 * @todo type abstraction
 *
 * @author Kumar Ishan (@kumarishan)
 */
class MaxEntPOSTagger[T <% Tokenized[T, String]] extends POSTagger[T, String] {
  val tagger = new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger") with Serializable

  def posTag(s: T): Array[String] = {
    tagger.tagTokenizedString(s.tokens.mkString(" ")).split(" ").toArray
  }
}