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

trait SpPairTransOps[K, T, U] {
  def getChainable(s: RDD[(K, T)]): SpPairTransChainable[K, U]
}

case class mapValues[K : ClassManifest, T : ClassManifest, U : ClassManifest](f: T => U) extends SpPairTransOps[K, T, U]{
  def getChainable(s: RDD[(K, T)]) = MapValuesChainable(s, f)
}

class SpBaseChainOps[U](lhs: Chainable[U]) {
  def |@|[V : ClassManifest](rhs: SpTransOps[U,V]) = rhs.getChainable(lhs.getRDD)
  def |@|[V : ClassManifest](rhs: RDD[U] => RDD[V]) = RDDChainable(rhs(lhs.getRDD))
}

class SpPairChainOps[K, U](lhs: SpPairTransChainable[K, U]) {
  def |@|[V : ClassManifest](rhs: U => V) = lhs.append(rhs)
  def |@|[K2 : ClassManifest, V : ClassManifest](rhs: SpTransOps[(K, U), (K2, V)]) = rhs.getChainable(lhs.getRDD)
  def |@|[K2 : ClassManifest, V : ClassManifest](rhs: RDD[(K, U)] => RDD[(K2, V)]) = PairRDDChainable(rhs(lhs.getRDD))
  def |@|[V : ClassManifest](rhs: SpTransOps[(K, U), V]) = rhs.getChainable(lhs.getRDD)
}

final class SpTransChainOps[U](lhs: SpTransChainable[U]) extends SpBaseChainOps(lhs) {
  def |@|[V : ClassManifest](rhs: U => V) = lhs.append(rhs)
}

final class SpTransChainPairOps[K, U](lhs: SpTransChainable[(K, U)]) {
  def |@|[V : ClassManifest](rhs: SpPairTransOps[K, U, V]) = rhs.getChainable(lhs.getRDD)
}

final class RDDChainOps[U](lhs: RDDChainable[U]) extends SpBaseChainOps(lhs)

final class PairRDDChainOps[K, U](lhs: PairRDDChainable[K, U]) {
  def |@|[V : ClassManifest](rhs: SpPairTransOps[K, U, V]) = rhs.getChainable(lhs.getRDD)
  def |@|[V : ClassManifest](rhs: RDD[(K, U)] => RDD[(K, V)]) = PairRDDChainable(rhs(lhs.getRDD))
  def |@|[V : ClassManifest](rhs: RDD[(K, U)] => V) = rhs(lhs.getRDD)
}

trait ChainOpsImplicits {
  implicit def spChainable2Ops[U](lhs: SpTransChainable[U]) = new SpTransChainOps(lhs)
  implicit def spChainable2PairOps[K, U](lhs: SpTransChainable[(K, U)]) = new SpTransChainPairOps(lhs)
  implicit def rddChainable2Ops[U](lhs: RDDChainable[U]) = new RDDChainOps(lhs)
  implicit def rdd2Ops[U](lhs: RDD[U]) = new RDDChainOps(RDDChainable(lhs))
  implicit def rddPair2Ops[K, U](lhs: RDD[(K, U)]) = new PairRDDChainOps(PairRDDChainable(lhs))
  implicit def spPairChainable2Ops[K, U](lhs: SpPairTransChainable[K, U]) = new SpPairChainOps(lhs)
  implicit def pairRDDChainable2Ops[K, U](lhs: PairRDDChainable[K, U]) = new PairRDDChainOps(lhs)
}