package neu.test.createDF

import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by taos on 2017/6/10.
  */
object ReadFromParquet {
  def main(args: Array[String]): Unit = {
     //val conf = new SparkConf().setMaster("local").setAppName("ReadFromParquet")
    val spark = SparkSession.builder().master("local[4]").appName("CreateSchema").getOrCreate()
     val sc = spark.sparkContext
     val sqlContext = new SQLContext(sc)
     //val root = ReadFromParquet.getClass.getResource("/")
     val df1 = sqlContext.read.format("parquet").load("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第四次课\\data\\users.parquet")
    //df1.show()
     val df2 = sqlContext.read.parquet("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第四次课\\data\\users.parquet")
    df2.show()
  }
}
