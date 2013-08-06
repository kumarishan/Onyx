package onyx.core.syntax

import onyx.core._
import spark.RDD 

trait SpTransOps[T, U] {
  def getChainable(s: RDD[T]): SpTransChainable[U]
}

case class map[T, U : ClassManifest](f: T => U) extends SpTransOps[T, U]{
  def getChainable(s: RDD[T]) = MapChainable(s, f)
}

case class flatMap[T, U : ClassManifest](f: T => TraversableOnce[U]) extends SpTransOps[T, U] {
  def getChainable(s: RDD[T]) = FlatMapChainable(s, f)
}

class SpBaseChainOps[U](lhs : Chainable[U]) {
  def |@|[V : ClassManifest](t: SpTransOps[U,V]) = t.getChainable(lhs.getRDD)
  def |@|[V : ClassManifest](rhs: RDD[U] => RDD[V]) = RDDChainable(rhs(lhs.getRDD))
}

final class SpTransChainOps[U](lhs: SpTransChainable[U]) extends SpBaseChainOps(lhs) {
  def |@|[V : ClassManifest](rhs: U => V) = lhs.append(rhs)
}

final class RDDChainOps[U](lhs: RDDChainable[U]) extends SpBaseChainOps(lhs)

trait ChainOpsImplicits {
  implicit def spChainable2Ops[U](lhs: SpTransChainable[U]) = new SpTransChainOps(lhs)
  implicit def rddChainable2Ops[U](lhs: RDDChainable[U]) = new RDDChainOps(lhs)
  implicit def rdd2Ops[U](lhs: RDD[U]) = new RDDChainOps(RDDChainable(lhs))
}