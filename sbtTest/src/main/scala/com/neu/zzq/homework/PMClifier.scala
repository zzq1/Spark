package com.neu.zzq.homework

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler, VectorIndexer}
import org.apache.spark.ml.classification.RandomForestClassifier
import org.apache.spark.sql.SparkSession

/**
  * Created by zzq on 2018/1/4.
  */
object PMClifier {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder.master("local[4]").appName(s"${this.getClass.getSimpleName}").getOrCreate()
    val pm = spark.read.option("header","true")
      .option("inferSchema", "true")
      .csv("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第八节课\\pm.csv")
      .na.drop()
    spark.udf.register("get_LevelNum",(a: Double) => {
      if (a <= 50) 0
      else if (a > 50 & a <= 100) 1
      else if (a > 100 & a <= 150) 2
      else if (a > 150 & a <= 200) 3
      else if (a > 200 & a <= 300) 4
      else 5
    })
    spark.udf.register("get_LevelStr",(a: Double) => {
      if (a <= 50) "优"
      else if (a > 50 & a <= 100) "良"
      else if (a > 100 & a <= 150) "轻度污染"
      else if (a > 150 & a <= 200) "中度污染"
      else if (a > 200 & a <= 300) "重度污染"
      else "严重污染"
    })
    spark.udf.register("get_predictionStr",(a:Double) =>{
      if (a ==0) "优"
      else if (a == 1) "良"
      else if (a == 2) "轻度污染"
      else if (a == 3) "中度污染"
      else if (a == 4) "重度污染"
      else "严重污染"
    })
//    pm.show()
    val level = pm.selectExpr("No","year","month","day","hour","pm",
      "DEWP","TEMP","PRES","cbwd","Iws","Is","Ir","get_LevelNum(pm)","get_LevelStr(pm)")
    .toDF("No","year","month","day","hour","pm",
      "DEWP","TEMP","PRES","cbwd","Iws","Is","Ir","LevelNum","LevelStr")
//    level.printSchema()
//   level.show()
    val indexer = new StringIndexer().setInputCol("cbwd").setOutputCol("cbwd_indexed")
    val indexered = indexer.fit(level).transform(level).drop("cbwd","No","year","pm","LevelStr")
//    indexered.printSchema()
//    indexered.show()
    val assembler = new VectorAssembler()
      .setInputCols(Array("month","day","hour","DEWP","TEMP","PRES","Iws","Is","Ir","cbwd_indexed"))
      .setOutputCol("features")
    val featureIndexer = new VectorIndexer()
      .setInputCol("features")
      .setOutputCol("indexedFeatures")
      .setMaxCategories(4)
    //    val assemblered = assembler.transform(indexered).select("label","features")
    //    assemblered.show()
    val Array(training, testing) = indexered.randomSplit(Array(0.8, 0.2),seed = 2018)
    training.show()
    val rf = new RandomForestClassifier()
      .setLabelCol("LevelNum")
      .setFeaturesCol("indexedFeatures")
      .setMaxDepth(6)
      .setMaxBins(100)
      .setNumTrees(3)
    val pipeline = new Pipeline().setStages(Array(assembler, featureIndexer, rf))
    val model = pipeline.fit(training)
    val predictions = model.transform(testing)
    val prediction = predictions.selectExpr("LevelNum","prediction","get_predictionStr(prediction)")
      .toDF("LevelNum","prediction","predictionStr")

    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("LevelNum")
      .setPredictionCol("prediction")
      .setMetricName("accuracy")
    val rmse = evaluator.evaluate(predictions)
    println("Root Mean Squared Error (RMSE) on test data = " + rmse)
    val accuracy = evaluator.evaluate(predictions)
    println("Test Error = " + (1.0 - accuracy))
    prediction.show()
    //    val rfModel = model.stages(1).asInstanceOf[RandomForestRegressionModel]
    //    println("Learned regression forest model:\n" + rfModel.toDebugString)
    // $example off$
    spark.stop()
  }
}

