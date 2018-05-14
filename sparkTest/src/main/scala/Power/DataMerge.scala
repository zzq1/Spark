package Power

import org.apache.spark.sql.{SaveMode, SparkSession}

import scala.collection.mutable

/**
  * Created by zzq on 2018/1/20.
  */
object DataMerge {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder.master("local[2]").appName("SparkHiveJob").getOrCreate
    //BIN划分
    spark.udf.register("get_win_bin",(a: Double) => {
      val b = Math.floor(a)
      val c = a-b.toDouble
      if (c <= 0.25){
        b
      } else if (c >0.25 && c<=0.75) {
        b+0.5
      }else{
        b+1
      }
    })
    val power_res = spark.read
      .option("inferSchema", "true")
      .option("header","true")
      .option("inferSchema", "true")
      .csv("hdfs://master:9000/data/power_processed/")
    power_res.cache()
    val power_res_bin = power_res.selectExpr("FAN_NO","DATA_DATE","WIND_SPEED","ENVIRON_T","POWER","get_win_bin(WIND_SPEED)")
    .toDF("FAN_NO","DATA_DATE","WIND_SPEED","ENVIRON_T","POWER","BIN")

//    power_res_bin.show(50)
//    power_res.show(2)
    //加载GPC数据
    val mysql_gpc = spark.read.format("jdbc")
      .option("header","true")
      .option("inferSchema", "true")
      .option("url","jdbc:mysql://172.17.11.156:3306/mysql")
      .option("driver","com.mysql.jdbc.Driver")
      .option("dbtable","gpc")
      .option("user","hive")
      .option("password","123456")
      .load()
    //第一次power过滤
    val mysql_gpc_up = mysql_gpc.withColumn("up",mysql_gpc("POWER") * 1.4)
    val mysql_gpc_down =  mysql_gpc_up.withColumn("down",mysql_gpc("POWER") * 0.8).select("WIND_SPEED","up","down").toDF("BIN","up","down")
    //mysql_gpc_down.show(20)
//    spark.udf.register("get_win_bin",(a: Double,b:Double,c:Double) => {
//
//    }
    val power_bin = power_res_bin.join(mysql_gpc_down,"BIN")
    //println(power_bin.count())
    val power_bin_res = power_bin.filter(x=>{
      x.getAs("POWER").toString.toDouble <= x.getAs("up").toString.toDouble && x.getAs("POWER").toString.toDouble >= x.getAs("down").toString.toDouble
    })
    power_bin_res.drop("up").drop("down").coalesce(1).write.option("header","true").mode(SaveMode.Overwrite).csv("hdfs://master:9000/data/power_bin")
    //println(power_bin_res.count())
//   power_bin_res.show(50)
    //加载GPC_t数据
    val mysql_gpc_t = spark.read.format("jdbc")
      .option("url","jdbc:mysql://172.17.11.156:3306/mysql")
      .option("header","true")
      .option("inferSchema", "true")
      .option("driver","com.mysql.jdbc.Driver")
      .option("dbtable","gpc_template")
      .option("user","hive")
      .option("password","123456").load()
    mysql_gpc_t.show(2)
    //加载basicInfo 数据
    val mysql_basicInfo = spark.read.format("jdbc")
      .option("url","jdbc:mysql://172.17.11.156:3306/mysql")
      .option("header","true")
      .option("inferSchema", "true")
      .option("driver","com.mysql.jdbc.Driver")
      .option("dbtable","basicinfo")
      .option("user","hive")
      .option("password","123456").load()
    mysql_basicInfo.show(2)
    //创建数据结构Map进行风速阈值筛选算法，后续完成
    val gpc_w = mysql_gpc.select("WIND_SPEED").collectAsList()
    val gpc_p = mysql_gpc.select("POWER").collectAsList()
    var m_down:mutable.Map[Double,Double] = mutable.HashMap()
    println(gpc_w.size())
    println(gpc_p.size())
    val i = 0
    for (i <- 0 to gpc_p.size()-1){
      m_down += (gpc_w.get(i).get(0).toString().toDouble -> gpc_p.get(i).get(0).toString().toDouble * 0.8)
    }
  }
}
