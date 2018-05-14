package neu.test.createDF

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{Row, SQLContext, SaveMode, SparkSession}
import org.apache.spark.sql.types.{StringType, StructField, StructType}

/**
  * Created by taos on 2017/6/9.
  */
object CreateSchema {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().master("local[4]").appName("CreateSchema").getOrCreate()
    //val conf = new SparkConf().setAppName("CreateSchema").setMaster("local")
    val schemaString = "userID gender age occupation zipcode"
    val schema = StructType(schemaString.split(" ").map(fieldName => StructField(fieldName, StringType, true)))

    //val root = CreateSchema.getClass.getResource("/")
    val usersRdd = spark.sparkContext.textFile("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第三次课\\ml-1m\\users.dat")
    val userRDD2 = usersRdd.map(_.split("::")).map(p => Row(p(0), p(1).trim, p(2).trim,
      p(3).trim, p(4).trim))
    val userDataFrame2 = spark.createDataFrame(userRDD2, schema)
    userDataFrame2.take(10).foreach(println)
    println(userDataFrame2.count())
    userDataFrame2.write.mode(SaveMode.Overwrite).json("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第三次课\\ml-1m\\user.json")
    userDataFrame2.write.mode(SaveMode.Overwrite).parquet("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第三次课\\ml-1m\\user.parquet")

  }
}
