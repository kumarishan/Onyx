package onyx.examples

import java.util.UUID

import onyx.core._
import implicits._
import syntax._
import doc._
import onyx.processing._
import tokenize._
import tokenize.implicits._
import pos._
import featurize._
import featurize.implicits._
import clustering._

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
    val tfIdf = new TfIdfWeighting[String, AdaptiveVector[Int], Int](3)

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

    /* This is the Text processing pipeline */
    val processed =
      source |@|
      mapValues[Text, Text, Book](book.parse[Text]) |@|
      {s: (Book) => s.content} |@|
      map({s: (Text, Array[Byte]) => { s._1.toString -> new String(s._2)}}) |@|
      mapValues[String, String, Array[String]](tokenize) |@|
      posTag |@|
      filterPOSTag |@|
      featurize |@|
      tfIdf

    type Document = (String, TfIdfScore[Int])

    implicit val docAverage = new Average[Document, Int, Document, Int]{
      def numSum(a: Document, b: Document): Document = UUID.randomUUID.toString -> {
        val am = a._2.score.toMap
        val bm = b._2.score.toMap
        val mm = am ++ bm.map{ case (k, v) => k -> (v + am.getOrElse(k, 0.0)) }
        new TfIdfScore[Int](mm.toSeq)
      }

      def denomSum(a: Int, b: Int) = a + b
      def avg(num: Document, denom: Int) = num._1 -> new TfIdfScore(num._2.score.map(w => w._1 -> w._2 / denom))
    }

    implicit val docDist = new Distance[Document]{
      def distance(from: Document, to: Document): Double =
        scala.math.sqrt((from._2.score zip to._2.score).foldLeft(0.0)((s, t) => s + (t._1._2 - t._2._2)*(t._1._2 - t._2._2)))
    }

    val initRandom: (Int, RDD[Document]) => Seq[Document] =
      (n: Int, s: RDD[Document]) => s.takeSample(false, n, System.nanoTime.toInt)

    val kmeans = KMeans[Document](0.0001, true, initRandom)

    processed.getRDD.cache()

    val kmeansCentroid =
      processed |@|
      kmeans(2, 10, 1)

    println(kmeansCentroid.mkString(" "))

  }
}