package com.neu.zzq.homework

import java.io.File
import java.util

import org.ansj.domain.Term
import org.ansj.library.DicLibrary
import org.ansj.splitWord.analysis.ToAnalysis
import org.apache.spark.ml.feature.{CountVectorizer, CountVectorizerModel, IDF, StopWordsRemover}
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, udf}

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * Created by taos on 2017/12/24.
  */
//object Testor{
//  def main(args: Array[String]): Unit = {
//    val vocabulary  = Array("spark", "hadoop","stom","linux","neo4j", "graphx","nodejs","wike")
//    val sv1: Vector = Vectors.sparse(8, Array(0, 2,3,4,5), Array(1.0, 3.0, 5,7,9))
//    val array = Array(11,13,41,11,21,16,14)
//    //println(array.zipWithIndex.sortWith(_._1 > _._1).take(3).foreach(println(_)))
//    val rr = getWordIndex(3,sv1.toSparse.indices, sv1.toSparse.values, vocabulary)
//    rr.foreach(println(_))
//  }
//
//}

object NLP {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().master("local[4]").appName("NLP").getOrCreate()
    val root = NLP.getClass.getResource("/")
    DicLibrary.put(DicLibrary.DEFAULT,"E:/userDict/part-00000")
    val qcwy = spark.read.json(root + "/nlp/jobarea=010000&industrytype*.json")
    qcwy.createOrReplaceTempView("qcwy")
    //showTitle(spark)

    showDesc(spark)
    //三个分类， java  大数据  数据分析 看摘要
  }
  val splitContent = (str: String) => {
    val temp =  str.trim.replaceAll("\t","").replaceAll("职位描述:","").replaceAll("描述:","")
      .replaceAll("\\r","").replaceAll("\\n","").replaceAll("岗位要求 ： ","")
      .replaceAll("岗位职责 ：","").replaceAll("职责 ：","")
      .replaceAll("1、","").replaceAll("2、","")
      .replaceAll("3、","").replaceAll("4、","")
      .replaceAll("5、","").replaceAll("6、","")
      .replaceAll("7、","").replaceAll("8、","")
      .replaceAll("一、","").replaceAll("二 , ","")
      .replaceAll("三 , ","").replaceAll("四 , ","")
      .replaceAll("五 , ","").replaceAll("六 , ","")
      .replaceAll("七 , ","").replaceAll("八 , ","").trim
    //分词
    seg(temp)
  }
  def showDesc(spark: SparkSession) = {

    spark.udf.register("splitContent",splitContent)
    val dscrDF = spark.sql("select id, splitContent(dscr) from qcwy ")
    val strStop = Source.fromFile(new File("E:\\stopword.txt"))("utf-8").getLines()
    val stops = strStop.toArray
    val remover = new StopWordsRemover()
      .setInputCol("UDF:splitContent(dscr)")
      .setOutputCol("filtered")
    remover.setStopWords(stops)
    val df = remover.transform(dscrDF)
  //  dscrDF.select(col("dscr"), t(col("dscr")).as("desc"))
    println(dscrDF.schema)
    dscrDF.show()
    val cvModel: CountVectorizerModel = new CountVectorizer()
      .setInputCol("filtered")
      .setOutputCol("features")
      .setVocabSize(30000)
      .setMinDF(2)
      .fit(df)
    //cvModel.transform(dscrDF).select("features").collect.foreach(println(_))
    println(cvModel.vocabulary.size)

    val featurizedData = cvModel.transform(df)

    //dscrDF.collect().foreach(println(_))
    val idf = new IDF().setInputCol("features").setOutputCol("rawFeatures")
    val idfModel = idf.fit(featurizedData)
    val rescaledData = idfModel.transform(featurizedData)
    rescaledData
    val findWords = udf { words: Vector => getWordIndex(10, words.toSparse.indices, words.toSparse.values, cvModel.vocabulary)}
    rescaledData.withColumn("rawFeatures1", findWords(col("rawFeatures"))).show
    //rescaledData.show
   // rescaledData.collect().foreach(println(_))
  }
  //udf

  //探索title
  def showTitle(spark: SparkSession) = {
    val titleDF = spark.sql("select title from qcwy where title like '%java%' or title like '%数据%' ")
    //val t = udf { desc: String => desc.replaceAll("\t","") }
    //dscrDF.select(col("dscr"), t(col("dscr")).as("desc"))
    titleDF.collect().foreach(println(_))
  }
  def seg(str:String):Array[String] = {
      val result = ToAnalysis.parse(str)
      val terms: util.List[Term] = result.getTerms
      //var segged = ""
      val segged = new ArrayBuffer[String]()
      for(i<- 0 until terms.size() -1){
        val word = terms.get(i).getName
        segged += word + " "
      }
      segged.toArray
  }

  def getWordIndex(rsLength: Int, indexs: Array[Int], values: Array[Double],
                   vocabulary: Array[String]): Array[String] = {
    values.zipWithIndex.sortWith(_._1 > _._1).take(rsLength).map({
      line => vocabulary(indexs(line._2))
    })
  }
}
