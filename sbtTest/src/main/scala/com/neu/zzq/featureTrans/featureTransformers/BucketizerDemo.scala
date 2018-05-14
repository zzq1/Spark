package com.neu.zzq.featureTrans.featureTransformers

import org.apache.spark.ml.feature.Bucketizer
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext

/**
  * Created by taos on 2017/6/21.
  * Bucketizer将一列连续的特征转换为特征区间
  */
object BucketizerDemo {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("BucketizerDemo")
    val sc =  new SparkContext(conf)
    val sqlContext =  new SQLContext(sc)
    val splits = Array(Double.NegativeInfinity, -0.5, 0.0, 0.5, Double.PositiveInfinity)

    val data = Array(-0.5, -0.3, 0.0, 0.2)
    val dataFrame = sqlContext.createDataFrame(data.map(Tuple1.apply)).toDF("features")

    val bucketizer = new Bucketizer()
      .setInputCol("features")
      .setOutputCol("bucketedFeatures")
      .setSplits(splits)

    // Transform original data into its bucket index.
    val bucketedData = bucketizer.transform(dataFrame)
    bucketedData.show()
  }

}
