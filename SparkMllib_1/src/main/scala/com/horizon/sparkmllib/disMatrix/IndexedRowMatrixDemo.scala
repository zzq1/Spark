package com.horizon.sparkmllib.disMatrix

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.linalg.distributed.{IndexedRow, IndexedRowMatrix, RowMatrix}
import org.apache.spark.rdd.RDD

/**
  * Created by taos
  */
object IndexedRowMatrixDemo {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("RowMatrixDemo").setMaster("local")
    val sc = new SparkContext(conf)
    val rows: RDD[IndexedRow] = sc.parallelize(
      Seq(
        IndexedRow(0, Vectors.dense(1.0, 10.0, 100.0)),
        IndexedRow(1,Vectors.dense(2.0, 20.0, 200.0)),
        IndexedRow(2,Vectors.dense(5.0, 33.0, 366.0)))
    )
    // Create an IndexedRowMatrix from an RDD[IndexedRow].
    val mat: IndexedRowMatrix = new IndexedRowMatrix(rows)

    // Get its size.
    val m = mat.numRows()
    val n = mat.numCols()
    println(m)
    println(n)
    // Drop its row indices.
    val rowMat: RowMatrix = mat.toRowMatrix()
    println(rowMat)
  }

}
