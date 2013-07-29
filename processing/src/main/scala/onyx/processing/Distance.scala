package onyx.processing

trait Distance[T] extends Serializable {
  def distance(from: T, to: T): Double
}