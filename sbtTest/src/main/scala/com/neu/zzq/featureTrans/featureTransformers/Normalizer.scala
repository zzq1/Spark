package com.neu.zzq.featureTrans.featureTransformers

import org.apache.spark.ml.feature.Normalizer
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext

/**
  * Created by Administrator on 2017/6/21.
  */
object NormalizerDemo {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("NormalizerDemo")
    val sc =  new SparkContext(conf)
    val sqlContext =  new SQLContext(sc)
    val root = NormalizerDemo.getClass.getResource("/")
    val dataFrame = sqlContext.read.format("libsvm")
      .load(root + "sample_libsvm_data.txt")

    // Normalize each Vector using $L^1$ norm.
    val normalizer = new Normalizer()
      .setInputCol("features")
      .setOutputCol("normFeatures")
      .setP(2.0) //求LN范数

    val l1NormData = normalizer.transform(dataFrame)
    l1NormData.show()

    // Normalize each Vector using $L^\infty$ norm.
    val lInfNormData = normalizer.transform(dataFrame, normalizer.p -> Double.PositiveInfinity)
    lInfNormData.take(10).foreach(println)
  }

}
