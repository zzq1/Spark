package com.neu.zzq.featureTrans.featureTransformers

import org.apache.spark.ml.feature.NGram
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SQLContext, SparkSession}

/**
  * Created by taos on 2017/6/21.
  * n-gram 用于将句子分为连续词的序列，用于自然语言处理中机器翻译等应用，这种连续的字符有助于基于统计的
  * 模型存放连续的词语进而准确的进行翻译。
  */
object NGramDemo {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().master("local").appName("StopWordsRemoverDemo").getOrCreate()
    import spark.implicits._
    val wordDataFrame = spark.createDataFrame(Seq(
      (0, Array("Hi", "I", "heard", "about", "Spark")),
      (1, Array("I", "wish", "Java", "could", "use", "case", "classes")),
      (2, Array("Logistic", "regression", "models", "are", "neat"))
    )).toDF("label", "words")

    val ngram = new NGram().setN(3).setInputCol("words").setOutputCol("ngrams")
    val ngramDataFrame = ngram.transform(wordDataFrame)
    ngramDataFrame.take(3).map(_.getAs[Stream[String]]("ngrams").toList).foreach(println)
  }
}
