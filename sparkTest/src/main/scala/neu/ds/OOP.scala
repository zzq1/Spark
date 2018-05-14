package neu.ds


import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{Dataset, SQLContext, SparkSession}

/**
  * Created by taos on 2017/6/10.
  */
object OOP {
  case  class  Person(name: String, age: Long)
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().master("local[4]").appName("OOP").getOrCreate()
    val sc = spark.sparkContext
//    val sqlContext = new SQLContext(sc)
    //val root = ReadFromParquet.getClass.getResource("/")
    val dataframe = spark.read.json("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第四次课\\data\\people.json")
    import spark.implicits._
    //映射为ds 对象，通过Person 对象的
    val ds: Dataset[Person] = dataframe.as[Person]
    ds.show()
//
//    val hist = ds.groupBy(_.name).mapGroups({
//      case (name, people) => {
//        val list = people.map(_.age).toList
//        (name, list)
//      }})
  }
}