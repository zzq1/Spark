package com.neu.zzq.tfIDF

import org.apache.spark.ml.feature.{HashingTF, IDF, Tokenizer}
import org.apache.spark.sql.{SparkSession}

/**
  * Created by taos on 2017/6/13.
  * 词频－逆向文件频率（TF-IDF）是一种在文本挖掘中广泛使用的特征向量化方法，
  * 它可以体现一个文档中词语在语料库中的重要程度。
  */
object TFIDFDEMO {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().master("local").appName("BinarizerDemo").getOrCreate()
    import spark.implicits._
    val sentenceData = spark.createDataFrame(Seq(
      (0, "Hi I heard about Spark Hi Spark"),
      (1, "I wish Java could use case classes"),
      (2, "Logistic regression models are neat")
    )).toDF("label", "sentence")

    val tokenizer = new Tokenizer().setInputCol("sentence").setOutputCol("words")
    val wordsData = tokenizer.transform(sentenceData)
    wordsData.show()
    val hashingTF = new HashingTF()
      .setInputCol("words").setOutputCol("rawFeatures").setNumFeatures(20)
    val featurizedData = hashingTF.transform(wordsData)
    featurizedData.show()
    featurizedData.collect().foreach {println}

    println(hashingTF.explainParams)
    // 我们可以看到每一个单词被哈希成了一个不同的索引值。
    // 以"Hi I heard about Spark Hi Spark"为例，
    // 输出结果中2000代表哈希表的桶数，“(2000,[105,365,1329,1469,1926],[1.0,2.0,2.0,1.0,1.0])”
    // 分别代表着“i, Hi, Spark, heard about”的哈希值，
    // “[2.0,2.0,1.0,1.0,1.0,1.0]”为对应单词的出现次数。
    val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
    val idfModel = idf.fit(featurizedData)
    val rescaledData = idfModel.transform(featurizedData)
    rescaledData.show()
    rescaledData.select("features", "label").take(3).foreach(println)
  }

}
