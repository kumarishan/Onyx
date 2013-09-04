Onyx
====
__[Testing and trying different desing .. definitely not to be used yet]__

collection of libs to create text processing -> indexing -> searching pipeline
look at design notes below as what you can expect

Design Notes
============

__updated 5th Sept 2010__

Sequence has following methods
```
iterator: Iterator[]
+(value: )
++(values: Iterator[])

__updated 14th Aug 2013__

Sequence.... it stores and iterable data structure. Each item in the iterator can be of any type.  
Idea is to allow each kind of Sequence to store in their own data structure. For example.  

```
tokenization -> bagOfWords -> featureHashing
  = Array[String]   = Map[String, Int]  = SparseVector[Int] \(real ds\)
  = TokenSequence   = WordCountSequence = FeatureHashSequence \(container\)

sentenceSplit -> tokenization -> bagOfWords -> featureHashing
  = Array[String]     = Array[String]  = Map[String, Int]  = SparseVector[Int] \(read ds\)
  = SentenceSequence  = TokenSequence  = WordCountSequence = FeatureHashSequence \(container\)
```

With each item in the sequence there is a label attached. For example.. in case of POS, each token will have a POS tag.  
A label is defined by its type and may or maynot have a value. For example in POS taggger each label is of Type POSTag and there values could be actual Tag like NN, DT, etc.
While creating a new Sequence out of old through transformation. One can choose to preserve the old Label for example..  

```
val result =
 sentenceSplit -> tokenization(preserveLabel) -> posTag(preserveLabel)
  = Array[String]     = Array[String]             = Array[String]
  = SentenceSequence  = TokenSequence             = TokenSequence
  = Sentence          = Token, Sentence           = Token, Sentence, POSTag (labels)

result -> bagOfWords
            = Map[String, Int]
            = WordCountSequence
            = Token
```
what this means is that at the end. I can iterate through result using any sequence label.. for example I still want to iterate through Token, I can call result.iterator[Token] .. this will iterate through labels.. or result.iterator[Sentence] or result.iterator[POSTag] . If the corresponding Label is not preserved then the iterator returns and empty Iterator

What label can be preserved is upto the transformer. for example the bagOfWords cannot preserver Sentence and also the Token label will also have a different behavior

_now the question is how the previous token is stored ?_
so first of all how we create a new Sequence by operating on previous and updating.  
for eg tokenization takes any Sequence over String, iterates through them and then create new TokenSequence, which is a sequence over string itself.
so if I have a tokenizer that takes a string and outputs array of String ie tokens..  

```
def tokenizer(s: String): Array[String] = ....
inputSequence.iterator.foldLeft(new TokenSequence)((s, t) => s ++ tokenizer(s))
 - <: Sequence[String]             - a new tokenSequence      - keep accumulating to the tokenSequence
//or
val tokens: Array[String] = inputSequence.iterator.flatMap(t => tokenizer(t))
new TokenSequence(tokens) // appears to be better in peformance
```

well this is all good.. now if we want to store the previous label..

_serialization_
each type of sequence may register a custom serialization using Kryo

__updated 13th Aug 2013__  
_documents_ - every process is done on documents  
Document uses Map[String, SegmentSeq] \(immutable\)  
example: Book <: Document  
- Book.parse(with\_chapters) or Book.parse(without\_chapters)
for Book various fields could be authors, etc.. each field can be optionally tokenizable, used as feature.. etc..  
for example.. while in clustering Book features that can be used .. authors, content's feature, important tags, etc..  
in the above case every field will have its own feature vector(or any DS).. and thus when computing similarity of document similarity has to be calculated with correspding fields only.. and thus can be furhter combined to one score using weights..


_tokenization_  
at every segmentation or tokenization we produce another Seq of Tokens for each previous Seq of tokens
for example - Book <: Document -> book.chapters -> paragraphs -> sentences -> tokens  
here Book which is a document has multiple field... it may or maynot have chapters..  
if it already is not subdivided into chapters.. then book.chapters uses field named content and get the ContentSeq which just now contains only one string ie the whole content. book.chapters will update this field content to ChapterSeq.

next when paragraphs is applied.. it divided each chapter into into its paragraph...
each of this operation returns the book only..  
val book: Book = Book -> book.chapters -> paragraphSegmenter -> sentenceTokenizer -> ptbTokenizer..

a simple example first...

val sentence = "This is the first sentence. This is the sencond sentence."

sentence -> sentenceTokenizer

Token alias String
TokenSeq implements scala.collection.Seq[Token] // cannot be lazy considering the distributed environment
Annotation -> 
AnnotatedTokenSeq[AnnotationType]


__updated 7th Aug 2013__  

```
onyx.core
  Chainable
  syntax
    ChainSyntax
  tools
    StoreToHDFS
  doc
    Doc
    Book
    WebPage

