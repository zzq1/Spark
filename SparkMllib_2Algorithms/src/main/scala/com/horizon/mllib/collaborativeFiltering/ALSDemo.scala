package com.horizon.mllib.collaborativeFiltering

import com.horizon.mllib.classification2Regression.SVMWithSGDDemo
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.mllib.recommendation.Rating
/**
  * Created by taos on 2017/6/20.
  */
object ALSDemo {
  def main(args: Array[String]): Unit = {
    // Load and parse the data
    val conf =  new SparkConf().setAppName("ALSDemo").setMaster("local")
    val sc = new SparkContext(conf)
    //val root = ALSDemo.getClass.getResource("/")
    val root = "C:/Users/zzq/IdeaProjects/SparkMllib_2Algorithms/resource/"
    val data = sc.textFile(root + "als/test.data")
    val ratings = data.map(_.split(',') match { case Array(user, item, rate) =>
      Rating(user.toInt, item.toInt, rate.toDouble)
    })

    // Build the recommendation model using ALS
    val rank = 10
    val numIterations = 10
    val model = ALS.train(ratings, rank, numIterations, 0.01)

    // Evaluate the model on rating data
    val usersProducts = ratings.map { case Rating(user, product, rate) =>
      (user, product)
    }
    val predictions =
      model.predict(usersProducts).map { case Rating(user, product, rate) =>
        ((user, product), rate)
      }
    val ratesAndPreds = ratings.map { case Rating(user, product, rate) =>
      ((user, product), rate)
    }.join(predictions)
    val MSE = ratesAndPreds.map { case ((user, product), (r1, r2)) =>
      val err = (r1 - r2)
      err * err
    }.mean()
    println("Mean Squared Error = " + MSE)

    // Save and load model
    model.save(sc, "target/tmp/myCollaborativeFilter")
    val sameModel = MatrixFactorizationModel.load(sc, "target/tmp/myCollaborativeFilter")
  }
}
