package com.horizon.sparkmllib.disMatrix
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.linalg.distributed.{CoordinateMatrix, IndexedRow, MatrixEntry}
import org.apache.spark.rdd.RDD
/**
  * Created by taos
  */
object CoordinateMatrixDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("CoordinateMatrixDemo").setMaster("local")
    val sc = new SparkContext(conf)
    /**
      *  1 2
      *  3 5
      */
    val entries: RDD[MatrixEntry] = sc.parallelize(
      Seq(
        MatrixEntry(0, 0, 1),
        MatrixEntry(0,1,2),
        MatrixEntry(1,0,3),
        MatrixEntry(1,1,5))
    )
    // Create a CoordinateMatrix from an RDD[MatrixEntry].
    val mat: CoordinateMatrix = new CoordinateMatrix(entries)

    // Get its size.
    val m = mat.numRows()
    val n = mat.numCols()
    println(m)
    println(n)
    // Convert it to an IndexRowMatrix whose rows are sparse vectors.
    val indexedRowMatrix = mat.toIndexedRowMatrix()
  }
}
