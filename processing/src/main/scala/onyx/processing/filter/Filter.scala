package onyx.processing.filter

trait Filter[D] extends (D => Boolean) with Serializable