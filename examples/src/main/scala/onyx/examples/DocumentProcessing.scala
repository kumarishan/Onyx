package onyx.examples

import java.io.File
import java.io.StringReader
import java.net.URI

import java.util.{Properties, UUID}

import scala.io.Source
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{IOUtils, IntWritable, SequenceFile, Text}

import spark._
import spark.SparkContext
import SparkContext._

import edu.stanford.nlp.ling.{CoreLabel, HasWord}
import edu.stanford.nlp.process.{LexedTokenFactory, CoreLabelTokenFactory, PTBTokenizer}
import edu.stanford.nlp.pipeline.{StanfordCoreNLP, Annotation}
import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.tagger.maxent.MaxentTagger

class DocumentProcessing {

  def createSeqFile(){
    val files = new File("/tmp/gutenberg").listFiles
                .filter(_.getName.endsWith(".txt"))  
    
    val conf = new Configuration()

    // creating a sequence file locally. You can later copy this file to hdfs
    val uri = "doc-processing-ex.seq"
    val fs = FileSystem.get(URI.create(uri), conf)
    val path = new Path(uri)
    val key = new Text()
    val value = new Text()
    var writer: SequenceFile.Writer = null
    try {
      writer = SequenceFile.createWriter(fs, conf, path, key.getClass, value.getClass)
      for(f <- files){
        key.set(f.getName)
        val source = Source.fromFile(f)
        value.set(source.map(_.toByte).toArray)
        writer.append(key, value)
        source.close()
      }
    }finally {
      IOUtils.closeStream(writer)
    }
  }

  def processDoc(){
    val sc = new SparkContext("local", "doc-processing")
    val source = sc.sequenceFile[Text, Text]("hdfs://localhost:54310/usr/hduser/doc-processing-ex.seq")
    val tokenized = tokenize(source).cache()

    tokenized.take(10).map(s => println(s._2.mkString("----")))
    println("POS Tags\n\n\n\n")
    println(filterUsingPOSTags(posTag(tokenized)).groupByKey(6).count())
  }

  def tokenize[T](source: RDD[(T, T)]): RDD[(String, Array[String])] = {
    source.flatMap(d => {
      val props = new Properties
      props.put("annotators", "tokenize ssplit")
      val tokenizer = new StanfordCoreNLP(props
) 
      val docId = d._1.toString
      val docContent = d._2.toString
      val annotation = new Annotation(docContent)
      tokenizer.annotate(annotation)
      annotation.get(classOf[SentencesAnnotation]).map(s => {
        val words = new ArrayBuffer[String]
        for(t <- s.get(classOf[TokensAnnotation])){
          words.add(t.word)
        }
        docId -> words.toArray
      })
    })
  }

  def posTag(source: RDD[(String, Array[String])]): RDD[(String, String)] = {
    val tagger = new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger") with Serializable
    source.map(s => {
       s._1 -> tagger.tagTokenizedString(s._2.mkString(" "))
    })
  }

  def filterUsingPOSTags(source: RDD[(String, String)]): RDD[(String, Array[String])] = {
    val allowedTags = List(
      "JJ",
      "JJR",
      "JJS",
      "NN",
      "NNS",
      "NNP",
      "NNPS",
      "RB",
      "RBR",
      "RBS",
      "VB",
      "VBD",
      "VBG",
      "VBN",
      "VBP",
      "VBZ",
      "UH",
      "SYM",
      "RP",
      "FW"
    )
    source.map(s => {
      s._1 -> s._2.split(" ").filter(w => (false /: allowedTags)((s, t) =>  s || w.split("_")(1).contentEquals(t))).toArray
    })
  }
}

object DocumentProcessing {

  def main(args: Array[String]){

    val docProcess = new DocumentProcessing

    args(0) match {
      case "create-seq" => docProcess.createSeqFile()
      case "process-doc" => docProcess.processDoc()
      case "combined-process" =>
    }
  }
}
