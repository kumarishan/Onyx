package onyx.examples

import onyx.core._
import implicits._
import syntax._
import doc._
import onyx.processing.tokenize._

import org.apache.hadoop.io.Text

import spark._
import spark.SparkContext
import SparkContext._

object OnyxCoreExamples {

  implicit def string2Tokenizable(s: String) = new Tokenizable[String] {
    def text = s.toString
  }

  def main(args: Array[String]){
    val sc = new SparkContext("local", "onyx-core")
    val source = sc.sequenceFile[Text, Text]("hdfs://localhost:54310/usr/hduser/doc-processing-ex.seq")

    val tokenizer = new WhitespaceTokenizer[String]

    val processed =
      source |@|
      mapValues[Text, Text, Book](book.parse[Text]) |@|
      {s: (Book) => s.content} |@|
      map({s: (Text, Array[Byte]) => { s._1.toString -> new String(s._2)}}) |@|
      mapValues[String, String, Array[String]](tokenizer)

    processed.getRDD.collect.foreach({s: (String, Array[String]) => println(s._2.length)})

  }
}