name := "SparkML_4"

version := "1.0"

scalaVersion := "2.11.11"

lazy val spark = "2.2.0"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core" % spark,
  "org.apache.spark" % "spark-sql" % spark,
  "org.apache.spark" % "spark-mllib" % spark,
  "com.databricks" % "spark-csv_2.10" % "1.4.0",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1"% "test"
)