name := "SparkMllib_1"

version := "1.0"


scalaVersion := "2.10.6"
lazy val spark = "1.6.3"
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.10" % spark,
  "org.apache.spark" % "spark-sql_2.10" % spark,
  "org.apache.spark" % "spark-mllib_2.10" % spark,
  "com.databricks" % "spark-csv_2.10" % "1.4.0",
  "org.apache.spark" % "spark-streaming-kafka_2.10" % spark,
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1"% "test"
)
        