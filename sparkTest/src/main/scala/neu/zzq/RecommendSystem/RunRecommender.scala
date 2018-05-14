/*
 * Copyright 2015 Sanford Ryza, Uri Laserson, Sean Owen and Joshua Wills
 *
 * See LICENSE file for further information.
 */

package neu.zzq.RecommendSystem
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.ml.recommendation.ALSModel
import org.apache.spark.mllib.recommendation._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SaveMode, SparkSession}
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.Map
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object RunRecommender {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("Recommender").master("local[4]").getOrCreate()
    val sc = spark.sparkContext
    val base = "hdfs://172.17.11.156:9000/data/"
    val rawUserArtistData = sc.textFile(base + "user_artist_data_1.txt")
    val rawArtistData = sc.textFile(base + "artist_data_1.txt")
    val rawArtistAlias = sc.textFile(base + "artist_alias_1.txt")

    //preparation(rawUserArtistData, rawArtistData, rawArtistAlias)
    model(sc, rawUserArtistData, rawArtistData, rawArtistAlias)
    //evaluate(spark,sc, rawUserArtistData, rawArtistAlias)
    recommend(sc, rawUserArtistData, rawArtistData, rawArtistAlias)
  }

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

  def preparation(
      rawUserArtistData: RDD[String],
      rawArtistData: RDD[String],
      rawArtistAlias: RDD[String]) = {
    val userIDStats = rawUserArtistData.map(_.split(' ')(0).toDouble).stats()
    val itemIDStats = rawUserArtistData.map(_.split(' ')(1).toDouble).stats()
    println(userIDStats)
    println(itemIDStats)

    val artistByID = buildArtistByID(rawArtistData)
    val artistAlias = buildArtistAlias(rawArtistAlias)

    val (badID, goodID) = artistAlias.head
    println(artistByID.lookup(badID) + " -> " + artistByID.lookup(goodID))
  }

  def buildRatings(
      rawUserArtistData: RDD[String],
      bArtistAlias: Broadcast[Map[Int,Int]]) = {
    rawUserArtistData.map { line =>
      val Array(userID, artistID, count) = line.split(' ').map(_.toInt)
      val finalArtistID = bArtistAlias.value.getOrElse(artistID, artistID)
      Rating(userID, finalArtistID, count)
    }
  }

  def model(
      sc: SparkContext,
      rawUserArtistData: RDD[String],
      rawArtistData: RDD[String],
      rawArtistAlias: RDD[String]): Unit = {

    val bArtistAlias = sc.broadcast(buildArtistAlias(rawArtistAlias))

    val trainData = buildRatings(rawUserArtistData, bArtistAlias).cache()

    val model = ALS.trainImplicit(trainData, 10, 5, 0.01, 1.0)
    //保存模型到HDFS
    model.save(sc,"hdfs://172.17.11.156:9000/data/model/")


    trainData.unpersist()
    println(trainData)


    println(model.userFeatures.mapValues(_.mkString(", ")).first())

    val userID = 1000002
    val recommendations = model.recommendProducts(userID, 2)
    recommendations.foreach(println)
    val recommendedProductIDs = recommendations.map(_.product).toSet

    val rawArtistsForUser = rawUserArtistData.map(_.split(' ')).
      filter { case Array(user,_,_) => user.toInt == userID }

    val existingProducts = rawArtistsForUser.map { case Array(_,artist,_) => artist.toInt }.
      collect().toSet

    val artistByID = buildArtistByID(rawArtistData)

    artistByID.filter { case (id, name) => existingProducts.contains(id) }.
      values.collect().foreach(println)
    artistByID.filter {
      case (id, name) => recommendedProductIDs.contains(id) }.
      values.collect().foreach(println)

    unpersist(model)
  }

  def areaUnderCurve(
      positiveData: RDD[Rating],
      bAllItemIDs: Broadcast[Array[Int]],
      predictFunction: (RDD[(Int,Int)] => RDD[Rating])) = {
    // What this actually computes is AUC, per user. The result is actually something
    // that might be called "mean AUC".

    // Take held-out data as the "positive", and map to tuples
    val positiveUserProducts = positiveData.map(r => (r.user, r.product))
    // Make predictions for each of them, including a numeric score, and gather by user
    val positivePredictions = predictFunction(positiveUserProducts).groupBy(_.user)

    // BinaryClassificationMetrics.areaUnderROC is not used here since there are really lots of
    // small AUC problems, and it would be inefficient, when a direct computation is available.

    // Create a set of "negative" products for each user. These are randomly chosen
    // from among all of the other items, excluding those that are "positive" for the user.
    val negativeUserProducts = positiveUserProducts.groupByKey().mapPartitions {
      // mapPartitions operates on many (user,positive-items) pairs at once
      userIDAndPosItemIDs => {
        // Init an RNG and the item IDs set once for partition
        val random = new Random()
        val allItemIDs = bAllItemIDs.value
        userIDAndPosItemIDs.map { case (userID, posItemIDs) =>
          val posItemIDSet = posItemIDs.toSet
          val negative = new ArrayBuffer[Int]()
          var i = 0
          // Keep about as many negative examples per user as positive.
          // Duplicates are OK
          while (i < allItemIDs.size && negative.size < posItemIDSet.size) {
            val itemID = allItemIDs(random.nextInt(allItemIDs.size))
            if (!posItemIDSet.contains(itemID)) {
              negative += itemID
            }
            i += 1
          }
          // Result is a collection of (user,negative-item) tuples
          negative.map(itemID => (userID, itemID))
        }
      }
    }.flatMap(t => t)
    // flatMap breaks the collections above down into one big set of tuples

    // Make predictions on the rest:
    val negativePredictions = predictFunction(negativeUserProducts).groupBy(_.user)

    // Join positive and negative by user
    positivePredictions.join(negativePredictions).values.map {
      case (positiveRatings, negativeRatings) =>
        // AUC may be viewed as the probability that a random positive item scores
        // higher than a random negative one. Here the proportion of all positive-negative
        // pairs that are correctly ranked is computed. The result is equal to the AUC metric.
        var correct = 0L
        var total = 0L
        // For each pairing,
        for (positive <- positiveRatings;
             negative <- negativeRatings) {
          // Count the correctly-ranked pairs
          if (positive.rating > negative.rating) {
            correct += 1
          }
          total += 1
        }
        // Return AUC: fraction of pairs ranked correctly
        correct.toDouble / total
    }.mean() // Return mean AUC over users
  }

  def predictMostListened(sc: SparkContext, train: RDD[Rating])(allData: RDD[(Int,Int)]) = {
    val bListenCount =
      sc.broadcast(train.map(r => (r.product, r.rating)).reduceByKey(_ + _).collectAsMap())
    allData.map { case (user, product) =>
      Rating(user, product, bListenCount.value.getOrElse(product, 0.0))
    }
  }

  def evaluate(
                spark:SparkSession,
      sc: SparkContext,
      rawUserArtistData: RDD[String],
      rawArtistAlias: RDD[String]): Unit = {
    val bArtistAlias = sc.broadcast(buildArtistAlias(rawArtistAlias))

    val allData = buildRatings(rawUserArtistData, bArtistAlias)
    val Array(trainData, cvData) = allData.randomSplit(Array(0.9, 0.1))
    trainData.cache()
    cvData.cache()

    val allItemIDs = allData.map(_.product).distinct().collect()
    val bAllItemIDs = sc.broadcast(allItemIDs)

    val mostListenedAUC = areaUnderCurve(cvData, bAllItemIDs, predictMostListened(sc, trainData))
    println(mostListenedAUC)

    val evaluations =
      for (rank   <- Array(10,  50);
           lambda <- Array(1.0, 0.0001);
           alpha  <- Array(1.0, 40.0))
      yield {
        val model = ALS.trainImplicit(trainData, rank, 10, lambda, alpha)
        val auc = areaUnderCurve(cvData, bAllItemIDs, model.predict)
        unpersist(model)
        ((rank, lambda, alpha), auc)
      }

    evaluations.sortBy(_._2).reverse.foreach(println)

    val aa = trainData.collect()
    println("999999999999999999999999")
    spark.createDataFrame(trainData).coalesce(1)
      .write.mode(SaveMode.Overwrite)
      .parquet("hdfs://172.17.11.156:9000/data/rattingData/")
    /*    val schemaString="id,name,age"
    val fields=schemaString.split(",").map(fieldName => StructField(fieldName, StringType, nullable = true))
    val schema=StructType(fields)
    val rowRDD=stuRDD.map(_.split(",")).map(parts⇒Row(parts(0),parts(1),parts(2)))
    val stuDf=spark.createDataFrame(rowRDD, schema)
    stuDf.printSchema()*/
//    val schemaString="USER_ID,ARTIST_ID,number"
//    val fields=schemaString.split(",").map(fieldName => StructField(fieldName, StringType, nullable = true))
//    val schema=StructType(fields)
//    trainData.collect().map(x=>{
//      (x.user,x.product,x.rating)
//    }).foreach()
  cvData.unpersist()
}

  def recommend(
      sc: SparkContext,
      rawUserArtistData: RDD[String],
      rawArtistData: RDD[String],
      rawArtistAlias: RDD[String]): Unit = {

    val bArtistAlias = sc.broadcast(buildArtistAlias(rawArtistAlias))
    val allData = buildRatings(rawUserArtistData, bArtistAlias).cache()
    val model = ALS.trainImplicit(allData, 50, 10, 1.0, 40.0)
    allData.unpersist()

    val userID = 1000002
    val sameModel = ALSModel.load("hdfs://172.17.11.156:9000/data/model/")
    val recommendations = model.recommendProducts(userID, 2)
    //val recommendations = s.recommendProducts(userID, 2)
    val recommendedProductIDs = recommendations.map(_.product).toSet

    val artistByID = buildArtistByID(rawArtistData)

    artistByID.filter { case (id, name) => recommendedProductIDs.contains(id) }.
       values.collect().foreach(println)

    val someUsers = allData.map(_.user).distinct().take(100)
    val someRecommendations = someUsers.map(userID => model.recommendProducts(userID, 5))
    someRecommendations.map(
      recs => recs.head.user + " -> " + recs.map(_.product).mkString(", ")
    ).foreach(println)
    unpersist(model)
  }

  def unpersist(model: MatrixFactorizationModel): Unit = {
    // At the moment, it's necessary to manually unpersist the RDDs inside the model
    // when done with it in order to make sure they are promptly uncached
    model.userFeatures.unpersist()
    model.productFeatures.unpersist()
  }

}