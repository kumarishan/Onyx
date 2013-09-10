import sbt._
import sbt.Classpaths.publishTask
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._
import com.typesafe.sbt.SbtStartScript

object OnyxBuild extends Build {

	lazy val root = Project("root", file("."), settings = rootSettings) aggregate(core, examples, processing)
	lazy val core = Project("core", file("core"), settings = coreSettings)
	lazy val examples = Project("examples", file("examples"), settings = examplesSettings) dependsOn (core) dependsOn (processing) dependsOn (indexing) dependsOn (mining)
	lazy val processing = Project("processing", file("processing"), settings = processingSettings) dependsOn (core)
	lazy val indexing = Project("indexing", file("indexing"), settings = indexingSettings) dependsOn (core)
	lazy val mining = Project("mining", file("mining"), settings = miningSettings) dependsOn (core) dependsOn (processing)

	def commonSettings = Defaults.defaultSettings ++ Seq(
		organization := "io.onyx",
		version := "0.0.1",
		scalaVersion := "2.9.3",
		scalacOptions := Seq("-unchecked", "-optimize", "-deprecation"),
		retrieveManaged := true,

		fork := true,
		javaOptions += "-Xmx2500m",

		resolvers ++= Seq(
			"Akka Repository" at "http://repo.akka.io/releases",
			"Spray Repository" at "http://repo.spray.cc/"
		),
		publishMavenStyle := true
	)

	def coreSettings = commonSettings ++ Seq(
		name := "onyx-core",
		libraryDependencies ++= Seq(
			"org.spark-project" % "spark-core_2.9.3" % "0.7.3",
			"org.apache.tika" % "tika-core" % "1.4",
			"org.apache.tika" % "tika-parsers" % "1.4",
			"org.apache.pdfbox" % "pdfbox" % "1.8.2"
		)
	) ++ assemblySettings ++ extraAssemblySettings

	def rootSettings = commonSettings ++ Seq(
		publish := {}
	)

	def processingSettings = commonSettings ++ Seq(
		name := "onyx-processing",
		libraryDependencies ++= Seq(
			"org.spark-project" % "spark-core_2.9.3" % "0.7.3",
			"org.apache.opennlp" % "opennlp-tools" % "1.5.3",
			"org.apache.opennlp" % "opennlp-maxent" % "1.5.3",
			"edu.stanford.nlp" % "stanford-corenlp" % "3.2.0",
			"com.twitter" % "algebird-core_2.9.2" % "0.1.13"
		)
	) ++ assemblySettings ++ extraAssemblySettings

	def indexingSettings = commonSettings ++ Seq(
		name := "onyx-indexing",
		libraryDependencies ++= Seq(
			"org.spark-project" % "spark-core_2.9.3" % "0.7.3",
			"org.apache.lucene" % "lucene-core" % "4.4.0",
			"org.apache.lucene" % "lucene-analyzers-common" % "4.4.0",
			"com.twitter" % "algebird-core_2.9.2" % "0.1.13"
		)
	) ++ assemblySettings ++ extraAssemblySettings

	def miningSettings = commonSettings ++ Seq(
		name := "onyx-mining",
		libraryDependencies ++= Seq(
			"com.twitter" % "algebird-core_2.9.2" % "0.1.13"
		)
	) ++ assemblySettings ++ extraAssemblySettings

	def examplesSettings = commonSettings ++ Seq(
		name := "onyx-examples",
		libraryDependencies ++= Seq(
			"org.apache.hadoop" % "hadoop-core" % "1.2.0",
			"edu.stanford.nlp" % "stanford-corenlp" % "3.2.0",
			"edu.washington.cs.knowitall.stanford-corenlp" % "stanford-postag-models" % "1.3.5"
		)
	) ++ assemblySettings ++ extraAssemblySettings ++ SbtStartScript.startScriptForClassesSettings

	def extraAssemblySettings() = Seq(test in assembly := {}) ++ Seq(
    mergeStrategy in assembly := {
      case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
      case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
      case "reference.conf" => MergeStrategy.concat
      case _ => MergeStrategy.first
    }
  )
}