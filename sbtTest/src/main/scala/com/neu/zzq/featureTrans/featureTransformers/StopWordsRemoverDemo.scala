package com.neu.zzq.featureTrans.featureTransformers

import java.io.File

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.ml.feature.StopWordsRemover
import org.apache.spark.sql.SQLContext

import scala.io.Source

/**
  * Created by taos on 2017/6/21.
  */
object StopWordsRemoverDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("StopWordsRemoverDemo")
    val sc =  new SparkContext(conf)
    val sqlContext =  new SQLContext(sc)
    val root = StopWordsRemoverDemo.getClass.getResource("/")
    //之间  这个词在我们自定义的停用词中
    val strStop = Source.fromFile(new File("E:\\stopword.txt"))("utf-8").getLines()
    val stops = strStop.toArray
    val remover = new StopWordsRemover()
      .setInputCol("raw")
      .setOutputCol("filtered")
    remover.setStopWords(stops)
    //将英文的停用词打印
    val stopwords = remover.getStopWords
    //stopwords.foreach(println)
    val dataSet = sqlContext.createDataFrame(Seq(
      (0, Seq("I", "saw", "the", "red", "baloon")),
      (1, Seq("Mary", "had", "a", "little", "lamb")),
        (2, Seq("你", "我", "之间", "没有", "矛盾"))
    )).toDF("id", "raw")

    remover.transform(dataSet).show()
  }
}
