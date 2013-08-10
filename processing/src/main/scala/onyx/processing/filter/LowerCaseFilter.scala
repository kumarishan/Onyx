package onyx.processing.filter

class LowerCaseFiltering extends (String => String) {
  def apply(s: String) = s.toLowerCase
}