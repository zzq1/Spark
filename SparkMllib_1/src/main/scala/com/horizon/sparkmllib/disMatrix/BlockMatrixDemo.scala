package com.horizon.sparkmllib.disMatrix

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.linalg.distributed.{BlockMatrix, CoordinateMatrix, MatrixEntry}
import org.apache.spark.rdd.RDD

/**
  * Created by taos
  */
object BlockMatrixDemo {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("CoordinateMatrixDemo").setMaster("local")
    val sc = new SparkContext(conf)
    val entries: RDD[MatrixEntry] = sc.parallelize(
      Seq(
        MatrixEntry(0, 0, 1),
        MatrixEntry(0,1,2),
        MatrixEntry(1,0,3),
        MatrixEntry(1,1,5),
      MatrixEntry(2,1,5))
    )
    // Create a CoordinateMatrix from an RDD[MatrixEntry].
    val coordMat: CoordinateMatrix = new CoordinateMatrix(entries)
    // Transform the CoordinateMatrix to a BlockMatrix
    val matA: BlockMatrix = coordMat.toBlockMatrix().cache()

    // Validate whether the BlockMatrix is set up properly. Throws an Exception when it is not valid.
    // Nothing happens if it is valid.
    println(matA.toLocalMatrix())
    matA.validate()

    // Calculate A^T A.
    val ata = matA.transpose.multiply(matA)
    println(ata.toLocalMatrix())
  }

}
