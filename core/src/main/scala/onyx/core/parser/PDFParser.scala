package onyx.core.parser

import java.io.{File, InputStream}

import scala.collection.JavaConversions._

import onyx.core.util._
import onyx.core.parser.{PDFTextStripper => PDT}

import org.apache.pdfbox.util.{TextPosition}
import org.apache.pdfbox.pdmodel.{PDDocument}

/**
 * PDFParser parses pdf documents and publishes common emitters like lineE, textPositionE, articleE, etc
 * All these emitters can then be observed by the user for further processing
 *
 * Implementation details:
 * - extends PDFStreamEngine
 * - borrows implementations of PDFTextStripper
 * - Ideally its a port of PDFTextStripper to Reactive Programming
 *
 * @author Kumar Ishan
 */
class PDFParser extends PDT {

  val lineE = Var(new java.util.ArrayList[TextPosition]())
  // val lineE = Reactive{_lineE()}

  def parse(doc: InputStream): String = parse(PDDocument.load(doc))
  def parse(doc: File): String = parse(PDDocument.load(doc))
  def parse(doc: PDDocument): String = { getText(doc) }

  override def writeLine(lineTP: java.util.List[TextPosition], isRtlDominant: Boolean, hasRtl: Boolean){
    lineE() = lineTP.asInstanceOf[java.util.ArrayList[TextPosition]]
    println(lineE.childrens)
    super.writeLine(lineTP, isRtlDominant, hasRtl)
  }
}