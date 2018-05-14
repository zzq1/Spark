package com.neu.zzq.homework
import java.io.File

import org.ansj.recognition.impl.StopRecognition
import org.ansj.splitWord.analysis.ToAnalysis
import org.apache.spark.ml.classification.NaiveBayes
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature._
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.sql.{Row, SQLContext, SaveMode, SparkSession}

import scala.io.Source
/**
  * Created by zzq on 2018/1/2.
  */
object FenciPre {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().master("local").appName("Recruitment").getOrCreate()

    //将数据进行结构化，jsDF1作为训练集，jsDF2作为测试集
    val jsDF1 = spark.read.json("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第七次课\\data\\jobarea=010000&industrytype=01.json")
    val jsDF2 = spark.read.json("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第七次课\\data\\jobarea=010000&industrytype=31.json")
    //val jsDF = jsDF1.union(jsDF2)
    //jsDF.createOrReplaceTempView("jsDB")
    //val DBCate = spark.sql("SELECT t.dscr FROM jsDB t")
    val stop = new StopRecognition()
    stop.insertStopNatures("w")//过滤掉标点
    stop.insertStopNatures("m")//过滤掉m词性
    stop.insertStopNatures("null")//过滤null词性
    stop.insertStopNatures("<br />")//过滤<br　/>词性
    stop.insertStopNatures(":")
    stop.insertStopNatures("'")
    import  spark.implicits._
    //对测试集和训练集的desr进行过滤掉停用词filter
    val train=jsDF1.select("dscr","cate").rdd.map { x =>
      (ToAnalysis.parse(x(0).toString()).recognition(stop).toStringWithOutNature(" "),
        x(1).toString)
    }.map(a=>(a._1.split(" "),a._2)).toDF("word","target")
    val test=jsDF2.select("dscr","cate").rdd.map { x =>
      (ToAnalysis.parse(x(0).toString()).recognition(stop).toStringWithOutNature(" "),
        x(1).toString)
    }.map(a=>(a._1.split(" "),a._2)).toDF("word","target")
    val remover = new StopWordsRemover()
      .setInputCol("word")
      .setOutputCol("word1")
    val strStop = Source.fromFile(new File("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第七次课\\stopword.txt"))("utf-8").getLines()
    val stops = strStop.toArray
    //根据stops进行二次过滤
    remover.setStopWords(stops)
    val train1=remover.transform(train)
    val test1=remover.transform(test)
    //计算TF-IDF值
    val hashingTF = new HashingTF().setInputCol("word1").setOutputCol("rawFeatures").setNumFeatures(5000)
    val featurizedData = hashingTF.transform(train1)
    val featurizedDatatest = hashingTF.transform(test1)

    val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
    val idfModel = idf.fit(featurizedData)
    val rescaledData = idfModel.transform(featurizedData)
    val idfModeltest = idf.fit(featurizedDatatest)
    val rescaledDatatest = idfModeltest.transform(featurizedDatatest)

    val indexer = new StringIndexer()
      .setInputCol("target")
      .setOutputCol("targetIndex")
    //遇到没见过的字符串直接过滤掉
    indexer.setHandleInvalid("skip")
    val indexed = indexer.fit(rescaledData).transform(rescaledData)
    val indexedtest = indexer.fit(rescaledDatatest).transform(rescaledDatatest)
    //将数据转化成LabeledPoint，输入给算法
    val train3=indexed.select("targetIndex","features").map {
      case Row(label: Double, features: Vector)=>
        LabeledPoint(label,features)
    }.filter(point=>{point.features.numNonzeros!=0})
    indexedtest.show()
    val test3=indexedtest.select("targetIndex","features").map {
      case Row(label: Double, features: Vector)=>
        LabeledPoint(label,features)
    }.filter(point=>{point.features.numNonzeros!=0})

    val model = new NaiveBayes().fit(train3)
//    train3.show()
//    test3.show()
    val predictions = model.transform(test3)
    predictions.show()
    // 模型评估
    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("accuracy")
    val accuracy = evaluator.evaluate(predictions)
    println("Test set accuracy = " + accuracy)
    spark.stop()
  }
}
