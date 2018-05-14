package com.horizon.sparkmllib.datatype.vectortype

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

/**
  * Created by taos on 2017/6/11.
  */
object LabelPointDemo {

  def main(args: Array[String]): Unit = {
    val pos = LabeledPoint(1.0, Vectors.dense(1.0, 0.0, 3.0))

    // Create a labeled point with a negative label and a sparse feature vector.
    val neg = LabeledPoint(0.0, Vectors.sparse(3, Array(0, 2), Array(1.0, 3.0)))
  }

}
