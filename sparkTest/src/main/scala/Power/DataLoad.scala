package Power

import org.apache.spark.sql.{Row, SaveMode, SparkSession}


/**
  * Created by zzq on 2018/1/18.
  */
object DataLoad {
  def main(args: Array[String]): Unit = {
      case class Tenmindata(DATASOURCE:String,FAN_NO:String ,DATA_DATE:String,FAN_STATUS:Double,WIND_SPEED:Double,ROTOR_RS:Double,RS:Double,
      WIND_DIRECTION:Double, YAW_ANGLE:Double,BOX_T:Double,BOX_BEARING_T:Double,ENVIRON_T:Double,WT_T:Double,ROTOR_GROUP_T:Double,
    A_PHASE_C:Double,B_PHASE_C:Double,C_PHASE_C:Double,A_PHASE_V:Double,B_PHASE_V:Double,C_PHASE_V:Double,MACHINE_FREQUENCY:Double,
    REACTIVE_POWER:Double,POWER:Double,POWER_FACTOR:Double,TOTAL_POWER:Double,TOTAL_GEN_TIME:Double,DOWN_TIME:Double,STANDBY_TIME:Double,
    REMARK:Double)
    val spark = SparkSession.builder.master("local[2]").appName("SparkHiveJob").getOrCreate
    val sc = spark.sparkContext
    val sqlContext = spark.sqlContext
    //      val driverName = "org.apache.hive.jdbc.HiveDriver"
    val driverName = "com.mysql.jdbc.Driver"
    Class.forName(driverName)
      val jdbcDF = spark.read.option("inferSchema", "true").csv("hdfs://master:9000/fc=1").na.drop().toDF(
            "DATASOURCE","FAN_NO","DATA_DATE","FAN_STATUS","WIND_SPEED","ROTOR_RS","RS",
            "WIND_DIRECTION","YAW_ANGLE","BOX_T","BOX_BEARING_T","ENVIRON_T","WT_T","ROTOR_GROUP_T",
            "A_PHASE_C","B_PHASE_C","C_PHASE_C","A_PHASE_V","B_PHASE_V","C_PHASE_V","MACHINE_FREQUENCY",
            "REACTIVE_POWER","POWER","POWER_FACTOR","TOTAL_POWER","TOTAL_GEN_TIME","DOWN_TIME","STANDBY_TIME","REMARK","FJ"
      )
    jdbcDF.show(50)
    println(jdbcDF.count())



    val power = spark.read
      .option("inferSchema", "true")
      .option("header","true")
      .option("inferSchema", "true")
      .csv("hdfs://master:9000/data/power/")
    power.cache()
    spark.udf.register("get_month",(a: String) => {
      a.split("-")(0) + a.split("-")(1)
    })
    val power_m = power.selectExpr("FAN_NO","get_month(DATA_DATE)","WIND_SPEED","ENVIRON_T","POWER")
      .toDF("FAN_NO","DATA_DATE","WIND_SPEED","ENVIRON_T","POWER")
    val power_filter_correct = power_m.filter(x=>{
      x.getAs("ENVIRON_T").toString.toDouble >= -50 && x.getAs("ENVIRON_T").toString.toDouble <= 90
    })
    val power_filter_worr = power_m.filter(x=>{
      x.getAs("ENVIRON_T").toString.toDouble < -50 || x.getAs("ENVIRON_T").toString.toDouble > 90
    })

    import org.apache.spark.sql.functions._
    val every_t = power_filter_correct.groupBy("FAN_NO","DATA_DATE").agg(("ENVIRON_T","avg"))
    val power_w_t = power_filter_worr.join(every_t,Seq("FAN_NO", "DATA_DATE"))
      .drop("ENVIRON_T")
      .toDF("FAN_NO","DATA_DATE","WIND_SPEED","POWER","ENVIRON_T")
      .selectExpr("FAN_NO","DATA_DATE","WIND_SPEED","ENVIRON_T","POWER")
    val pr = power_w_t.union(power_filter_correct)
//    power_filter_correct.show()
//    power_filter_worr.show()
//    every_t.show()
//    power_w_t.show()
//    pr.show()
    //将处理后的数据写到hdfs上
    pr.coalesce(1).write.option("header","true").mode(SaveMode.Overwrite).csv("hdfs://master:9000/data/power_processed")

//    power_m.show()



    val jdbcDF_res =jdbcDF.filter(x=>{
       x.getAs("WIND_SPEED").toString.toDouble <= 12 && x.getAs("WIND_SPEED").toString.toDouble >= 3
    }).filter(x=>{
       x.getAs("POWER").toString.toDouble >= -750 && x.getAs("POWER").toString.toDouble <= 3000
    })

    val jdbcDF_res1 = jdbcDF_res.selectExpr("FAN_NO","DATA_DATE","WIND_SPEED","ENVIRON_T","POWER")
    jdbcDF_res1.coalesce(1).write.option("header","true").mode(SaveMode.Overwrite).csv("hdfs://master:9000/data/power")
    jdbcDF_res1.show()
//    println(jdbcDF_res1.count())
//      val mysql_gpc = spark.read.format("jdbc")
////        .option("header","true")
////        .option("inferSchema", "true")
//        .option("url","jdbc:mysql://172.17.11.156:3306/mysql")
//        .option("driver","com.mysql.jdbc.Driver")
//        .option("dbtable","gpc")
//        .option("user","hive")
//        .option("password","123456")
//        .load()
//      mysql_gpc.show()
//      val mysql_gpc_t = spark.read.format("jdbc")
//        .option("url","jdbc:mysql://172.17.11.156:3306/mysql")
////        .option("header","true")
////        .option("inferSchema", "true")
//        .option("driver","com.mysql.jdbc.Driver")
//        .option("dbtable","gpc_template")
//        .option("user","hive")
//        .option("password","123456").load()
//      mysql_gpc_t.show()
//    val mysql_basicInfo = spark.read.format("jdbc")
//      .option("url","jdbc:mysql://172.17.11.156:3306/mysql")
//      //        .option("header","true")
//      //        .option("inferSchema", "true")
//      .option("driver","com.mysql.jdbc.Driver")
//      .option("dbtable","basicinfo")
//      .option("user","hive")
//      .option("password","123456").load()
//    mysql_basicInfo.show()
//    jdbcDF.createOrReplaceTempView("dftable")
//    val ss = sqlContext.sql("select * from dftable limit 10")
//    ss.show()
//
//
////      val conf=new SparkConf().setAppName("SparkSql running......................").setMaster("local")
////      val sc=new SparkContext(conf)
////      val sqlContext=new SQLContext(sc)//10.0.190.102
////
////      var jdbcDf=sqlContext.read.format("jdbc").options(Map("url"->"jdbc:mysql://10.10.190.102:3306/db",
////          "driver" -> "com.mysql.jdbc.Driver",
////          "dbtable"->"temp",
////          "user"->"root","password"->"root")).load()
    }
}
