resolvers += Resolver.url("artifactory", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Spray Repository" at "http://repo.spray.cc/"

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.8.5")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.3")