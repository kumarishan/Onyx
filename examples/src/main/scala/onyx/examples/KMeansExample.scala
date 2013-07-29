package onyx.examples

import onyx.processing._
import clustering._

import spark._
import spark.SparkContext
import SparkContext._

class Doc(_tokens: Map[String, Int]) extends Serializable {
  val tokens  = _tokens
  
  def this(sentence: String) = this(
    sentence.split(" ").map(_ -> 1).foldLeft(Map[String, Int]())((m, token) => {
      m.updated(token._1, token._2 + m.get(token._1).getOrElse(0))
    })
  )

  override def hashCode = tokens.hashCode()
  override def equals(a: Any) = a.isInstanceOf[Doc] && a.asInstanceOf[Doc].tokens.equals(tokens)
  override def toString = tokens.mkString("(", ", ", ")")
}

object Doc {
  def apply(sentence: String) = new Doc(sentence)
}

object KMeansExample {

  implicit val docAverage = new Average[Doc, Int, Doc, Int]{
    def numSum(a: Doc, b: Doc): Doc = new Doc(a.tokens.foldLeft(Map[String, Int]())((m, t) => {
      b.tokens.get(t._1) match {
        case Some(c) => m.updated(t._1, t._2 + c)
        case None => m.updated(t._1, t._2)
      }
    }))
    
    def denomSum(a: Int, b: Int) = a + b
    def avg(num: Doc, denom: Int) = new Doc(num.tokens.map({t => t._1 -> t._2 / denom}))
  }
  
  implicit val docDist = new Distance[Doc]{
    private def compute(small: Map[String, Int], large: Map[String, Int]) = 
      scala.math.sqrt(
        small.foldLeft(0.0)((sum, t) => large.get(t._1) match {
          case Some(c) => sum + (c - t._2)*(c - t._2)
          case None => sum
        })
      )

    def distance(from: Doc, to: Doc): Double = (from.tokens.size, to.tokens.size) match {
      case (0, _) | (_, 0) => 0.0
      case (r, s) => if(r > s) compute(to.tokens, from.tokens) / s else compute(from.tokens, to.tokens) / r
    }
  }

  implicit val vecAverage = new Average[Vector[Double], Int, Vector[Double], Int]{
    def numSum(a: Vector[Double], b: Vector[Double]): Vector[Double] = Vector[Double](a(0) + b(0), a(1) + b(1))
    def denomSum(a: Int, b: Int) = a + b
    def avg(num: Vector[Double], denom: Int) = Vector[Double](scala.math.floor(num(0) / denom), scala.math.floor(num(1) / denom))
  }

  implicit val vecDist = new Distance[Vector[Double]]{
    def distance(from: Vector[Double], to: Vector[Double]): Double =
      scala.math.sqrt((from zip to).foldLeft(0.0)((s, t) => s + (t._1 - t._2)*(t._1 - t._2)))
  }

  def main(args: Array[String]){

    val sc = new SparkContext("local", "kmeans")
    val s1File = sc.textFile("s1.txt")

    val source = s1File.map(
      t => {
        val a = t.trim.split(" "); 
        Vector[Double]((a(0) + ".0").toDouble, (a(1) + ".0").toDouble)
      }
    )

    val initRandom: (Int, RDD[Vector[Double]]) => Seq[Vector[Double]] = 
      (n: Int, s: RDD[Vector[Double]]) => s.takeSample(false, n, System.nanoTime.toInt)
  
    val kmeans = KMeans[Vector[Double]](0.001, true, initRandom)

    source.cache()
    println(kmeans(15, 10, 1)(source).mkString("\n"))
  }
}