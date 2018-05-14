package neu.zzq

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by zzq on 2017/11/29.
  */
object QuestionTwo_one {
  def main(args: Array[String]): Unit = {
    val conf=new SparkConf().setMaster("local[4]").setAppName("net")
    val sc=new SparkContext(conf)
    val users=sc.textFile("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第三次课\\ml-1m\\users.dat")
    val movies=sc.textFile("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第三次课\\ml-1m\\movies.dat")
    val ratings=sc.textFile("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第三次课\\ml-1m\\ratings.dat")
    users.cache()
    ratings.cache()
    movies.cache()
    val rating = ratings.flatMap{line_rating =>
      val a = line_rating.split("::")
      if (!a(0).isEmpty) Some(a(1),a(2)) else None
    }
    val movie = movies.flatMap{line_movie =>
      val b = line_movie.split("::")
      if (!b(0).isEmpty) Some(b(0),b(1)) else None
    }
    val user = users.flatMap{line_user=>
      val a = line_user.split("::")
      if (a(1) == "M" && a(2).toInt == 18) Some(a(0),(a(1),a(2))) else None
    }
    val r_m0 = rating.join(movie).map(x => (x._2._2)).map((_,1)).reduceByKey(_+_)
    //.map(x => (x._2._1,x._2._2)).map((_,1)).reduceByKey(_+_).map(x => (x._1._1,x._1._2)).sortBy(_._1,false).take(10)
    val r_m = rating.join(movie).map(x => (x._2._2,x._2._1.toInt)).reduceByKey(_+_)

    val res = r_m0.join(r_m).map(x => (x._1,x._2._2.toDouble/x._2._1.toDouble)).sortBy(_._2,false).take(10)
//      map(x => (x._1,(x._2,1)))
//      .reduceByKey{(v1,v2) => (v1._1 + v2._1,v1._2+v2._2)}
//      .map{ x =>(x._2._1.toFloat / x._2._2.toFloat, x._1)}.sortByKey(false)
      res.foreach(println)

  }
}