onyx.processing
  tokenize
    Tokenizer
    WhitespaceTokenizer
    RegexTokenizer
    PTBTokenizer
    Tokenizable
  postag
    POSTagger
    MaxEntPOSTagger
    BrillTagger
    RegexTagger
    NGramTagger
    AffixTagger
    NaiveBayesTagger
    TntTagger
  ner
    NamedEntityRecognition
  concordance
  featurize
    FeatureHashing
    TfIdfWeighting
  analyze
    StopwordFiltering

onyx.mining
  clustering
    KMeans
    HierarchicalClustering
  extraction
    RAKE
  modelling
    LDA
    

onyx.indexing
  LuceneIndex

onyx.searching

```
__updated 3rd Aug 2013__  
_(coded)_  

_design modification_  
the master or chaining orperator
- |@| _(see if it compiles to the monadic operator)_ is also an alias for andThen
- there are three variants of |@|
  - ```|@|```
  - ```|@| op(..) like |@| map(..), |@| flatMap(..)```
    - rhs is always ```T => U```
  - ```|@| parallelize(..) to create RDDs``` _just a syntactic sugar to sc.parallelize_ 
- it operates on following kind of functions
  - ```T => U```
  - ```RDD[T] => RDD[U]```
  - ```RDD[T] => U```
  - ```RDD[T]```
  - ```T```
- valid application of the operator on above types
  - ```RDD[T] => RDD[U] |@| op(U => V)```
  - ```T => U |@| U => V```
  - ```T => U |@| op(U => V)```
  - ```RDD[T] => RDD[U] |@| RDD[U] => RDD[V]```
  - ```T => U |@| parallelize(RDD[U] => RDD[V])```
  - ```T |@| parallelize(RDD[T] => RDD[V])```
  - ```RDD[T] |@| RDD[T] => RDD[V]```

_spark process_ (RDD[T] => RDD[V])  
spark process they always take RDD as input hence they are always combined using |@|

_spark streaming process_ (to be added eventually)  

_atomic process_ (T => V)  
atomic process are those tat is to be applied to each RDD entries they dont work on RDDs but on data points itself. Hence they dont take RDDs as parameter.  
Thus any proper third party apis can also be encapsulated inside a function and used
But inorder to work with RDDs they combine using RDD operations like
flatmap or map. Can be another other too

_this way tomorrow we can een have storm process or hadoop process added too_  

_chaining of atomic process_
- ```tokenize |@| map(postag)``` in this tokenize output is converted to RDD and then map transformation is applied to the RDD and fed to postag (another atomic) process
- ```tokenize |@| postag``` in this case no RDD is not involved in between tokenize and postag. The result of tokenize is directly fed to postag
- ```tokenize |@| kmeans``` if either of the parameters of |@| is not a atomic process then ouput of the atomic process is RDD. In this case tokenize output is converted to RDD and then fed it to kmeans using Map as default
- full fledged example

```
Book.hdfsSource("....") |@|  // read books from a sequence file in hdfs
  Book.chapters |@|          // split the book into chapters
  map(tokenize) |@|          // tokenize each chapter 
  postag |@|                 // pos tag each chapter tokens
  {filterPosTag(_)} |@|      // custom function to filter the postags
  map(featureHashing) |@|    // use hashing trick to create sparse vector feature hash
  Book.combineChapters |@|   // combine features of each chapters into one book
  kmeans |@|                 // do kmeans clustering on the books
  toHDFS                     // store the cluster result to HDFS
```

|@| chain operator when used to combine them can be

__old__

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
 * /
class kMeans[T, U, M](epsilon: Float, centroids: Seq[T]) extends (RDD[T] => M[RDD[U]]) with Serializable {
  
  // as defined in spark-KMeans by @ankurdave
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

