package com.neu.zzq.featureTrans.featureTransformers

import org.apache.spark.ml.feature.QuantileDiscretizer
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by taos.
  */
object QuantileDiscretizerDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("QuantileDiscretizerDemo")

    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    val data = Array((0, 18.0), (1, 19.0), (2, 8.0), (3, 5.0), (4, 2.2))
    import sqlContext.implicits._
    val df = sc.parallelize(data).toDF("id", "hour")

    val discretizer = new QuantileDiscretizer()
      .setInputCol("hour")
      .setOutputCol("result")
      .setNumBuckets(3)

    val result = discretizer.fit(df).transform(df)
    result.show()
  }
}
