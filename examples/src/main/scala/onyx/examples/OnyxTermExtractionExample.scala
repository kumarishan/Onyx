package onyx.examples

import scala.io.Source

import onyx.core._
import implicits._
import syntax._
import doc._
import onyx.processing._
import extraction._
import tokenize._
import filter._

import spark._
import spark.SparkContext
import SparkContext._

import org.apache.hadoop.io.Text

object OnyxTermExtractionExample {
  def main(args: Array[String]){

    val sc = new SparkContext("local", "onyx-term-extraction")
    val source = sc.sequenceFile[Text, Text]("hdfs://localhost:54310/usr/hduser/doc-processing-ex.seq", 3)

    implicit def book2Tokenizable(s: Book) = new Tokenizable[Book]{
      def text: String = new String(s.content)
    }

    val sentTokenizer = new PTBSentenceTokenizer[Book]
    val stopFilter = new StopWordFilter(Source.fromFile("google-10000-english.txt").getLines.toArray.map(_.trim))
    val termExtract = new StatisticalKeywordExt[Book](stopFilter, sentTokenizer)

    val impTerms =
      source |@|
      mapValues[Text, Text, Book](book.parse[Text]) |@|
      map({s: (Text, Book) => s._1.toString -> {s._2.title = s._1.toString; s._2}}) |@|
      mapValues[String, Book, Array[(String, Double)]](termExtract)

    impTerms.getRDD.collect.foreach({s: (String, Array[(String, Double)]) =>
      println(s._2.filter(_._1.contains(" ")).mkString("\n"))
      println("\n\n\n")
    })
  }
}
