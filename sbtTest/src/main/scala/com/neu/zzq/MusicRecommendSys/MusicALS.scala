package com.neu.zzq.MusicRecommendSys

import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.recommendation.ALS
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.ml.recommendation.ALSModel
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.collection.Map


object MusicALS {

  case class Rating(userId: Int, artistId: Int, rating: Float)
  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .master("local[4]")
      .appName("ALSExample")
      .getOrCreate()
    val base = "hdfs://172.17.11.156:9000/data/"
    val sc = spark.sparkContext
    //加载数据
    val rawUserArtistData = sc.textFile(base + "user_artist_data.txt")
    val rawArtistData = sc.textFile(base + "artist_data.txt")
    val rawArtistAlias = sc.textFile(base + "artist_alias.txt")
    //解析数据
    def buildArtistByID(rawArtistData: RDD[String]) =
      rawArtistData.flatMap { line =>
        val (id, name) = line.span(_ != '\t')
        if (name.isEmpty) {
          None
        } else {
          try {
            Some((id.toInt, name.trim))
          } catch {
            case e: NumberFormatException => None
          }
        }
      }

    def buildArtistAlias(rawArtistAlias: RDD[String]): Map[Int,Int] =
      rawArtistAlias.flatMap { line =>
        val tokens = line.split('\t')
        if (tokens(0).isEmpty) {
          None
        } else {
          Some((tokens(0).toInt, tokens(1).toInt))
        }
      }.collectAsMap()

    def buildRatings(
                      rawUserArtistData: RDD[String],
                      bArtistAlias: Broadcast[Map[Int,Int]]) = {
      rawUserArtistData.map { line =>
        val Array(userID, artistID, count) = line.split(' ').map(_.toInt)
        val finalArtistID = bArtistAlias.value.getOrElse(artistID, artistID)
        Rating(userID, finalArtistID, count)
      }
    }
    //将ArtistAlias读入内存转化为map广播出去
    val bArtistAlias = sc.broadcast(buildArtistAlias(rawArtistAlias))

    val trainData = buildRatings(rawUserArtistData, bArtistAlias).cache()
    val data  = spark.createDataFrame(trainData)
    val Array(training, test) = data.randomSplit(Array(0.8, 0.2))
    //构建模型
    val als = new ALS()
      .setMaxIter(5)
      .setRegParam(0.01)
      .setUserCol("userId")
      .setItemCol("artistId")
      .setRatingCol("rating")
    //训练模型
    val model = als.fit(training)
    //预测
    model.setColdStartStrategy("drop")
    val predictions = model.transform(test)
    //评估
    val evaluator = new RegressionEvaluator()
      .setMetricName("rmse")
      .setLabelCol("rating")
      .setPredictionCol("prediction")
    val rmse = evaluator.evaluate(predictions)
    println(s"Root-mean-square error = $rmse")

/*    val paramGrid = new ParamGridBuilder()
      .addGrid(als.rank, Array[Int](5, 10, 20))
      .addGrid(als.regParam, Array[Double](0.05, 0.10, 0.15, 0.20, 0.40, 0.80))
      .build
    val pipeline = new Pipeline() .setStages(Array(als))
    val cv=new CrossValidator()
      .setEstimator(pipeline)
      .setEvaluator(new RegressionEvaluator()
        .setLabelCol("rating")
        .setPredictionCol("prediction")
        .setMetricName("rmse")
      )
      .setEstimatorParamMaps(paramGrid)
      .setNumFolds(5);
    val cvModel = cv.fit(training)*/

    //保存模型到HDFS
    model.write.overwrite().save("hdfs://172.17.11.156:9000/data/model/")
    //加载模型
    val sameModel = ALSModel.load("hdfs://172.17.11.156:9000/data/model/")
    //给用户ID推荐5个艺术家ID
    val userRecs = model.recommendForAllUsers(5)
    userRecs.rdd.collect().foreach(println)

    spark.stop()
  }
}

