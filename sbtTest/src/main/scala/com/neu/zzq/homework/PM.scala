package com.neu.zzq.homework

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.ml.regression.RandomForestRegressor
import org.apache.spark.sql.SparkSession

/**
  * Created by zzq on 2018/1/3.
  */

object PM {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder.master("local[4]").appName(s"${this.getClass.getSimpleName}").getOrCreate()
    val pm = spark.read.option("header","true").option("inferSchema", "true").csv("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第八节课\\pm.csv")
      .na.drop().drop("No").drop("year")
      .withColumnRenamed("pm","label")
    val indexer = new StringIndexer().setInputCol("cbwd").setOutputCol("cbwd_indexed")
    val indexered = indexer.fit(pm).transform(pm).drop("cbwd")
    indexered.printSchema()
    indexered.show()
    val assembler = new VectorAssembler()
      .setInputCols(Array("month","day","hour","DEWP","TEMP","PRES","Iws","Is","Ir","cbwd_indexed"))
      .setOutputCol("features")
//    val assemblered = assembler.transform(indexered).select("label","features")
//    assemblered.show()
    val Array(training, testing) = indexered.randomSplit(Array(0.8, 0.2),seed = 2018)
    training.show()
    val rf = new RandomForestRegressor().setLabelCol("label").setFeaturesCol("features")
    val pipeline = new Pipeline().setStages(Array(assembler, rf))
    val model = pipeline.fit(training)
    val predictions = model.transform(testing)
    predictions.show()
    val evaluator = new RegressionEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("rmse")
    val rmse = evaluator.evaluate(predictions)
    println("Root Mean Squared Error (RMSE) on test data = " + rmse)

//    val rfModel = model.stages(1).asInstanceOf[RandomForestRegressionModel]
//    println("Learned regression forest model:\n" + rfModel.toDebugString)
    // $example off$
    spark.stop()
 }

}
