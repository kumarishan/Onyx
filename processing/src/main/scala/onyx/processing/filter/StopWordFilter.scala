package onyx.processing.filter

/**
 * yet to properly implement .. based on google-10000 stopword list and
 * google-1/3million freq word list
 *
 * use fast techniques like bloom filter etc to search faster especially if its
 * a huge list
 *
 * @author Kumar Ishan (@kumarishan)
 */
class StopWordFilter(stopList: Array[String]) extends Filter[String] {
  def apply(s: String) = !stopList.contains(s.toLowerCase)
}