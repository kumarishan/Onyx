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
private[tokenize] trait PTBTokenizer[V] extends Tokenizer[V] {

  def tokenize(s: V)(implicit tokenizable: V => Tokenizable[V]) = {
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
      Tag[Array[String], Tokenized](words.toArray)
    }).toArray(manifest[Array[String] @@ Tokenized])
  } 
}

class BatchPTBTokenizer[K, V](
  implicit m: ClassManifest[K], 
  n: ClassManifest[V], 
  tokenizable: V => Tokenizable[V]
) extends (RDD[(K, V)] => RDD[(K, Array[Array[String] @@ Tokenized])])
  with PTBTokenizer[V] with Serializable {
  
  def apply(source: RDD[(K, V)]) = {
    source.map(doc => {
      doc._1 -> tokenize(doc._2)
    })
  }
}

class SimplePTBTokenizer[K, V](
  implicit m: ClassManifest[K],
  n: ClassManifest[V],
  tokenizable: V => Tokenizable[V]
) extends (RDD[(K, V)] => RDD[(K, Array[String] @@ Tokenized)])
  with PTBTokenizer[V] with Serializable {

  def apply(source: RDD[(K, V)]) = {
    source.flatMap(doc => {
      val tokenized = tokenize(doc._2).map(t => doc._1 -> t)
      println(tokenized)
      tokenized
      // doc._1 -> tokenize(doc._2)
    })
  }
}

// object PTBTokenizer {

//   def apply[K, V](
//     implicit m: ClassManifest[K],
//     n: ClassManifest[V],
//     tokenizable: T => Tokenizable[T]
//   ) = 
//     new BatchPTBTokenizer[K, V]

//   def apply[K, V @@ Simple](
//     implicit m: ClassManifest[K],
//     n: ClassManifest[V],
//     tokenizable: T => Tokenizable[T]
//   ) = 
//     new SimplePTBTokenizer[K, V]

// }