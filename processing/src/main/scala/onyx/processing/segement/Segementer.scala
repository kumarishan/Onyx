package onyx.processing.segement

import onyx.processing.tokenize.Tokenizable

trait Segementer[D] {}

object Segementer {
  type Line = String
}

import Segementer._

/**
 * SimpleLineSegmenter is a uses character set to identify sentences. It currently only supports
 * English language.
 *
 * @author Kumar Ishan (@kumarishan)
 */
class PTBSentenceSegmenter[D <% Tokenizable[D]] extends (D => Array[Line]) with Segementer[D] {
  def apply(doc: D) = {
    doc.text.split("\n")
  }
}