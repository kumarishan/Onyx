package onyx.core.parser

import java.io.{File, InputStream}

import scala.collection.JavaConversions._

import onyx.core.util._
import onyx.core.parser.{PDFTextStripper => PDT}

import org.apache.pdfbox.util.{TextPosition}
import org.apache.pdfbox.pdmodel.{PDDocument}

/**
 * ResearchPapaerParser parses extracts title authors and abstract from the pdf file
 *
 * @author Kumar Ishan
 */
class ResearchPaperParser extends PDT {

  var title = ""
  var author = ""
  var abs = ""
  var authorFound = false
  var titleFound = false
  var abstractFound = false
  var lastFontSize = -1.0f
  var lastCapHeight = -1.0f
  var lastFontName = ""

  def parse(doc: InputStream): String = parse(PDDocument.load(doc))
  def parse(doc: File): String = parse(PDDocument.load(doc))
  def parse(doc: PDDocument): String = { getText(doc) }

  override def writeLine(lineTP: java.util.List[TextPosition], isRtlDominant: Boolean, hasRtl: Boolean){
    if(!titleFound || !authorFound || !abstractFound){
      val lineLength = lineTP.size

      lineTP.foreach(w => {
        if(w.getFont() != null && w.getFont().getFontDescriptor() != null){
          val fontDesc = w.getFont().getFontDescriptor()
          val fontName = fontDesc.getFontName()
          val isItalic = fontDesc.isItalic
          val capHeight = fontDesc.getCapHeight()
          val currFontSize = w.getFontSizeInPt()
          if(!acceptableChange(capHeight, currFontSize, isItalic)){
            if(title.length > 0 && (!isTitleCandidate(capHeight, currFontSize, isItalic, fontName))){ titleFound = true}
            if(!titleFound && isNewTitleCand(capHeight, currFontSize, isItalic, fontName)) {title = ""}
          }
          if(!titleFound){
            if(isTitleCandidate(capHeight, currFontSize, isItalic, fontName)){
              title = title + w.getCharacter()
            }
          } else if(!authorFound){
            if(isAuthorCandidate(w, capHeight, currFontSize, isItalic, lineLength)){
              author = author + w.getCharacter()
            } else {
              authorFound = true
              abs = abs + w.getCharacter()
            }
          } else if(!abstractFound){
            abs = abs + w.getCharacter()
          }

          lastFontSize = currFontSize
          lastCapHeight = capHeight
          lastFontName = fontName
        } else if(!titleFound && title.trim.length != 0){ title = title + " "}
        else if(!authorFound && author.trim.length != 0){ author = author + " "}
        else if(!abstractFound && abs.trim.length != 0){ abs = abs + " "}
      })

      if(!titleFound && title.trim.length != 0){ title = title + " "}
      else if(!authorFound && author.trim.length != 0){ author = author + " "}
    }
    super.writeLine(lineTP, isRtlDominant, hasRtl)
  }

  def acceptableChange(capHeight: Float, fontSize: Float, isItalic: Boolean): Boolean = {
    if(fontSize != lastFontSize){return false}
    else { return true}
  }

  def isTitleCandidate(capHeight: Float, fontSize: Float, isItalic: Boolean, fontName: String): Boolean = {
    if(fontSize >= 12.0f && capHeight >= 700){ return true}
    else if(fontSize == 14.0f && capHeight > 600){ return true}
    else if(fontSize >= 15.0f && fontSize <= 17.0f && capHeight < 700){ return true}
    else if(fontSize == 11.0f && fontName.toLowerCase.contains("bold")){ return true}
    else if(fontSize > 17 && capHeight < 700){ return false}
    else {return false}
  }

  def isNewTitleCand(capHeight: Float, fontSize: Float, isItalic: Boolean, fontName: String): Boolean = {
    if(lastFontSize > fontSize && (lastFontSize - fontSize) > 4){return false}
    else {return false}
  }

  def isAuthorCandidate(w: TextPosition, capHeight: Float, fontSize: Float, isItalic: Boolean, lineLength: Int): Boolean = {
    if(w.getX < 120 && (lineLength < 50 || lineLength > 70)){ return true}
    else if(w.getX < 120 && !author.toLowerCase.contains("abstract")){ return false}
    else if(author.toLowerCase.contains("abstract")){
      abs = author.substring(author.toLowerCase.indexOf("abstract"))
      author = author.substring(0, author.toLowerCase.indexOf("abstract"))
      return false
    } else { return true}
  }

  private var contParaEnd = 0

  override def writeParagraphEnd(){
    super.writeParagraphEnd()
    if(!titleFound && title.length > 0){if(contParaEnd > 1){titleFound = true}else {contParaEnd = contParaEnd + 1}}
    if(authorFound && !abstractFound && abs.length > 400){
      if(abs.toLowerCase.contains("abstract") && author.trim.length == 0){
      }
      abstractFound = true
    }
  }
}