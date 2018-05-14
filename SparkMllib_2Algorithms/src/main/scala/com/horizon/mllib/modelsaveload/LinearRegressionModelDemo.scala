package com.horizon.mllib.modelsaveload

import com.horizon.mllib.classification2Regression.SVMWithSGDDemo
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.{LabeledPoint, LinearRegressionModel, LinearRegressionWithSGD}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by taos on 2017/6/20.
  */
object LinearRegressionModelDemo {
  def main(args: Array[String]): Unit = {
    // Load and parse the data
    val conf =  new SparkConf().setAppName("LeanerRegressionModelDemo").setMaster("local[4]")
    val sc = new SparkContext(conf)
    //val root = SVMWithSGDDemo.getClass.getResource("/")
    val root ="C:/Users/zzq/IdeaProjects/SparkMllib_2Algorithms/resource/"
    val data = sc.textFile(root + "ridge-data/lpsa.data")
    val parsedData = data.map { line =>
      val parts = line.split(',')
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
    }.cache()
    // Building the model
    val numIterations = 100
    val stepSize = 0.00000001
    val model = LinearRegressionWithSGD.train(parsedData, numIterations, stepSize)
    // Evaluate model on training examples and compute training error
    val valuesAndPreds = parsedData.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }
    val MSE = valuesAndPreds.map{case(v, p) => math.pow((v - p), 2)}.mean()
    println("training Mean Squared Error = " + MSE)
    model.save(sc, "lineaner")
    val sameModel = LinearRegressionModel.load(sc, "lineaner")

    val valuesAndPreds_ = parsedData.map { point =>
      val prediction_ = sameModel.predict(point.features)
      (point.label, prediction_)
    }
    valuesAndPreds_.collect().foreach(println)
  }
}
