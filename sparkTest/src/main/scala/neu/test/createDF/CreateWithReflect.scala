package neu.test.createDF

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.Row

/**
  * Created by taos on 2017/6/9.
  */
object CreateWithReflect {
  case class User(userID: Long, gender: String, age: Int, occupation: String, zipcode: String)
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("CreateWithReflect").setMaster("local")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    //val root = CreateWithReflect.getClass.getResource("/")
    val usersRdd = sc.textFile("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第三次课\\ml-1m\\users.dat")
    val userRDD = usersRdd.map(_.split("::")).map(p => User(p(0).toLong, p(1).trim, p(2).toInt, p(3), p(4).trim))
    import sqlContext.implicits._
    //rdd 的泛型是User，转为DF 表头自动带出来
    val userDataFrame = userRDD.toDF()
    userDataFrame.show()
    userDataFrame.take(10).foreach(println)
    userDataFrame.count()
  }
}
