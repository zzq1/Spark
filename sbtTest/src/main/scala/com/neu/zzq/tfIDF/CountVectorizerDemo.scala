package com.neu.zzq.tfIDF

import org.apache.spark.ml.feature.{CountVectorizer, CountVectorizerModel}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by taos on 2017/6/13.
  * 词频－逆向文件频率（TF-IDF）是一种在文本挖掘
  * 中广泛使用的特征向量化方法，
  * 它可以体现一个文档中词语在语料库中的重要程度
  */
object CountVectorizerDemo {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("CountVectorizerDemo")

    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    val df = sqlContext.createDataFrame(Seq(
      (0, Array("a", "b", "c")),
      (1, Array("a", "b", "b", "c", "a"))
    )).toDF("id", "words")

    // fit a CountVectorizerModel from the corpus
    val cvModel: CountVectorizerModel = new CountVectorizer()
      .setInputCol("words")
      .setOutputCol("features")
      .setVocabSize(3)//设定词汇表的最大size为3
      .setMinDF(2)//设定词汇表中的词至少要在2个文档中出现过
      .fit(df)
                                                   //先验词汇
    // alternatively, define CountVectorizerModel with a-priori vocabulary
    val cvm = new CountVectorizerModel(Array("a", "b", "c"))
      .setInputCol("words")
      .setOutputCol("features")

    cvModel.transform(df).select("features").show()

//    从打印结果我们可以看到，词汇表中有“a”，“b”，“c”三个词，
//    且这三个词都在2个文档中出现过。其中结果中前面的3代表的是vocabsize；
//    “a”和“b”都出现了3次，而“c”出现两次，所以在结果中0和1代表“a”和“b”，2代表“c”；
//    后面的数组是相应词语在各个文档中出现次数的统计。倘若把vocabsize设为2，则不会出现“c”。
//    也可以用下面的方式来创建一个CountVectorizerModel，
//    通过指定一个数组来预定义一个词汇表，在本例中即只包含“a”，“b”，“c”三个词。
  }

}
