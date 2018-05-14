package neu.zzq.RecommendSystem

import java.text.SimpleDateFormat

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SQLContext, SaveMode}

/**
  * Created by Administrator on 2018/1/24.
  */
object Increased {
  private var time = "2018-01-26 18:33:44"
  def main(args: Array[String]): Unit = {

    val driver = "com.mysql.jdbc.Driver"
    val url = "jdbc:mysql://172.17.11.156/wind_analysis"
    val username = "hive"
    val password = "123456"
    val tablename = "TB_SYS_USER_MUSIC"
    val dataPath = "hdfs://172.17.11.156:9000/data/rattingData/"

    val conf = new SparkConf()
      .setMaster("local[*]").setAppName("s") //设置APP 的name，设置Local 模式的CPU资源
    val sc = new SparkContext(conf)//只有一个
    val sqlContext = new SQLContext(sc)
//    Class.forName(driver)
    val df = sqlContext.read.format("jdbc").options(Map("url"->url,
      "driver" -> driver,
      "dbtable"->tablename,
      "user"->username,"password"->password)).load()
    df.registerTempTable(tablename)
    val newSql = "SELECT USER_ID,ARTIST_ID,COUNT(*) as count from TB_SYS_USER_MUSIC WHERE UPDATE_TIME>'"+time+"' GROUP BY USER_ID,ARTIST_ID"
    val hdfsdf = sqlContext.read.option("inferSchema", "true").option("delimiter", ",")
      .option("header","true").parquet(dataPath).distinct().toDF("USER_ID","ARTIST_ID","number")
    val newdf = sqlContext.sql(newSql)

    hdfsdf.head(10).foreach(println)
    newdf.show()

//    val dd = hdfsdf.unionAll(newdf)
////
//    dd.head(10).foreach(println)

    val ansdf = hdfsdf.join(newdf,Seq("USER_ID","ARTIST_ID"),"outer")
    val filldf = ansdf.na.fill(0)
    filldf.head(10).foreach(println)
    val newans = filldf.withColumn("newCount",filldf("count")+filldf("number"))
    newans.createOrReplaceTempView("temple")
    val selectsql = "SELECT USER_ID,ARTIST_ID,newCount as count from temple"
    sqlContext.sql("REFRESH TABLE temple")
    val result = sqlContext.sql(selectsql)
    println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
    result.show()

//    newdf.show()
//    val newdf = df.where("UPDATE_TIME>'"+time+"'").select("")
//    val dfRDD = newdf.rdd.map(line=>{
//      ((line.getInt(3),line.getInt(5)),line.getInt(4))
//    })
//    dfRDD.groupByKey().
//    result.show()

     result.coalesce(1).write.mode(SaveMode.Overwrite).parquet("hdfs://172.17.11.156:9000/data/rattingData/"+System.currentTimeMillis())

    val sql = "SELECT MAX(UPDATE_TIME) as time from TB_SYS_USER_MUSIC"
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val ans = sqlContext.sql(sql).head(1).map((each)=>{
      val s = each.getTimestamp(10)
      val  ss = sdf.format(s)
      ss
    })
    ans.foreach(println)
    time = ans.apply(0)
  }
}
