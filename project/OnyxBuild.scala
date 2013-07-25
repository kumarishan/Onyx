import sbt._
import sbt.Classpaths.publishTask
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object OnyxBuild extends Build {

	lazy val root = Project("root", file("."), settings = rootSettings) aggregate(core, examples, processing)
	lazy val core = Project("core", file("core"), settings = coreSettings)
	lazy val examples = Project("examples", file("examples"), settings = examplesSettings) dependsOn (core) dependsOn (processing)
	lazy val processing = Project("processing", file("processing"), settings = processingSettings) dependsOn (core)

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
			"org.spark-project" % "spark-core_2.9.3" % "0.7.3"
		) 
	) ++ assemblySettings ++ extraAssemblySettings

	def rootSettings = commonSettings ++ Seq(
		publish := {}
	)

	def processingSettings = commonSettings ++ Seq(
		name := "onyx-processing",
		libraryDependencies ++= Seq(
			"org.spark-project" % "spark-core_2.9.3" % "0.7.3"
		)
	) ++ assemblySettings ++ extraAssemblySettings

	def examplesSettings = commonSettings ++ Seq(
		name := "onyx-examples"
	) ++ assemblySettings ++ extraAssemblySettings

	def extraAssemblySettings() = Seq(test in assembly := {}) ++ Seq(
    mergeStrategy in assembly := {
      case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
      case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
      case "reference.conf" => MergeStrategy.concat
      case _ => MergeStrategy.first
    }
  )
}