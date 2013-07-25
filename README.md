Onyx
====

collection of libs to create text processing -> indexing -> searching pipeline

Design Notes
============

_important initial design considerations_
- functional style programming
- easily composable operations
- easily extendible
- complete flexibility in optimization for eg: using optimization in mapReduce
- functional design follows wats in scalaz... but to keep it simple its recoded here with only the functionality desired

The whole process will be defined as a pipeline  
and each consists of stages  
stages are defined using _define-stage_  

eg:  

```
htmls = twitterStream -> extractLink -> getHTML
books = hdfsStore ->
chapters = books -> extractChapters

_define-stage_ preprocess =
  stripHTML ->
  tokenize ->
  removeStopWords ->
  lemmatize ->
  accumulate ->
  result

wordsFromTwitter = htmls -> preprocess -> mergerAll
wordsFromBooks = chapters -> preprocess -> mergeAll
```

### stage definition
- stage is a monadic function (always) ie it takes value and return an amplified or monadic value
- stage are composed using -> or andThen
- stage extends Function* from Scala eg: (Int) => M[String]
- stage definition has to extend Serializable down the chain like so that one can use it directly while dealing with RDD transformers

simple examples

```
type Sentence = String
type Token = String
//
object TokenizerHelper {
  def tokenize(sentence: Sentence): Option[Seq[Token]] = {
    ...
  }
}

class whitespaceTokenizer extends (RDD[Sentence] => Option[RDD[Seq[Token]]]) with Serializable {
  def apply(rdd: RDD[String]): Some[RDD[Seq[String]]] = {
    // rdd.map(TokenizerHelper.tokenize(_))
  }
}

// Better way
class WhitespaceTokenizer[T, U, M] extends (RDD[T] => M[RDD[U]]) with Serializable {
  def apply(RDD[U] : M)(rdd: RDD[T]): M[RDD[U]] = {
    ...
  }
}

// or
def stemming = (rdd: RDD[String]): RDD[String]) = {
  ....
}


/*
 * Important things about type T
 * 
class kMeans[T, U, M](epsilon: Float, centroids: Seq[T]) extends (RDD[T] => M[RDD[U]]) with Serializable {
  
  def apply(source: RDD[T])(implicit sim: Similarity[T], implicit avg: Average[T]) = {
    val clusters = (source
    .map(
      point => 
        centroids.reduceLeft(
          (a, b) => if(sim.similarity(point, a) < sim.similarity(point, b)) a else b 
        ) -> (point, 1)
    ).reduceByKeyToDriver({
      case ((point1, num1), (point2, num2)) => (avg.sum(point1, point2), num1 + num2)
    }).map({
      case (centroid, (pointSum, numPts)) => centroid -> pointSum / numPts
    }))

    
  }

}

```

core
----
- source
  - streams - twitter, rssfeeds, message queues
  - data store - hdfs, local filesystem
- documents -
  - each entry in RDD directly after reading from source is a doc
  - example: feeds, blogs entry, webpage, file, documents, etc
  - further operations to process (non-nlp, non-datamining) these documents like splitting book to chapters
- base to attach processing components at any point
  - more detailed implementations in _processing_
- sinks and joints
  - attaching result of one pipeline to another using various methods like Actors, http, rpc, message queues, etc
- operators for defining and constructing pipelines
  - _->_ 
      * creates a pipeline if added to a Source
      * composes or adds a stage to pipeline

### tech details
use monads  
all stages in pipeline is a monadic functions inherited from A => M[A]  

all stages in preprocess are also monadic functions  
in _define-stage_ stage we can create complex computation/pipelines for calculation  
and they can use the RDD of spark directly.  

for example  

```
_define-stage_ word_count =
	rdd
	.map ( (DocId, Doc) => Term, Int {
		for all term t in doc
			yield (term t, count 1)
	})
	.reduce ( (Term t, List[Int] counts) => Term, Int{
		sum = 0
		for all count c in counts: List[Int]
			sum = sum + c 
		yield (term t, sum)
	})
```

processing
----------
- nlp and data mining classes here


onyx and spark connection
-------------------------

