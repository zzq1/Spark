package com.neu.zzq.featureTrans.featureTransformers.tokenizer

import org.apache.spark.ml.feature.{RegexTokenizer, Tokenizer}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by taos on 2017/6/21.
  * Tokenization将文本划分为独立个体（通常为单词）。
    RegexTokenizer基于正则表达式提供更多的划分选项。默认情况下，
    参数“pattern”为划分文本的分隔符。
    或者可以指定参数“gaps”来指明正则“patten”表示“tokens”而不是分隔符，
    这样来为分词结果找到所有可能匹配的情况。
  */
object TokenizerDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("PipelineDemo")
    val sc =  new SparkContext(conf)
    val sqlContext =  new SQLContext(sc)
    val sentenceDataFrame = sqlContext.createDataFrame(Seq(
      (0, "Hi I heard about Spark"),
      (1, "I wish Java could use case classes"),
      (2, "Logistic,regression,models,are,neat")
    )).toDF("label", "sentence")

    val tokenizer = new Tokenizer().setInputCol("sentence").setOutputCol("words")
    val regexTokenizer = new RegexTokenizer()
      .setInputCol("sentence")
      .setOutputCol("words")
      .setPattern("\\W") // alternatively .setPattern("\\w+").setGaps(false)

    val tokenized = tokenizer.transform(sentenceDataFrame)
    tokenized.select("words", "label").take(3).foreach(println)
    val regexTokenized = regexTokenizer.transform(sentenceDataFrame)
    regexTokenized.select("words", "label").take(3).foreach(println)
  }
}
