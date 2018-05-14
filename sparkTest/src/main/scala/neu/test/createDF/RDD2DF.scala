package neu.test.createDF

import org.apache.spark.sql.SparkSession

/**
  * Created by taos on 2017/11/29.
  */
object RDD2DF {
  case class Person(name: String, age: Int)
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().master("local[4]").appName("RDD2DF").getOrCreate()
    import spark.implicits._
    //val root = CreateSchema.getClass.getResource("/")
    val peopleDF = spark.sparkContext
      .textFile("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第四次课\\data\\people.txt")
      .map(_.split(","))
      .map(attributes => Person(attributes(0), attributes(1).trim.toInt))
      .toDF()
    peopleDF.show()
  }
}
