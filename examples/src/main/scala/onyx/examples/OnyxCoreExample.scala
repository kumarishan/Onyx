package onyx.examples

import onyx.core._
import implicits._
import syntax._
import doc._
import onyx.processing._
import tokenize._
import tokenize.implicits._
import pos._
import featurize._

import org.apache.hadoop.io.Text

import com.twitter.algebird._

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

    val tokenize = new PTBTokenizer[String]
    val posTag = new MaxEntPOSTagger[Array[String]]
    val featurize = new FeatureHashing[Array[String]](10000)

    val filterPOSTag = (s: Array[String]) => {
      val allowedTags = List(
        "JJ", "JJR", "JJS", "NN",
        "NNS", "NNP", "NNPS", "RB",
        "RBR", "RBS", "VB", "VBD",
        "VBG", "VBN", "VBP", "VBZ",
        "UH", "SYM", "RP", "FW"
      )

      s.filter(t => (false /: allowedTags)((s, a) => s || t.split("_")(1).contentEquals(a)))
    }

    val processed =
      source |@|
      mapValues[Text, Text, Book](book.parse[Text]) |@|
      {s: (Book) => s.content} |@|
      map({s: (Text, Array[Byte]) => { s._1.toString -> new String(s._2)}}) |@|
      mapValues[String, String, Array[String]](tokenize) |@|
      posTag |@|
      filterPOSTag |@|
      featurize

    processed.getRDD.collect.foreach({s: (String, AdaptiveVector[Int]) => println(s._2.denseCount)})

  }
}