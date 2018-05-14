//package com.horizon.featureTransformers
//
//import org.apache.spark.ml.feature.ElementwiseProduct
//import org.apache.spark.mllib.linalg.Vectors
//import org.apache.spark.{SparkConf, SparkContext}
//import org.apache.spark.sql.SQLContext
//
///**
//  * Created by taos on 2017/6/25.
//  */
//object ElementwiseProductDemo {
//  def main(args: Array[String]): Unit = {
//    val conf = new SparkConf().setMaster("local").setAppName("ElementwiseProductDemo")
//    val sc = new SparkContext(conf)
//    val sqlContext = new SQLContext(sc)
//    // Create some vector data; also works for sparse vectors
//    val dataFrame = sqlContext.createDataFrame(Seq(
//      ("a", Vectors.dense(1.0, 2.0, 3.0)),
//      ("b", Vectors.dense(4.0, 5.0, 6.0)))).toDF("id", "vector")
//
//    val transformingVector = Vectors.dense(0.0, 1.0, 2.0)
//
//    println(transformingVector)
//    val transformer = new ElementwiseProduct()
//      .setScalingVec(transformingVector)
//      .setInputCol("vector")
//      .setOutputCol("transformedVector")
//
//    // Batch transform the vectors to create new column:
//    transformer.transform(dataFrame).show()
//  }
//}
