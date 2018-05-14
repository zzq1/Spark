package com.horizon.ml.regression

import com.horizon.ml.classification.TreeRegression
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.VectorIndexer
import org.apache.spark.ml.regression.{RandomForestRegressionModel, RandomForestRegressor}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext

/**
  * Created by taos on 2017/6/28.
  */
object RandomForestDemo {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("PipelineDemo")
    val sc =  new SparkContext(conf)
    val sqlContext =  new SQLContext(sc)
    val root = TreeRegression.getClass.getResource("/")
    // Load and parse the data file, converting it to a DataFrame.
    val data = sqlContext.read.format("libsvm").load(root + "sample_libsvm_data.txt")

    // Automatically identify categorical features, and index them.
    // Set maxCategories so features with > 4 distinct values are treated as continuous.
    val featureIndexer = new VectorIndexer()
      .setInputCol("features")
      .setOutputCol("indexedFeatures")
      .setMaxCategories(4)
      .fit(data)

    // Split the data into training and test sets (30% held out for testing)
    val Array(trainingData, testData) = data.randomSplit(Array(0.7, 0.3))

    // Train a RandomForest model.
    val rf = new RandomForestRegressor()
      .setLabelCol("label")
      .setFeaturesCol("indexedFeatures")

    // Chain indexer and forest in a Pipeline
    val pipeline = new Pipeline()
      .setStages(Array(featureIndexer, rf))

    // Train model.  This also runs the indexer.
    val model = pipeline.fit(trainingData)

    // Make predictions.
    val predictions = model.transform(testData)

    // Select example rows to display.
    predictions.select("prediction", "label", "features").show(5)

    // Select (prediction, true label) and compute test error
    val evaluator = new RegressionEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("rmse")
    val rmse = evaluator.evaluate(predictions)
    println("Root Mean Squared Error (RMSE) on test data = " + rmse)

    val rfModel = model.stages(1).asInstanceOf[RandomForestRegressionModel]
    println("Learned regression forest model:\n" + rfModel.toDebugString)
  }

}
