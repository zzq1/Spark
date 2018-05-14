package com.horizon.ml.classification

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.DecisionTreeClassifier
import org.apache.spark.ml.evaluation.{MulticlassClassificationEvaluator, RegressionEvaluator}
import org.apache.spark.ml.feature.{OneHotEncoder, StringIndexer, VectorAssembler, VectorIndexer}
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.regression.{DecisionTreeRegressionModel, DecisionTreeRegressor}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.sql.types._

/**
  * Created by taos on 2017/6/27.
  */
object DecisionTreeDemo {
  case class temp(a: String, b: String, c: Int, d: String)
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("PipelineDemo")set("spark.driver.extraJavaOptions",
      "-Xms1512m -Xmx2048m -XX:PermSize=512M -XX:MaxPermSize=512M")

    val sc =  new SparkContext(conf)
    val sqlContext =  new SQLContext(sc)
    val root = TreeRegression.getClass.getResource("/")
    // Load the data stored in LIBSVM format as a DataFrame.
    val rddString = sc.textFile(root + "decisionTree.txt")
    val tempRDD = rddString.map(_.split(",")).map(p => temp(p(0).toString, p(1).toString,
      p(2).toInt, p(3).toString))

//
//
//    val wordDataFrame = sqlContext.createDataFrame(rddArray,
//      StructType(Seq(StructField("a", StringType, nullable = true),StructField("d", StringType, nullable = true),StructField("b", IntegerType, nullable = true),StructField("c", StringType, nullable = true)))
//    )
//        val wordDataFrame = sqlContext.createDataFrame(rddArray,
//          StructType(Seq(StructField("a", StringType, nullable = true)))
//        )
    import sqlContext.implicits._
    val wordDataFrame = tempRDD.toDF
    wordDataFrame.printSchema
    wordDataFrame.show()
    // transform
    val indexer = new StringIndexer()
    val paramMapa = ParamMap(indexer.inputCol -> "a")
      .put(indexer.outputCol -> "a_")
    val paramMapc = ParamMap(indexer.inputCol -> "b")
      .put(indexer.outputCol -> "b_")
    val paramMapd = ParamMap(indexer.inputCol -> "d")
      .put(indexer.outputCol -> "label")
    val indexeda = indexer.fit(wordDataFrame,paramMapa).transform(wordDataFrame)
    val indexedc = indexer.fit(indexeda,paramMapc).transform(indexeda)
    val indexedd = indexer.fit(indexedc,paramMapd).transform(indexedc)
    indexedd.show()

//    +---+---+---+---+---+---+-----+--------------+
//    |  a|  b|  c|  d| a_| b_|label|      features|
//    +---+---+---+---+---+---+-----+--------------+
//    |  是| 单身| 12|  是|0.0|1.0|  0.0|[12.0,0.0,1.0]|
//    |  否| 单身| 15|  是|1.0|1.0|  0.0|[15.0,1.0,1.0]|
//    |  是|离过婚| 10|  是|0.0|0.0|  0.0|[10.0,0.0,0.0]|
//    |  否| 单身| 18|  是|1.0|1.0|  0.0|[18.0,1.0,1.0]|
//    |  是|离过婚| 25|  是|0.0|0.0|  0.0|[25.0,0.0,0.0]|
//    |  是| 单身| 50|  是|0.0|1.0|  0.0|[50.0,0.0,1.0]|
//    |  否|离过婚| 35|  是|1.0|0.0|  0.0|[35.0,1.0,0.0]|
//    |  是|离过婚| 40|  是|0.0|0.0|  0.0|[40.0,0.0,0.0]|
//    |  否| 单身| 60|  是|1.0|1.0|  0.0|[60.0,1.0,1.0]|
//    |  否|离过婚| 17|  否|1.0|0.0|  1.0|[17.0,1.0,0.0]|
//    +---+---+---+---+---+---+-----+--------------+


    val assembler = new VectorAssembler()
      .setInputCols(Array("c", "a_","b_"))
      .setOutputCol("features")
    val output = assembler.transform(indexedd)
    output.show
    //features selector

    // Automatically identify categorical features, and index them.
    // Here, we treat features with > 4 distinct values as continuous.
//    val featureIndexer = new VectorIndexer()
//      .setInputCol("a")
//      .setOutputCol("indexedFeatures")
//      .setMaxCategories(4)
//      .fit(wordDataFrame)
//
//    // Split the data into training and test sets (30% held out for testing)
//     val Array(trainingData, testData) = output.randomSplit(Array(0.7, 0.3))
//
    // Train a DecisionTree model.
    val dt = new DecisionTreeClassifier()
      .setLabelCol("label")
      .setFeaturesCol("features")
//
    // Chain indexer and tree in a Pipeline
//    val pipeline = new Pipeline()
//      .setStages(Array(featureIndexer, dt))
    val testRDD = sc.textFile(root + "testDecisionTree.txt")
    val testResultRDD = testRDD.map{
      line =>
        val array = line.split(",")
        Row(array.map(_.toDouble):_*)
    }
    val testData = sqlContext.createDataFrame(testResultRDD,
          StructType(Seq(StructField("a", DoubleType, nullable = true),StructField("b", DoubleType, nullable = true),
            StructField("c", DoubleType, nullable = true),StructField("label", DoubleType, nullable = true)))
    )
    val assemblerTest = new VectorAssembler()
      .setInputCols(Array("c", "a","b"))
      .setOutputCol("features")
    val testDataResult = assemblerTest.transform(testData)

    // Train model.  This also runs the indexer.
    val model = dt.fit(output)
//    1,0,33 1  无房 离过婚  33w  不见
//    1,0,38 1 无房 离过婚  38w   不见
//    0,1,22 0 有房 单身    22w   见面
    // Make predictions.
    val predictions = model.transform(testDataResult)

    // Select example rows to display.
    predictions.select("prediction", "label", "features").show()

    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("precision")
    val accuracy = evaluator.evaluate(predictions)
    println("Test Error = " + (1.0 - accuracy))

  }
}
