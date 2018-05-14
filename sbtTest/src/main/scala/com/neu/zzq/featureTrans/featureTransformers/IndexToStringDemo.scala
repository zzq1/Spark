package com.neu.zzq.featureTrans.featureTransformers

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.ml.feature.{IndexToString, StringIndexer}
import org.apache.spark.sql.SQLContext
/**
  * Created by taos on 2017/6/21.
  */
object IndexToStringDemo {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("IndexToStringDemo")
    val sc =  new SparkContext(conf)
    val sqlContext =  new SQLContext(sc)
    val df = sqlContext.createDataFrame(Seq(
      (0, "a"),
      (1, "b"),
      (2, "c"),
      (3, "a"),
      (4, "a"),
      (5, "c")
    )).toDF("id", "category")

    val indexer = new StringIndexer()
      .setInputCol("category")
      .setOutputCol("categoryIndex")
      .fit(df)
    val indexed = indexer.transform(df)
    indexed.show()
    val converter = new IndexToString()
      .setInputCol("categoryIndex")
      .setOutputCol("originalCategory")

    val converted = converter.transform(indexed)
    converted.select("id", "originalCategory").show()
  }

}
