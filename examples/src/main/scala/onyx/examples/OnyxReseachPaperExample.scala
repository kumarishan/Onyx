package onyx.examples

import java.io.{File, FileInputStream}

import onyx.core._
import implicits._
import syntax._
import doc._

object OnyxResearchPaperExample {
  implicit def file2InputStream(s: File) = new FileInputStream(s)

  def main(args: Array[String]){
    var pdf: File = null
    var rsrchPaper: ResearchPaper = null

    pdf = new File("research-paper-1.pdf")
    rsrchPaper = researchPaper.parse[File].apply(pdf)
    print("\ntitle:  ")
    rsrchPaper.get("title").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nauthors:  ")
    rsrchPaper.get("authors").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nabstract:  ")
    rsrchPaper.get("abstract").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))

    println("\n\n")

    pdf = new File("research-paper-2.pdf")
    rsrchPaper = researchPaper.parse[File].apply(pdf)
    print("\n\ntitle:  ")
    rsrchPaper.get("title").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nauthors:  ")
    rsrchPaper.get("authors").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nabstract:  ")
    rsrchPaper.get("abstract").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))

    println("\n\n")

    pdf = new File("research-paper-3.pdf")
    rsrchPaper = researchPaper.parse[File].apply(pdf)
    print("\n\ntitle:  ")
    rsrchPaper.get("title").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nauthors:  ")
    rsrchPaper.get("authors").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nabstract:  ")
    rsrchPaper.get("abstract").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))

    println("\n\n")

    pdf = new File("research-paper-4.pdf")
    rsrchPaper = researchPaper.parse[File].apply(pdf)
    print("\n\ntitle:  ")
    rsrchPaper.get("title").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nauthors:  ")
    rsrchPaper.get("authors").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nabstract:  ")
    rsrchPaper.get("abstract").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))

    println("\n\n")

    pdf = new File("research-paper-5.pdf")
    rsrchPaper = researchPaper.parse[File].apply(pdf)
    print("\n\ntitle:  ")
    rsrchPaper.get("title").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nauthors:  ")
    rsrchPaper.get("authors").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nabstract:  ")
    rsrchPaper.get("abstract").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))

    println("\n\n")

    pdf = new File("research-paper-6.pdf")
    rsrchPaper = researchPaper.parse[File].apply(pdf)
    print("\n\ntitle:  ")
    rsrchPaper.get("title").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nauthors:  ")
    rsrchPaper.get("authors").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nabstract:  ")
    rsrchPaper.get("abstract").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))

    println("\n\n")

    pdf = new File("research-paper-7.pdf")
    rsrchPaper = researchPaper.parse[File].apply(pdf)
    print("\n\ntitle:  ")
    rsrchPaper.get("title").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nauthors:  ")
    rsrchPaper.get("authors").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nabstract:  ")
    rsrchPaper.get("abstract").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))

    println("\n\n")

    pdf = new File("research-paper-8.pdf")
    rsrchPaper = researchPaper.parse[File].apply(pdf)
    print("\n\ntitle:  ")
    rsrchPaper.get("title").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nauthors:  ")
    rsrchPaper.get("authors").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nabstract:  ")
    rsrchPaper.get("abstract").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))

    println("\n\n")

    pdf = new File("research-paper-9.pdf")
    rsrchPaper = researchPaper.parse[File].apply(pdf)
    print("\n\ntitle:  ")
    rsrchPaper.get("title").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nauthors:  ")
    rsrchPaper.get("authors").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nabstract:  ")
    rsrchPaper.get("abstract").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))

    println("\n\n")

    pdf = new File("research-paper-10.pdf")
    rsrchPaper = researchPaper.parse[File].apply(pdf)
    print("\n\ntitle:  ")
    rsrchPaper.get("title").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nauthors:  ")
    rsrchPaper.get("authors").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nabstract:  ")
    rsrchPaper.get("abstract").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))

    println("\n\n")

    pdf = new File("research-paper-11.pdf")
    rsrchPaper = researchPaper.parse[File].apply(pdf)
    print("\n\ntitle:  ")
    rsrchPaper.get("title").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nauthors:  ")
    rsrchPaper.get("authors").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nabstract:  ")
    rsrchPaper.get("abstract").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))

    println("\n\n")

    // pdf = new File("research-paper-12.pdf")
    // rsrchPaper = researchPaper.parse[File](pdf)
    // print("\n\ntitle:  ")
    // rsrchPaper.get("title").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    // print("\nauthors:  ")
    // rsrchPaper.get("content").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))

    // println("\n\n")

    pdf = new File("research-paper-13.pdf")
    rsrchPaper = researchPaper.parse[File].apply(pdf)
    print("\n\ntitle:  ")
    rsrchPaper.get("title").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nauthors:  ")
    rsrchPaper.get("authors").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))
    print("\nabstract:  ")
    rsrchPaper.get("abstract").get.asInstanceOf[ContentSequence].iterator.foreach(a => print(a))

  }
}