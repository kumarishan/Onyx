package object onyx {
  type Tagged[T] = {type Tag = T}

  /**
   * Tag a type `T` with `Tag`. The resulting type is a subtype of `T`.
   *
   * The resulting type is used to discriminate between type class instances.
   *
   * @see [[onyx.Tag]] and [[onyx.Tags]]
   *
   * Credit to Miles Sabin for the idea.
   */
  type @@[+T, Tag] = T with Tagged[Tag]
}