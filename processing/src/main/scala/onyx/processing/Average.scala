package onyx.processing

trait Average[T, S, U, V] extends Serializable {
  def numSum(a: T, b: T): U
  def denomSum(a: S, b: S): V
  def avg(num: U, denom: V): T
}