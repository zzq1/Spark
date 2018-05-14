package neu.test.createDF

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SQLContext, SparkSession}

/**
  * Created by TAOS on 2017/6/10.
  * 读取json 文件，直接生成DF
  */
object ReadJSON {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().master("local[4]").appName("ReadJSON").getOrCreate()
    //val sqlContext = new SQLContext(sc)
    //val root = CreateSchema.getClass.getResource("/")
    val df1 = spark.read.format("json").load("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第四次课\\data\\people.json")
    df1.show()
    val df2 = spark.read.json( "C:\\Users\\zzq\\Desktop\\spark\\Spark\\第四次课\\data\\people.json")
    df2.show()
  }

}
