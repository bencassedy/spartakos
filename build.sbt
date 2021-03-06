name := "spartakos"
version := "1.0"
scalaVersion := "2.11.8"
val sparkVersion = "2.0.0"

libraryDependencies += "org.apache.spark" % "spark-core_2.11" % sparkVersion
libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % sparkVersion
libraryDependencies += "org.apache.spark" % "spark-mllib_2.11" % sparkVersion
libraryDependencies += "org.mongodb.mongo-hadoop" % "mongo-hadoop-core" % "1.4.1" % "provided"
libraryDependencies += "org.mongodb" % "mongo-java-driver" % "3.1.0" % "provided"
libraryDependencies += "com.github.scopt" %% "scopt" % "3.3.0"
libraryDependencies += "com.typesafe" % "config" % "1.2.1"
libraryDependencies += "io.continuum.bokeh" % "bokeh_2.11" % "0.7"
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0"
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0" classifier "models"
libraryDependencies += "com.holdenkarau" % "spark-testing-base_2.11" % "2.0.1_0.4.7" % "test"

// bump up some of the JVM defaults
javaOptions ++= Seq("-Xms1024M", "-Xmx4096M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled")

// build an assembly jar
assemblyJarName in assembly := "spartakos.jar"
mainClass in assembly := Some("EnronSpark")
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*)                 => MergeStrategy.discard
  case "results.txt"                                 => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

// enable deployment of artifact to s3 bucket
import S3._

s3Settings

mappings in upload := Seq((new java.io.File("target/scala-2.11/enron-spark.jar"), "enron-spark.jar"))
host in upload := "bencassedyenron.s3.amazonaws.com"
credentials += Credentials(Path.userHome / ".aws/s3credentials")