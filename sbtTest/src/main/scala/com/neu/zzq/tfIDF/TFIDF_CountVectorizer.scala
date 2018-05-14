package com.neu.zzq.tfIDF

import org.apache.spark.ml.feature._
import org.apache.spark.sql.SparkSession

/**
  * Created by taos on 2017/12/23.
  */
object TFIDF_CountVectorizer {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().master("local").appName("BinarizerDemo").getOrCreate()
    import spark.implicits._
    val sentenceData = spark.createDataFrame(Seq(
      (0, "Hi I heard about Spark Hi Spark"),
      (1, "I wish Spark could use case classes"),
      (2, "Logistic regression models are neat")
    )).toDF("label", "sentence")
    sentenceData.show()
    val tokenizer = new Tokenizer().setInputCol("sentence").setOutputCol("words")
    val wordsData = tokenizer.transform(sentenceData)
    wordsData.show()
    // fit a CountVectorizerModel from the corpus
    val cvModel: CountVectorizerModel = new CountVectorizer()
      .setInputCol("words")
      .setOutputCol("rawFeatures")
      .setVocabSize(10)//设定词汇表的最大size为3
      .setMinDF(2)//设定词汇表中的词至少要在2个文档中出现过
      .fit(wordsData)
    println(cvModel.vocabulary.toBuffer)
    val featurizedData = cvModel.transform(wordsData)
    val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
    val idfModel = idf.fit(featurizedData)
    val rescaledData = idfModel.transform(featurizedData)
    rescaledData.show()
    rescaledData.select("features", "label").take(3).foreach(println)
  }
}
