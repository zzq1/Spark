package com.neu.zzq.featureTrans.featureTransformers

import org.apache.spark.ml.feature.Binarizer
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by taos on 2017/6/21.
  */
object BinarizerDemo {

  def main(args: Array[String]): Unit = {
    val data = Array((0, 0.1), (1, 0.8), (2, 0.2))
    val conf = new SparkConf().setMaster("local").setAppName("BinarizerDemo")
    val sc =  new SparkContext(conf)
    val sqlContext =  new SQLContext(sc)
    val dataFrame = sqlContext.createDataFrame(data).toDF("label", "feature")
    dataFrame.show()
    val binarizer: Binarizer = new Binarizer()
      .setInputCol("feature")
      .setOutputCol("binarized_feature")
      .setThreshold(0.5)
    //Binarizer参数有输入、输出以及阀值。特征值大于阀值将映射为1.0，特征值小于等于阀值将映射为0.0。
    val binarizedDataFrame = binarizer.transform(dataFrame)
    binarizedDataFrame.show()
    val binarizedFeatures = binarizedDataFrame.select("binarized_feature")
    binarizedFeatures.collect().foreach(println)
  }

}
