package onyx.processing.tokenize

import java.util.Properties

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._

import spark.RDD

import edu.stanford.nlp.process.{CoreLabelTokenFactory}
import edu.stanford.nlp.pipeline.{StanfordCoreNLP, Annotation}
import edu.stanford.nlp.ling.CoreAnnotations._

import onyx._
import onyx.OnyxTags._

/**
 * trait PTBTokenizer currently uses Stanford CoreNLP with PTBTokenizer
 * will be replaced soon either with scalanlp/chalk or a native implementation
 * hence the code is subject to major change....
 *
 * not using chalk because of scala 2.9.3 compatibility issues
 *
 * @author Kumar Ishan (@kumarishan)
 */
class PTBTokenizer[V <% Tokenizable[V]] extends Tokenizer[V, String] {

  def tokenize(s: V) = {
    val sent = new PTBSentenceTokenizer[V]
    sent.tokenize(s).flatten
  }
}

class PTBSentenceTokenizer[V <% Tokenizable[V]] extends Tokenizer[V, Array[String]] {

  def tokenize(s: V) = {
    val props = new Properties
    props.put("annotators", "tokenize ssplit")
    val tokenizer = new StanfordCoreNLP(props)

    val text = s.text
    val annotation = new Annotation(text)
    tokenizer.annotate(annotation)

    annotation.get(classOf[SentencesAnnotation]).map(s => {
      val words = new ArrayBuffer[String]
      for(t <- s.get(classOf[TokensAnnotation])){
        words.add(t.word)
      }
      words.toArray
    }).toArray
  }
}