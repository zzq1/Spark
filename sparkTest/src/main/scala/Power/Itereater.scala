package Power

import org.apache.spark.sql.{SaveMode, SparkSession}

/**
  * Created by zzq on 2018/1/20.
  */
object Itereater {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder.master("local[2]").appName("SparkHiveJob").getOrCreate
    val power_bin = spark.read
      .option("inferSchema", "true")
      .option("header","true")
      .option("inferSchema", "true")
      .csv("hdfs://master:9000/data/power_bin/")
    val pp = power_bin.drop("up").drop("down")
    pp.coalesce(1).write.option("header","true").mode(SaveMode.Overwrite).csv("hdfs://master:9000/data/power_bin")
//    power_bin.printSchema()
//    println(power_bin.count())
    import org.apache.spark.sql.functions._
//    val power_by_bin = power_bin.groupBy("BIN").count().sort("BIN")
//    val power_avg_power = power_bin.groupBy("BIN").agg(("POWER","avg"),("WIND_SPEED","avg"))
//    val power_bin_avgP = power_by_bin.join(power_avg_power,"BIN")
////    power_by_bin.show()
////    power_avg_power.show()
//    power_bin_avgP.show()
  }
}
