package onyx.examples

import java.io.StringReader

import org.apache.lucene.document.{Document, Field, TextField, StringField}

import java.util.UUID

import onyx.core._
import implicits._
import syntax._
import doc._
import onyx.indexing.lucene._

import spark._
import spark.SparkContext
import SparkContext._

import org.apache.hadoop.io.Text

object OnyxIndexingExample {
  def main(args: Array[String]){

    System.setProperty("spark.local.dir", "/tmp")

    val sc = new SparkContext("local", "onyx-indexing")
    val source = sc.sequenceFile[Text, Text]("hdfs://localhost:54310/usr/hduser/doc-processing-ex.seq", 3)

    implicit def book2LDocument(s: Book): Document = {
      val doc = new Document
      doc.add(new TextField("content", new StringReader(new String(s.content))))
      doc.add(new TextField("author", new StringReader(s.authors.mkString(" "))))
      doc
    }

    val uuid = UUID.randomUUID
    val indexer = new StandardLuceneIndexer[Book]("/usr/hduser/" + uuid.toString)

    val indexing =
      source |@|
      mapValues[Text, Text, Book](book.parse[Text]) |@|
      map({s: (Text, Book) => s._2}) |@|
      indexer
  }

}