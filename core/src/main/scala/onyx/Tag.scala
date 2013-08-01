package onyx

object Tag {
  @inline def apply[@specialized A, T](a: A): A @@ T = a.asInstanceOf[A @@ T]
}