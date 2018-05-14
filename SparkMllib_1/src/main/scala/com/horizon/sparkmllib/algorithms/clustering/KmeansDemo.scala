package com.horizon.sparkmllib.algorithms.clustering

import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by taos on 2017/6/16.
  */
object KmeansDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setMaster("local[4]").setAppName("DenseVectorDemo")
    //设置APP 的name，设置Local 模式的CPU资源
    val sc = new SparkContext(conf)
    val root = KmeansDemo.getClass.getResource("/")
    val data = sc.textFile(root + "kmeans_data.txt")
    val parsedData = data.map(s => Vectors.dense(s.split(' ').map(_.toDouble))).cache()

    // Cluster the data into two classes using KMeans
    val numClusters = 2
    val numIterations = 20
    val clusters = KMeans.train(parsedData, numClusters, numIterations)
val s: Array[linalg.Vector] = clusters.clusterCenters
    // Evaluate clustering by computing Within Set Sum of Squared Errors
    val WSSSE = clusters.computeCost(parsedData)
    println("Within Set Sum of Squared Errors = " + WSSSE)

    // Save and load model
    clusters.save(sc, "myModelPath")
    clusters.toPMML("myModelPath")
    val sameModel = KMeansModel.load(sc, "myModelPath")
  }
}
