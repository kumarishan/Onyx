package onyx.processing.featurize

import scala.math._

import spark.RDD
import spark.SparkContext._

trait TermCount[V, W] {
  def termItr: Iterator[(W, Int)]
}

class TfIdfScore[W](val score: Seq[(W, Double)]) extends Serializable

class TfIdfWeighting[K : ClassManifest, V <% TermCount[V, W], W : ClassManifest](numberOfDocs: Int)
  extends (RDD[(K, V)] => RDD[(K, TfIdfScore[W])]) with Serializable {

  def apply(s: RDD[(K, V)]) =
    s.flatMap(d => {
      d._2.termItr.map(wc => wc._1 -> (d._1 -> wc._2))

    }).groupByKey().flatMap(w => {
      val idf = log(numberOfDocs.toDouble / (w._2.size.toDouble + 1))
      w._2.map(d => {
        d._1 -> (w._1 -> log1p(d._2.toDouble) * idf)
      })

    }).groupByKey().map(d => {
      d._1 -> new TfIdfScore[W](d._2)
    })

}