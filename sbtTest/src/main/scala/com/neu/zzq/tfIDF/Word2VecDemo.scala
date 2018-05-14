package com.neu.zzq.tfIDF

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.ml.feature.Word2Vec
import org.apache.spark.sql.SQLContext
/**
  * Created by taos on 2017/6/13.
  */
object Word2VecDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("Word2VecDemo")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    // Input data: Each row is a bag of words from a sentence or document.
    val documentDF = sqlContext.createDataFrame(Seq(
      "打 I heard about Spark".split(" "),
      "踢 wish Java could use case classes".split(" "),
      "玩 regression models are neat".split(" ")
    ).map(Tuple1.apply)).toDF("text")
    documentDF.show()
    // Learn a mapping from words to Vectors.
    val word2Vec = new Word2Vec()
      .setInputCol("text")
      .setOutputCol("result")
      .setVectorSize(5)
      .setMinCount(0)
    .setNumPartitions(3)
    .setMaxIter(10)
    val model = word2Vec.fit(documentDF)
    val ss = model.findSynonyms("打",2)
    ss.show
    //求近义词
//    val synonyms = model.findSynonyms("Spark", 1)
//    synonyms.show()
    val result = model.transform(documentDF)
    result.show
    result.select("result").take(3).foreach(println)
  }
}
