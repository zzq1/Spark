package com.neu.zzq.featureTrans.featureTransformers

import org.apache.spark.ml.feature.MinMaxScaler
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext

/**
  * Created by Administrator on 2017/6/21.
  */
object MinMaxScalerDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("MinMaxScalerDemo")
    val sc =  new SparkContext(conf)
    val sqlContext =  new SQLContext(sc)
    val root = MinMaxScalerDemo.getClass.getResource("/")
    val dataFrame = sqlContext.read.format("libsvm").load(root + "sample_libsvm_data.txt")
    val scaler = new MinMaxScaler()
      .setInputCol("features")
      .setOutputCol("scaledFeatures")

    // Compute summary statistics and generate MinMaxScalerModel
    val scalerModel = scaler.fit(dataFrame)

    // rescale each feature to range [min, max].
    val scaledData = scalerModel.transform(dataFrame)
    scaledData.collect().foreach(println)
  }

}
