package com.horizon.sparkmllib.disMatrix

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.linalg.{Vectors,Vector}
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.rdd.RDD

/**
  * Created by taos on 2017/6/22.
  */
object RowMatrixDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("RowMatrixDemo").setMaster("local")
    val sc = new SparkContext(conf)
    val rows: RDD[Vector] = sc.parallelize(
      Seq(
        Vectors.dense(1.0, 10.0, 100.0),
        Vectors.dense(2.0, 20.0, 200.0),
        Vectors.dense(5.0, 33.0, 366.0))
    )
    // Create a RowMatrix from an RDD[Vector].
    val mat: RowMatrix = new RowMatrix(rows)

    // Get its size.
    val m = mat.numRows()
    val n = mat.numCols()

    // QR decomposition
    val qrResult = mat.tallSkinnyQR(true)
  }
}
