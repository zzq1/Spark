package com.horizon.mllib.evaluation

import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.mllib.regression.LinearRegressionWithSGD
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by taos on 2017/6/20.
  */
object RegressionMetricsDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[4]").setAppName("RegressionMetricsDemo")
    val sc = new SparkContext(conf)

    val root = RegressionMetricsDemo.getClass.getResource("/")
    // Load the data
    val data = MLUtils.loadLibSVMFile(sc, root + "sample_linear_regression_data.txt").cache()

    // Build the model
    val numIterations = 100
    val model = LinearRegressionWithSGD.train(data, numIterations)

    // Get predictions
    val valuesAndPreds = data.map{ point =>
      val prediction = model.predict(point.features)
      (prediction, point.label)
    }

    // Instantiate metrics object
    val metrics = new RegressionMetrics(valuesAndPreds)

    // Squared error
    println(s"MSE = ${metrics.meanSquaredError}")
    println(s"RMSE = ${metrics.rootMeanSquaredError}")

    // R-squared
    println(s"R-squared = ${metrics.r2}")

    // Mean absolute error
    println(s"MAE = ${metrics.meanAbsoluteError}")

    // Explained variance 返回平均回归和的平方
    println(s"Explained variance = ${metrics.explainedVariance}")
  }
}
