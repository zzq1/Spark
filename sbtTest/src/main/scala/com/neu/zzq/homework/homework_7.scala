package com.neu.zzq.homework

import java.io.File

//import com.seven.StopWordsRemoverDemo
import org.ansj.recognition.impl.StopRecognition
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{Row, SQLContext, SparkSession}
import org.ansj.splitWord.analysis.ToAnalysis
import org.apache.spark.ml.classification.{DecisionTreeClassifier, NaiveBayes}
import org.apache.spark.ml.feature._
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.feature.LabeledPoint
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator

import scala.io.Source

object homework_7 {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().master("local[4]").appName("homework_7").getOrCreate()
    val df = spark.read.format("json").load("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第七次课\\data\\jobarea=010000&industrytype=01.json")
    val dftest = spark.read.format("json").load("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第七次课\\data\\jobarea=010000&industrytype=31.json")

    //.show()
    //df.select("dscr").show()
    df .where("title = '大数据工程师'" ).select("title","expr","slry").orderBy("slry").show()


    import spark.implicits._


    //之间  这个词在我们自定义的停用词中
    val strStop = Source.fromFile(new File("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第七次课\\stopword.txt"))("utf-8").getLines()
    val stops = strStop.toArray

    val filter = new StopRecognition()
    filter.insertStopNatures("w") //过滤掉标点
    filter.insertStopNatures("m")//过滤掉数字
    filter.insertStopNatures("null")//过滤null词性
    filter.insertStopNatures("<br />")//过滤<br　/>词性
    filter.insertStopNatures(":")
    filter.insertStopNatures("'")
    //val df2=remover.transform(df.select("dscr"))
    val result=df.select("dscr","cate").rdd.map { x =>
      (ToAnalysis.parse(x(0).toString()).recognition(filter).toStringWithOutNature(" "),
      x(1).toString)
    }.map(a=>(a._1.split(" "),a._2)).toDF("word","target")

    val resulttest=dftest.select("dscr","cate").rdd.map { x =>
      (ToAnalysis.parse(x(0).toString()).recognition(filter).toStringWithOutNature(" "),
        x(1).toString)
    }.map(a=>(a._1.split(" "),a._2)).toDF("word","target")


    val remover = new StopWordsRemover()
      .setInputCol("word")
      .setOutputCol("word2")
    remover.setStopWords(stops)
    //将英文的停用词打印
    val result2=remover.transform(result)
    val result2test=remover.transform(result)
    //result2.rdd.foreach(println)


    //将每个词转换成Int型，并计算其在文档中的词频（TF）
    val hashingTF = new HashingTF()
      .setInputCol("word2").setOutputCol("rawFeatures").setNumFeatures(2000)
    val featurizedData = hashingTF.transform(result2)

    val featurizedDatatest = hashingTF.transform(result2test)

    //计算TF-IDF值
    val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
    val idfModel = idf.fit(featurizedData)
    val rescaledData = idfModel.transform(featurizedData)
    val idfModeltest = idf.fit(featurizedDatatest)
    val rescaledDatatest = idfModeltest.transform(featurizedDatatest)
    //rescaledData.show()
  //rescaledData.select("features", "label").take(3).foreach(println)

    val indexer = new StringIndexer()
      .setInputCol("target")
      .setOutputCol("targetIndex")
    //遇到没见过的字符串直接过滤掉
    indexer.setHandleInvalid("skip")
    val indexed = indexer.fit(rescaledData).transform(rescaledData)
    val indexedtest = indexer.fit(rescaledDatatest).transform(rescaledDatatest)
    //indexed.rdd.collect().foreach(println)

    val result3=indexed.select("targetIndex","features")
//    result3.printSchema()
      .map {
      case Row(label: Double, features: Vector)=>
        LabeledPoint(label,features)
    }.filter(point=>{point.features.numNonzeros!=0})

    val result3test=indexedtest.select("targetIndex","features")
      //    result3.printSchema()
      .map {
      case Row(label: Double, features: Vector)=>
        LabeledPoint(label,features)
    }.filter(point=>{point.features.numNonzeros!=0})

//    result3test.collect().foreach(println)

    val model = new NaiveBayes().fit(result3)

    val predictions = model.transform(result3test)
//    println("predictln out:")
//    predictions.show
//    model.write.overwrite.save("resoult")

    //模型评估
//    val evaluator = new MulticlassClassificationEvaluator().setLabelCol("label").setPredictionCol("prediction").setMetricName("accuracy")
//    val accuracy = evaluator.evaluate(predictions)
//    println("accuracy out :")
//    println("Accuracy:" + accuracy)

  }

}
