package onyx.examples

import onyx.processing._
import tokenize._
import pos._

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
    println(source.map(t => t._1.toString -> t._2.toString).groupByKey().count()) // number of documents in sequence file

    // Testing simple tokenize then pos tag workflow
    val tokenizer = new SimplePTBTokenizer[String, Text]
    val postagger = new SimpleMaxEntPOSTagger[String]

    val tokenized = tokenizer(s).cache()
    println(tokenized.map(a => a._1.toString -> a._2.asInstanceOf[Array[String]]).groupByKey().count()) //number of documents in sequence file

    val tagged = postagger(tokenized).cache()
    tagged.groupByKey(6).collect().foreach(t => println(t._1))
    tagged.take(10).foreach(t => println(t._2.mkString(" ")))

    // Testing batch tokenize and then batch pos tag workflow
    val batchTokenizer = new BatchPTBTokenizer[String, Text]
    val batchPOSTagger = new BatchMaxEntPOSTagger[String]

    val batchTokenized = batchTokenizer(s).cache()
    println("number of entries after batch tokenization " + batchTokenized.count())
    val batchTagged = batchPOSTagger(batchTokenized)
    println("number of entried after batch POSTagging " + batchTagged.count())

  }
}
