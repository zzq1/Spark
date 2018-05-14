package com.horizon.mllib.classification2Regression.DecisionTree

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.rdd.RDD

/**
  * Created by taos on 2017/6/23.
  */
object TreeClassification {
  def main(args: Array[String]): Unit = {
    val conf =  new SparkConf().setAppName("LogisticRegressionDemo").setMaster("local")
    val sc = new SparkContext(conf)
    val root = TreeClassification.getClass.getResource("/")
    val data = sc.textFile(root + "iris.data")
    val parsedData = data.map { line =>
      val parts = line.split(',')
      LabeledPoint(if(parts(4)=="Iris-setosa") 0.toDouble else if (parts(4)
        =="Iris-versicolor") 1.toDouble else
      2.toDouble, Vectors.dense(parts(0).toDouble,parts(1).toDouble,parts
    (2).toDouble,parts(3).toDouble))
    }

    parsedData.foreach { x => println(x) }
    val splits = parsedData.randomSplit(Array(0.7, 0.3))
    val (trainingData, testData): Tuple2[RDD[LabeledPoint],RDD[LabeledPoint]] = (splits(0), splits(1))
    val numClasses = 3
    val categoricalFeaturesInfo = Map[Int, Int]()
    val impurity = "gini"
    val maxDepth = 5
    val maxBins = 32
    val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo, impurity, maxDepth, maxBins)
    val labelAndPreds = testData.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }
    println("Learned classification tree model:\n" + model.toDebugString)
  }
}
