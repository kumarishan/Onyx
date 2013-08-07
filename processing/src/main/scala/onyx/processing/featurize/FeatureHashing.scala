package onyx.processing.featurize

import java.nio.ByteBuffer

import onyx.processing.tokenize._

import com.twitter.algebird._

class FeatureHashing[T <% Tokenized[T, String]](numFeatures: Int) extends Featurize[T, Int] {

  def featurize(s: T): AdaptiveVector[Int] = {
    var featureVector = AdaptiveVector.fromMap(Map[Int, Int](), 0, numFeatures)

    s.tokens.foreach(t => {
      var k = CassandraMurmurHash.hash32(ByteBuffer.wrap(t.getBytes), 0, t.length, 31) % numFeatures/2
      k = if( k > 0) k + numFeatures/2
      else k + numFeatures/2 - 1

      featureVector(k)
      featureVector = featureVector.updated(k, featureVector(k) + 1) 
    })
    featureVector
  }

}