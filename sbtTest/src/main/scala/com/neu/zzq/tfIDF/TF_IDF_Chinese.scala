package com.neu.zzq.tfIDF

import org.apache.spark.ml.feature.{HashingTF, IDF, Tokenizer}
import org.apache.spark.sql.SparkSession

/**
  * Created by taos on 2017/12/22.
  */
object TF_IDF_Chinese {
  def main(args: Array[String]): Unit = {
    val str = "职位描述：\\n1.负责人工智能大数据系统设计、功能模块设计，数据平台的搭建、维护和优化；\\n2.负责规划数据挖掘的整体流程，并参与用户产品和数据产品的决策；\\n3.与业务部门密切配合，寻求数据层面的业务价值，利用数据分析结论推动产品优化；\\n4.设计和实现高性能可扩展的服务和架构，研究新算法，改进现有的算法；\\n5.技术团队的管理，制定开发规范，撰写相关技术文档指导和培训工程师；\\n6.对海量数据进行统计、采样和分析；\\n任职资格：\\n1.学历要求：\\n-毕业于211或985院校、统招本科及以上学历，数学、统计学、计算机信息类等专业背景。\\n2.工作经验及能力要求：\\n-对数字，数据敏感，具备良好的逻辑思维能力，能够从海量数据中发现有价值的规律；\\n-优秀的分析问题和解决问题的能力，具有较好的归纳能力与较好的文字和语言表达能力；\\n-熟悉数据结构,熟悉数据挖掘和机器学习算法等常用算法,并对机器学习算法和理论有较深入的研究（如对熟悉决策树、聚类、逻辑回归，序列标注,关联分析、SVM，贝叶斯等数据挖掘算法有较深理解和实践经验）；\\n-熟悉java/Python，有算法研究背景经验；\\n-熟悉文本挖掘分析方法及分布式数据分析工具使用；\\n-熟悉Hadoop、Spark平台，熟悉storm的流式处理框架，熟悉Hive工具，有百T左右数据处理经验。\\n3.优先考虑：\\n-有社会性网络数据的挖掘经验者优先。\\r\\n\\n\\r\\n\\n\\r\\n\\n\\r\\n\\n\""

    val spark = SparkSession.builder().master("local").appName("BinarizerDemo").getOrCreate()
    import spark.implicits._
    val sentenceData = spark.createDataFrame(Seq(
      (0, "负责 人工智能 大数据 系统设计 功能模块设计 数据平台 的 搭建 维护 和 优化"),
      (1, "负责 规划 数据挖掘 的 整体流程，并 参与 用户产品 和 数据产品 的 决策"),
      (2, "与 业务部门 密切 配合 寻求 数据层面 的 业务价值，利用 数据分析结论 推动 产品优化")
    )).toDF("label", "sentence")

   val tokenizer = new Tokenizer().setInputCol("sentence").setOutputCol("words")
    val wordsData = tokenizer.transform(sentenceData)
    wordsData.show()
    val hashingTF = new HashingTF()
      .setInputCol("words").setOutputCol("rawFeatures").setNumFeatures(20000)
    val featurizedData = hashingTF.transform(wordsData)
    featurizedData.show()
    featurizedData.collect().foreach {println}

    println(hashingTF.explainParams)

    val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
    val idfModel = idf.fit(featurizedData)
    val rescaledData = idfModel.transform(featurizedData)
    rescaledData.show()
    rescaledData.foreach(x => println(x))
    rescaledData.select("features", "label").take(3).foreach(println)
  }
}
