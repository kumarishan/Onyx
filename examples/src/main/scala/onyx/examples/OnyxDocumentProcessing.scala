package onyx.examples

import onyx.processing._
import onyx._
import OnyxTags._
import tokenize._
import pos._
import featurize._

import spark._
import spark.SparkContext
import SparkContext._

import org.apache.hadoop.io.Text

object OnyxDocumentProcessing {

  implicit def hadoopText2Tokenizable(h: Text) = new Tokenizable[Text]{
    def text = h.toString
  }

  def main(args: Array[String]){
    val sc = new SparkContext("local", "onyx-processing")
    val source = sc.sequenceFile[Text, Text]("hdfs://localhost:54310/usr/hduser/doc-processing-ex.seq")
    
    val s = source.map(t => t._1.toString -> t._2)
    // println(source.map(t => t._1.toString -> t._2.toString).groupByKey().count()) // number of documents in sequence file

    // Testing simple tokenize then pos tag workflow
    val tokenizer = new SimplePTBTokenizer[String, Text]
    val postagger = new SimpleMaxEntPOSTagger[String]
    val hashing = new SimpleFeatureHashing[String](1000)

    val tokenized = tokenizer(s).cache()
    // println(tokenized.map(a => a._1.toString -> a._2.asInstanceOf[Array[String]]).groupByKey().count()) //number of documents in sequence file

    val tagged = postagger(tokenized).cache()
    // tagged.groupByKey(6).collect().foreach(t => println(t._1))
    // tagged.take(10).foreach(t => println(t._2.mkString(" ")))

    val filtered = tagged.map(d => {
      val allowedTags = List(
        "JJ",
        "JJR",
        "JJS",
        "NN",
        "NNS",
        "NNP",
        "NNPS",
        "RB",
        "RBR",
        "RBS",
        "VB",
        "VBD",
        "VBG",
        "VBN",
        "VBP",
        "VBZ",
        "UH",
        "SYM",
        "RP",
        "FW"
      )

      d._1 -> Tag[Array[String], Tokenized](d._2.filter(w => 
        (false /: allowedTags)((s, t) => 
          s || w.split("_")(1).contentEquals(t)
        )
      ))
    })

    hashing(filtered).groupByKey().take(10).foreach(t => println(t._2.foldLeft(0)((s, t) => s + t.denseCount)))

    // Testing batch tokenize and then batch pos tag workflow
    val batchTokenizer = new BatchPTBTokenizer[String, Text]
    val batchPOSTagger = new BatchMaxEntPOSTagger[String]

    val batchTokenized = batchTokenizer(s).cache()
    //  println("number of entries after batch tokenization " + batchTokenized.count())
    val batchTagged = batchPOSTagger(batchTokenized)
    //  println("number of entried after batch POSTagging " + batchTagged.count())

    val featureHashing = new SimpleFeatureHashing[String](100000) with Serializable


    val filteredTokens = batchTagged.map(d => {
      val allowedTags = List(
        "JJ",
        "JJR",
        "JJS",
        "NN",
        "NNS",
        "NNP",
        "NNPS",
        "RB",
        "RBR",
        "RBS",
        "VB",
        "VBD",
        "VBG",
        "VBN",
        "VBP",
        "VBZ",
        "UH",
        "SYM",
        "RP",
        "FW"
      )

      d._1 -> Tag[Array[String], Tokenized](d._2.flatten.filter(w => 
        (false /: allowedTags)((s, t) => 
          s || w.split("_")(1).contentEquals(t)
        )
      ))

    })
    
    featureHashing(filteredTokens).take(10).foreach(t => println(t._2.denseCount))
  }

}
