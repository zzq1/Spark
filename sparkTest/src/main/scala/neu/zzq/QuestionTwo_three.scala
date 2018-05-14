package neu.zzq

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by zzq on 2017/11/29.
  */
object QuestionTwo_three {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[4]").setAppName("net")
    val sc = new SparkContext(conf)
    val users = sc.textFile("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第三次课\\ml-1m\\users.dat")
    val movies = sc.textFile("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第三次课\\ml-1m\\movies.dat")
    val ratings = sc.textFile("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第三次课\\ml-1m\\ratings.dat")
    users.cache()
    ratings.cache()
    movies.cache()
    val user = users.flatMap{ line_user =>
      val a = line_user.split("::")
      if (a(1) == "F") Some(a(0),a(1)) else None
    }
    val movie = movies.flatMap{line_movie =>
      val c = line_movie.split("::")
      if (!c(0).isEmpty) Some(c(0),c(1)) else None
    }
    val rating = ratings.flatMap{line_rating =>
      val b = line_rating.split("::")
      if (!b(0).isEmpty) Some(b(0),b(1)) else None
    }
    val u_r = rating.join(user).map(x => (x._2._1,x._1))
    val u_r_m = u_r.join(movie).map(x => (x._2._2)).map((_,1)).reduceByKey(_+_).sortBy(_._2,false).take(10)
    u_r_m.foreach(println)
  }
}
