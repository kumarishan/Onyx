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
}