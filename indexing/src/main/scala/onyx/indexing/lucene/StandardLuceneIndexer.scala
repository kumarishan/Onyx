package onyx.indexing.lucene

import java.io.File
import java.net.{URI, URLEncoder}

import spark.RDD

import org.apache.lucene.document.Document
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.util.Version
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer

import org.apache.hadoop.fs.{FileSystem, Path, FileUtil}
import org.apache.hadoop.conf.Configuration

/**
 * This class gives and easy to user function to index RDDs where each entry is
 * an document to be indexed. The index file is created in the local tmp directory
 * After indexing is done its optimized and then they are stored to provided hdfs
 *
 * Currently this serve as a naive example how to index using Lucene on spark
 *
 * @author Kumar Ishan (@kumarishan)
 */
class StandardLuceneIndexer[D](
    toHDFSUrl: String
  )(implicit ev: D => Document, ev2: ClassManifest[D]) extends (RDD[D] => RDD[D]) with Serializable {

  def apply(source: RDD[D]) = {
    source.mapPartitionsWithIndex((i, itr) => {
      val analyzer = new StandardAnalyzer(Version.LUCENE_44)
      val localIndexDir = new File(System.getProperty("spark.local.dir"), "shard-" + i)
      val indexWriterConfig = new IndexWriterConfig(Version.LUCENE_44, analyzer)
      val indexWriter = new IndexWriter(FSDirectory.open(localIndexDir), indexWriterConfig)

      val ret = itr.foreach {d => {indexWriter.addDocument(d); true}}
      indexWriter.close

      val conf = new Configuration(false)
      conf.addResource(new Path("/usr/lib/hadoop/conf/core-default.xml"))
      conf.addResource(new Path("/usr/lib/hadoop/conf/core-site.xml"))

      val filesystem = FileSystem.get(conf)
      val dest = new Path(toHDFSUrl)
      filesystem.copyFromLocalFile(true, new Path(localIndexDir.getAbsolutePath), dest)

      itr
    }).collect

    source
  }

}