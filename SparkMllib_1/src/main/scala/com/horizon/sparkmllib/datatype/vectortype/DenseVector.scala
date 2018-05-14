package com.horizon.sparkmllib.datatype.vectortype

import org.apache.spark.mllib.linalg.{Vector,Vectors}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by taos on 2017/6/11.
  */
object DenseVectorDemo {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setMaster("local[4]").setAppName("DenseVectorDemo")
    //设置APP 的name，设置Local 模式的CPU资源
    val sc = new SparkContext(conf)
    val dv: Vector = Vectors.dense(1.0, 0.0, 3.0)


    println(dv)
  }

}
