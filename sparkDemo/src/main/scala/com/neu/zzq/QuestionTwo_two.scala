package com.neu.zzq

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by zzq on 2017/11/29.
  */
object QuestionTwo_two {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[4]").setAppName("net")
    val sc = new SparkContext(conf)
    val users = sc.textFile("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第三次课\\ml-1m\\users.dat")
    val movies = sc.textFile("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第三次课\\ml-1m\\movies.dat")
    val ratings = sc.textFile("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第三次课\\ml-1m\\ratings.dat")
    users.cache()
    ratings.cache()
    movies.cache()
    val rating = ratings.flatMap{line_rating =>
      val a = line_rating.split("::")
      if (!a(0).isEmpty) Some(a(0),a(1)) else None
    }
    rating.map((_,1)).reduceByKey(_+_).map(x => (x._1._1)).map((_,1)).reduceByKey(_+_).sortBy(_._2,false).take(10).foreach(println)
  }
}
