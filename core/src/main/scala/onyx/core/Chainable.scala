package onyx.core

import scala.collection._

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

sealed case class RDDChainable[T](s: RDD[T]) extends Chainable[T] {
  def getRDD: RDD[T] = s
}

trait ChainableImplicits {
  implicit def rdd2Chainable[T](r: RDD[T]) = RDDChainable(r)
}
