package onyx.processing.pos 

import spark.RDD

import onyx._
import OnyxTags._

import edu.stanford.nlp.tagger.maxent.MaxentTagger

/**
 *
 * @todo type abstraction
 *
 * @author Kumar Ishan (@kumarishan)
 */
private[pos] trait MaxEntPOSTagger extends POSTagger {
  val tagger = new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger") with Serializable

  def posTag(s: Array[String] @@ Tokenized): Array[String] = {
    tagger.tagTokenizedString(s.mkString(" ")).split(" ").toArray
  }
}

class SimpleMaxEntPOSTagger[K : ClassManifest]
  extends (RDD[(K, Array[String] @@ Tokenized)] => RDD[(K, Array[String] @@ Tokenized)])
  with MaxEntPOSTagger with Serializable {

  def apply(source: RDD[(K, Array[String] @@ Tokenized)]) = {
    source.map(s => {
      s._1 -> Tag[Array[String], Tokenized](posTag(s._2))
    })
  }
}

class BatchMaxEntPOSTagger[K : ClassManifest]
  extends (RDD[(K, Array[Array[String] @@ Tokenized])] => RDD[(K, Array[Array[String] @@ Tokenized])])
  with MaxEntPOSTagger with Serializable {

  def apply(source: RDD[(K, Array[Array[String] @@ Tokenized])]) = {
    source.map(s => {
      s._1 -> s._2.map(t => 
        Tag[Array[String], Tokenized](posTag(t))
      ).toArray(manifest[Array[String] @@ Tokenized])
    })
  }
}