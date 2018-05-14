package com.horizon.sparkmllib.statistics

import org.apache.spark.mllib.stat.KernelDensity
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by taos on 2017/6/16.
  */
object KernelDensityEstimationDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("HypothesisTesting")
    conf.set("spark.driver.extraJavaOptions",
      " -XX:PermSize=256M -XX:MaxPermSize=256M")
    val sc = new SparkContext(conf)
    val data: RDD[Double] = sc.makeRDD(List(1.0,2.0,3.0,4.0,5.0))
    // Construct the density estimator with the sample data and a standard deviation for the Gaussian
    // kernels
    val kd = new KernelDensity()
      .setSample(data)
    //其中setBandwidth表示高斯核的宽度，为一个平滑参数，可以看做是高斯核的标准差。
    //构造了核密度估计kd，就可以对给定数据数据进行核估计：
      .setBandwidth(3.0)

    // Find density estimates for the given values
    val densities = kd.estimate(Array(-1.0, 2.0, 5.0))
  }
}
