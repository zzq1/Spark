package org.apache.spark.examples.ml

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.recommendation.ALS
import org.apache.spark.ml.recommendation.ALSModel
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}

import scala.collection.mutable

object ALSExample {

  case class Rating(userId: Int, movieId: Int, rating: Float, timestamp: Long)
  def parseRating(str: String): Rating = {
    val fields = str.split("::")
    assert(fields.size == 4)
    Rating(fields(0).toInt, fields(1).toInt, fields(2).toFloat, fields(3).toLong)
  }

  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .master("local[4]")
      .appName("ALSExample")
      .getOrCreate()
    import spark.implicits._
    val root = "C:/Users/zzq/IdeaProjects/spark-master/"
    val ratings = spark.read.textFile(root + "data/mllib/als/sample_movielens_ratings.txt")
      .map(parseRating)
      .toDF()
    ratings.show()
    val Array(training, test) = ratings.randomSplit(Array(0.8, 0.2))

    val als = new ALS()
      .setMaxIter(5)
      .setRegParam(0.01)
      .setUserCol("userId")
      .setItemCol("movieId")
      .setRatingCol("rating")
    val model = als.fit(training)

    // Evaluate the model by computing the RMSE on the test data
    // Note we set cold start strategy to 'drop' to ensure we don't get NaN evaluation metrics
    model.setColdStartStrategy("drop")
    val predictions = model.transform(test)


    val evaluator = new RegressionEvaluator()
      .setMetricName("rmse")
      .setLabelCol("rating")
      .setPredictionCol("prediction")
    val rmse = evaluator.evaluate(predictions)
    println(s"Root-mean-square error = $rmse")


    val paramGrid = new ParamGridBuilder()
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
    val cvModel = cv.fit(training)

    cvModel.transform(test)
      .select("userId", "movieId", "probability", "prediction")
      .collect()
      .foreach { case Row(id: Long, text: Long, prob: Double, prediction: Double) =>
        println(s"($id, $text) --> prob=$prob, prediction=$prediction")
      }
//    val userRecs = model.recommendForAllUsers(10)
//    val userRecs_1 =userRecs.rdd.map(x=>{
//      (x.get(0).toString.toInt,x.get(1))
//    })
//    val aa = userRecs_1.map(x=>{
//      (x._1,x._2.asInstanceOf[Seq[Vector[Double]]].toArray)
//    })


    //userRecs.rdd.map(row =>row.get(1).toSeq.map(x=>x.toSeq.apply(0))).foreach(println)
    // Generate top 10 user recommendations for each movie
//    val movieRecs = model.recommendForAllItems(10)
//    import spark.implicits._
//    println("movieRecs")
//    movieRecs.show()
//    // Generate top 10 movie recommendations for a specified set of users
//    val users = ratings.select(als.getUserCol).distinct().limit(3)
 //   val userSubsetRecs = model.recommendForUserSubset(users, 10)
//    // Generate top 10 user recommendations for a specified set of movies
//    val movies = ratings.select(als.getItemCol).distinct().limit(3)
//    val movieSubSetRecs = model.recommendForItemSubset(movies, 10)
    // $example off$
    //userRecs.show()
    //movieRecs.show()
//    userSubsetRecs.show()
//    movieSubSetRecs.show()
    //model.save("target/tmp/myCollaborativeFilter")
    //val sameModel = ALSModel.load("target/tmp/myCollaborativeFilter")
//    println(s"Root-mean-square error = $rmse")
//    predictions.rdd.collect().foreach(println)
//    spark.stop()
  }
}
// scalastyle:on println

