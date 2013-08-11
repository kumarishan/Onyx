package onyx.core

import scala.collection._

import spark._
import spark.SparkContext
import spark.SparkContext._
import spark.RDD

trait Chainable[U] {
  def getRDD: RDD[U]
}

sealed trait SpTransChainable[U] extends Chainable[U] {
  def append[V : ClassManifest](g: U => V): SpTransChainable[V]
}

sealed case class MapChainable[T, U : ClassManifest](s: RDD[T], f: T => U)
  extends SpTransChainable[U] {

  def append[V : ClassManifest](g: U => V) = MapChainable[T, V](s, {x => g(f(x))})
  def getRDD: RDD[U] = s.map(f)
}

sealed case class FlatMapChainable[T, U : ClassManifest](s: RDD[T], f: T => TraversableOnce[U])
  extends SpTransChainable[U] {

  def append[V : ClassManifest](g: U => V) = FlatMapChainable[T, V](s, {x => f(x) map g })
  def getRDD: RDD[U] = s.flatMap(f)
}

sealed trait SpPairTransChainable[K, U] extends Chainable[(K, U)] {
  def append[V : ClassManifest](g: U => V): SpPairTransChainable[K, V]
}

sealed case class MapValuesChainable[K : ClassManifest, T : ClassManifest, U : ClassManifest](s: RDD[(K, T)], f: T => U)
  extends SpPairTransChainable[K, U] {

    def append[V : ClassManifest](g: U => V) = MapValuesChainable[K, T, V](s, {x => g(f(x))})
    def getRDD: RDD[(K, U)] = s.mapValues[U](f)
}

sealed case class RDDChainable[T](s: RDD[T]) extends Chainable[T] {
  def getRDD: RDD[T] = s
}

sealed case class PairRDDChainable[K, V](s: RDD[(K, V)]) extends Chainable[(K, V)] {
  def getRDD: RDD[(K, V)] = s
}

trait ChainableImplicits {
  implicit def rdd2Chainable[T](r: RDD[T]) = RDDChainable(r)
  implicit def Chainable2RDD[T](lhs: Chainable[T]) = lhs.getRDD
}
