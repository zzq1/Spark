package com.neu.zzq.featureTrans.featureTransformers

import org.apache.spark.ml.feature.StandardScaler
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext

/**
  * Created by taos on 2017/6/21.
  */
object StandardScalerDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("StandardScalerDemo")
    val sc =  new SparkContext(conf)
    val sqlContext =  new SQLContext(sc)
    val root = StandardScalerDemo.getClass.getResource("/")
    val dataFrame = sqlContext.read.format("libsvm").load(root + "sample_libsvm_data.txt")

    val scaler = new StandardScaler()
      .setInputCol("features")
      .setOutputCol("scaledFeatures")
      .setWithStd(true)
      .setWithMean(false)

    // Compute summary statistics by fitting the StandardScaler.
    val scalerModel = scaler.fit(dataFrame)

    // Normalize each feature to have unit standard deviation.
    val scaledData = scalerModel.transform(dataFrame)
    scaledData.show()
  }

}
