package onyx.processing.clustering

import spark.SparkContext
import SparkContext._
import spark.RDD

import onyx.processing._

/**
 * KMeans clustering using standard Lloyd's algorithm.
 * This implementation allows configuring various parameters as described below
 *
 * Each type for which kMeans needs to be performed must have implicit objects
 * in the scope implementing Distance and Average class.
 *
 * @author Kumar Ishan (@kumarishan)
 *
 * @param numClusters
 * @param maxIter
 * @param overclusteringFactor
 * @param epsilon
 * @param skipEmptyCluster
 * @param initMethod
 *
 * @return a function to perform clustering and return centroids of cluster
 *
 * @see Distance, Average
 */
class KMeans[T](
    numClusters: Int,
    maxIter: Int,
    overclusteringFactor: Int,
    epsilon: Double,
    skipEmptyCluster: Boolean,
    initMethod: (Int, RDD[T]) => Seq[T]
 )(implicit m: ClassManifest[T], dist: Distance[T], avg: Average[T, Int, T, Int])
    extends (RDD[T] => Seq[T]) with Serializable {

  require(numClusters > 0)
  require(overclusteringFactor > 0)

  def apply(source: RDD[T]) = {

    def kMeans(centroids: Seq[T], nth: Int): Seq[T] = {
		 val clusters = (source
				.map(
					point =>
						centroids.reduceLeft(
							(a, b) => if(dist.distance(point, a) < dist.distance(point, b)) a else b
						) -> (point, 1)
				).reduceByKeyToDriver({
					case ((point1, num1), (point2, num2)) =>
            (avg.numSum(point1, point2), avg.denomSum(num1, num2))
				}).map({
					case (centroid, (pointSum, numPts)) =>
            centroid -> avg.avg(pointSum, numPts)
				})
			)

      val newCentroids = {
        if(skipEmptyCluster) centroids
        else centroids.filter(clusters.contains(_))
      }.map( oldCentroid => {
        clusters.get(oldCentroid) match {
          case Some(newCentroid) => newCentroid
          case None => oldCentroid
        }
      })

      val movement = (centroids zip newCentroids).map({case (a, b) => dist.distance(a, b)})

      if (movement.exists( _ > epsilon) && nth < maxIter)
        kMeans(newCentroids, nth + 1)
      else
        newCentroids
		}

    kMeans(initMethod(numClusters * overclusteringFactor, source), 1)
	}

}

/**
 * KMeans object provide easier instantiation of KMeans class
 */
object KMeans {

  def apply[T](
    epsilon: Double,
    skipEmptyCluster: Boolean,
    initMethod: (Int, RDD[T]) => Seq[T]
  )(implicit m: ClassManifest[T], dist: Distance[T], avg: Average[T, Int, T, Int]) =
    (numClusters: Int, maxIter: Int, overclusteringFactor: Int) =>
      new KMeans[T](
        numClusters,
        maxIter,
        overclusteringFactor,
        epsilon,
        skipEmptyCluster,
        initMethod
      )
}