package com.horizon.ml.classification

import com.horizon.ml.classification.DecisionTreeDemo.temp
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.DecisionTreeClassifier
import org.apache.spark.ml.evaluation.{MulticlassClassificationEvaluator, RegressionEvaluator}
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.regression.{DecisionTreeRegressionModel, DecisionTreeRegressor}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}

/**
  * Created by taos on 2017/6/27.
  */
object DecisionTreePipelineDemo {
  case class temp(a: String, b: String, c: Int, d: String)
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("PipelineDemo").set("spark.driver.extraJavaOptions",
      "-Xms1512m -Xmx2048m -XX:PermSize=512M -XX:MaxPermSize=512M")
    val sc =  new SparkContext(conf)
    val sqlContext =  new SQLContext(sc)
    val root = TreeRegression.getClass.getResource("/")
    // Load the data stored in LIBSVM format as a DataFrame.
    val rddString = sc.textFile(root + "decisionTree.txt")
    val tRDD = rddString.map(_.split(",")).map(p => temp(p(0).toString, p(1).toString,
      p(2).toInt, p(3).toString))
    import sqlContext.implicits._
    val trainRDD = tRDD.toDF
    //1
    val indexer1 = new StringIndexer().setInputCol("a").setOutputCol("a_")

    val indexer2 = new StringIndexer().setInputCol("b").setOutputCol("b_")

    val indexer3 = new StringIndexer().setInputCol("d").setOutputCol("label")

//    val indexeda = indexer1.fit(trainRDD,paramMapa).transform(trainRDD)
//    val indexedc = indexer2.fit(indexeda,paramMapc).transform(indexeda)
//    val trainedRDD = indexer3.fit(indexedc,paramMapd).transform(indexedc)
    //2
    val assembler = new VectorAssembler()
      .setInputCols(Array("c", "a_","b_"))
      .setOutputCol("features")
//    val assTrain = assembler.transform(trainedRDD)
//    assTrain.show
    //3
    val dt = new DecisionTreeClassifier()
      .setLabelCol("label")
      .setFeaturesCol("features")
    val pipeline = new Pipeline()
      .setStages(Array(indexer1,indexer2,indexer3, assembler, dt))

    val model = pipeline.fit(trainRDD)

    //**** resolve test Data
    val testRDD = sc.textFile(root + "testDecisionTree2.txt")
    val testedRDD = testRDD.map(_.split(",")).map(p => temp(p(0).toString, p(1).toString,
      p(2).toInt, p(3).toString))
    import sqlContext.implicits._
    val testedDF = testedRDD.toDF

//    val assemblerTest = new VectorAssembler()
//      .setInputCols(Array("c", "a","b"))
//      .setOutputCol("features")
    //val testDataResult = assemblerTest.transform(testedDF)
    //**** end test Data

    // Make predictions.
    val predictions = model.transform(testedDF)

    // Select example rows to display.
    predictions.select("prediction", "label", "features").show(5)

    // Select (prediction, true label) and compute test error
    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("precision")
    val accuracy = evaluator.evaluate(predictions)
    println("Test Error = " + (1.0 - accuracy))

//    val treeModel = model.stages(4).asInstanceOf[DecisionTreeRegressionModel]
//    println("Learned regression tree model:\n" + treeModel.toDebugString)
    Thread.sleep(100000000)
  }
}
