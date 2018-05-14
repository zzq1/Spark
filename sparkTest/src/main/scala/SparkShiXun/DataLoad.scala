package SparkShiXun

import org.apache.spark.sql.SparkSession

/**
  * Created by zzq on 2018/1/24.
  */
object DataLoad {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder.master("local[2]").appName("SparkHiveJob").getOrCreate
    val sc = spark.sparkContext
    val sqlContext = spark.sqlContext
    val artist_alias = spark.read.option("inferSchema", "true").csv("hdfs://master:9000/data/artist_alias.txt").na.drop()
    artist_alias.show()
    val artist_data = spark.read.option("inferSchema", "true").csv("hdfs://master:9000/data/artist_data.txt").na.drop()
    artist_data.show()
    val user_artist_data = spark.read.option("inferSchema", "true").csv("hdfs://master:9000/data/user_artist_data.txt").na.drop()
    user_artist_data.show()

  }

}
