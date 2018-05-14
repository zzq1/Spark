package com.neu.zzq.featureTrans.featureTransformers

import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by taos on 2017/6/21.
  */
object StringIndexerDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("PCADemo")
    val sc =  new SparkContext(conf)
    val sqlContext =  new SQLContext(sc)
    val df = sqlContext.createDataFrame(
      Seq((0, "a"), (1, "b"), (2, "c"), (3, "a"), (4, "a"), (5, "c"))
    ).toDF("id", "category")

    val df2 = sqlContext.createDataFrame(
      Seq((0, "a"), (1, "b"), (2, "c"), (3, "a"), (4, "d"), (5, "c"))
    ).toDF("id", "category")

    val indexer = new StringIndexer()
      .setInputCol("category")
      .setOutputCol("categoryIndex")
    //遇到没见过的字符串直接过滤掉
    indexer.setHandleInvalid("skip")
    val indexed = indexer.fit(df).transform(df2)
    indexed.show()
  }
}
