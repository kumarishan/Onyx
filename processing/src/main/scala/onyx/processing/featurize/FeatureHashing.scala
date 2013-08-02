package onyx.processing.featurize

import java.nio.ByteBuffer

import onyx._
import OnyxTags._

import spark.RDD

import com.twitter.algebird._

private[featurize] trait FeatureHashing {

  def featurize(numFeatures: Int, doc: Array[Array[String] @@ Tokenized]): AdaptiveVector[Int] = {
    featurize(numFeatures, Tag[Array[String], Tokenized](doc.flatten))
  }

  def featurize(numFeatures: Int, doc: Array[String] @@ Tokenized): AdaptiveVector[Int] = {
    var featureVector = AdaptiveVector.fromMap(Map[Int, Int](), 0, numFeatures)

    doc.foreach(t => {
      var k = CassandraMurmurHash.hash32(ByteBuffer.wrap(t.getBytes), 0, t.length, 31) % numFeatures/2
      k = if( k > 0) k + numFeatures/2
      else k + numFeatures/2 - 1

      featureVector(k)
      featureVector = featureVector.updated(k, featureVector(k) + 1) 
    })
    featureVector
  }
    
}

class BatchFeatureHashing[K](numFeatures: Int)
  extends (RDD[(K, Array[Array[String] @@ Tokenized])] => RDD[(K, AdaptiveVector[Int])])
  with FeatureHashing with Serializable {
  
  def apply(source: RDD[(K, Array[Array[String] @@ Tokenized])]) = {
    source.map(t => {
      t._1 -> featurize(numFeatures, t._2)
    })
  }
}

class SimpleFeatureHashing[K](numFeatures: Int)
  extends (RDD[(K, Array[String] @@ Tokenized)] => RDD[(K, AdaptiveVector[Int])])
  with FeatureHashing with Serializable {
  
  def apply(source: RDD[(K, Array[String] @@ Tokenized)]) = {
    source.map(t => {
      t._1 -> featurize(numFeatures, t._2)
    })
  }
}
