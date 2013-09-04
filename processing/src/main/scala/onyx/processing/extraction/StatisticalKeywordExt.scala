package onyx.processing.extraction

import onyx.processing._
import tokenize._
import filter._

/**
 * Statistical Keyword Extractor is a single document important keyword extractor using statistical
 * technique. The importance of this technique is that it doesnt require to process whole corpus
 * Currently its partially implemented from research paper "Keyword Extraction from a Single Document
 * using Word Co-occurences Statistical Information"
 *
 * @author Kumar Ishan (@kumarishan)
 */
class StatisticalKeywordExt[D <% Tokenizable[D]](
  stopFilter: Filter[String],
  sentTokenizer: Tokenizer[D, Array[String]],
  minTermFreq: Int = 3,
  selectFreqTerms: (Seq[(String, Int)] => Seq[(String, Int)]) =
    {(s: Seq[(String, Int)]) =>
      s.take(scala.math.floor(s.size.toDouble * 0.3).toInt)
    }
) extends (D => Array[(String, Double)]) with Serializable {

  def apply(doc: D) = {

    val sentences = sentTokenizer tokenize(doc) map { s => s filter(stopFilter) map {_.toLowerCase} }

    val freq = sentences.foldLeft((Map[String, Int](), Map[String, Int]()))((m, sent) =>
      (m._1 ++
        sent.map({w => w -> (1 + m._1.getOrElse(w, 0))}) ++
        sent.sliding(2).map({w => w.mkString(" ") -> (1 + m._1.getOrElse(w.mkString(" "), 0))})
      ) ->
      (m._2 ++
        sent.map({w => w -> (sent.length + m._2.getOrElse(w, 0))}) ++
        sent.sliding(2).map({w => w.mkString(" ") -> (sent.length + m._2.getOrElse(w.mkString(" "), 0))})
      )
    )

    val termF = freq._1.filter(_._2 > minTermFreq)
    val sentF = freq._2

    val allFreqTerms = termF.toSeq.sortWith((a, b) => a._2 > b._2 )
    val freqTerms = selectFreqTerms(allFreqTerms).map(_._1)

    var coccurrences = sentences.foldLeft(Map[(String, String), Int]())((c, sent) => {
      var partition = sent.partition(freqTerms.contains(_))
      c ++ partition._2.flatMap(n => partition._1.map({n -> _})).map(k => k -> (1 + c.getOrElse(k, 0)))
    })

    val comb = sentences.map(s => s.sliding(2))

    coccurrences = comb.foldLeft(coccurrences)((c, sent2) => {
      var partition = sent2.partition({bi => freqTerms.foldLeft(false)((b, t) => b || bi.contains(t))})
      var freq = partition._1.toSeq.flatten.filter(freqTerms.contains(_))
      c ++ partition._2.flatMap(n => freq.map({n.mkString(" ") -> _})).map(k => k -> (1 + c.getOrElse(k, 0)))
    })

    val chi2 = termF.foldLeft(Map[String, Double]())((m, t) => {
      if(!freqTerms.contains(t._1)){
        m + (t._1 -> freqTerms.foldLeft(0.0)((s, g) => {
          val nwpg = sentF.get(g).get * sentF.get(t._1).get
          s + scala.math.pow(coccurrences.getOrElse((t._1, g), 0) - nwpg, 2)/nwpg.toDouble
        }))
      } else m
    })

    chi2.toSeq.sortWith((a, b) => a._2 > b._2).toArray
  }
}