package neu.udf

import org.apache.spark.sql.SparkSession

/**
  * Created by taos on 2017/12/18.
  */
object SparkUDF {
  def main(args: Array[String]): Unit = {
    val root = SparkUDF.getClass.getResource("/")
    val spark =  SparkSession.builder().appName("SparkUDF").master("local[*]").getOrCreate()
    spark.udf.register("str_concat",(a: String, b: String) => a + b)
    val person = spark.read.json(root + "people.json")
    person.createOrReplaceTempView("ppson")
    //person.createGlobalTempView("person")
    //spark.sql("SELECT str_concat(name, age) FROM global_temp.person").show()
    spark.sql("SELECT str_concat(name, age) FROM ppson").show()
  }
}
