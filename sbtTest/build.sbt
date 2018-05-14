name := "sbtTest"

version := "1.0"

scalaVersion := "2.11.11"
lazy val spark = "2.2.0"
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.11" % spark,
  "org.apache.spark" % "spark-sql_2.11" % spark,
  "org.apache.spark" % "spark-mllib_2.11" % spark,
  "com.databricks" % "spark-csv_2.10" % "1.4.0",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1"% "test",
  "org.ansj" % "ansj_seg" % "5.1.5",
  "org.nlpcn" % "nlp-lang" % "1.7.7",
  "com.github.scopt" %% "scopt" % "3.7.0"
)







