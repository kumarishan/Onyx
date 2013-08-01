package onyx

object OnyxTags {

  sealed trait Batch
  def Batch[A](a: A): A @@ Batch = Tag[A, Batch](a)

  sealed trait Simple
  def Simple[A](a: A): A @@ Simple = Tag[A, Simple](a)

  sealed trait Tokenized
  def Tokenized[A](a: A): A @@ Tokenized = Tag[A, Tokenized](a)
}
