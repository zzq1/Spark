package com.neu.zzq.featureTrans.featureTransformers

import org.apache.spark.ml.feature.VectorIndexer
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext

/**
  * Created by taos on 2017/6/21.
  */
object VectorIndexerDemo_2 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("PCADemo")
    val sc =  new SparkContext(conf)
    val sqlContext =  new SQLContext(sc)
    val data = Seq(Vectors.dense(-1.0, 1.0, 1.0),Vectors.dense(-1.0, 3.0, 1.0), Vectors.dense(0.0, 5.0, 1.0))
    val df = sqlContext.createDataFrame(data.map(Tuple1.apply)).toDF("features")
    val indexer = new VectorIndexer().
      setInputCol("features").
      setOutputCol("indexed").
      //设置条件；只有种类小于2的特征才被认为是类别型特征
      setMaxCategories(2)
    val indexerModel = indexer.fit(df)
    val categoricalFeatures: Set[Int] = indexerModel.categoryMaps.keys.toSet
    println(s"Chose ${categoricalFeatures.size} categorical features: " + categoricalFeatures.mkString(", "))
    //[features: vector, indexed: vector] indexData 包含两部分 features vector 和  indexed vector
    val indexedData = indexerModel.transform(df)
    indexedData.collect().foreach { println }
  }
}
