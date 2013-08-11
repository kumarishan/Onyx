package onyx.processing.tokenize

abstract class StringTokenizer[T <% Tokenizable[T]] extends Tokenizer[T, String] {
  val delimiter: String

  def tokenize(s: T): Array[String] = {
    s.text.split(delimiter)
  }
}

class CharTokenizer[T <% Tokenizable[T]] extends Tokenizer[T, Char] {
  override def tokenize(s: T): Array[Char] = s.text.toCharArray
}

class WhitespaceTokenizer[T <% Tokenizable[T]] extends StringTokenizer[T] {
  val delimiter: String = " "
}

class LineTokenizer[T <% Tokenizable[T]](filter_blank_lines: Boolean = true, discard_eol: Boolean = true)
  extends Tokenizer[T, String] {

  def tokenize(s: T): Array[String] = {
    var lines = s.text.split("\n")
    if(filter_blank_lines){
      lines = lines.filter(_.trim.isEmpty)
    }
    if(discard_eol){
      lines = lines.map(_.stripLineEnd)
    }
    lines
  }

}

