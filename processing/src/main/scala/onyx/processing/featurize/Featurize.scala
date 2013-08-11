package onyx.processing.featurize

import com.twitter.algebird._

trait Featurize[T, R] extends (T => AdaptiveVector[R]) with Serializable {

  def apply(s: T) = featurize(s)
  def featurize(s: T): AdaptiveVector[R]
}

object implicits {
  implicit def adVector2TermCount(a: AdaptiveVector[Int]) = new TermCount[AdaptiveVector[Int], Int]{
    def termItr = a.denseIterator
  }

  implicit def arrayString2TermCount(s: Array[String]) = {
    new TermCount[Array[String], String]{
      def termItr = s.groupBy(p => p).map(t => t._1 -> t._2.foldLeft(0)((s, a) => s + 1)).toIterator
    }
  }
}