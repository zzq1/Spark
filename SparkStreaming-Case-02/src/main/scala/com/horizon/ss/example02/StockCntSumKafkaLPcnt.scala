package com.horizon.ss.example02

import java.sql.{DriverManager, Connection}
import java.util.Date
import java.util.regex.Pattern

import kafka.serializer.StringDecoder
import org.apache.commons.lang.time.DateFormatUtils
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkContext, SparkConf}

import scala.collection.mutable

import scala.collection.immutable.ListMap
//使用sparkSQL
object StockCntSumKafkaLPcnt {


  case class Tracklog(dateday: String, datetime: String, ip: String, cookieid: String, userid: String, logserverip: String, referer: String, requesturl: String, remark1: String,
                      remark2: String, alexaflag: String, ua: String)


  def main(args: Array[String]) {
    val smap = new mutable.HashMap[String, Integer]()

    val url = "jdbc:mysql://10.130.3.211:3306/charts"
    val user = "dbcharts"
    val password = "Abcd1234"

    val conf = new SparkConf().setAppName("stocker") //.setMaster("local[2]")
    val sc = new SparkContext(conf)

    val ssc = new StreamingContext(sc, Seconds(15)) //两个方法创建spark-streaming

    // Kafka configurations

    val topics = Set("teststreaming")

    val brokers = "bdc46.hexun.com:9092,bdc53.hexun.com:9092,bdc54.hexun.com:9092"

    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers, "serializer.class" -> "kafka.serializer.StringEncoder")

    // Create a direct stream
    val kafkaStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

    val events = kafkaStream.flatMap(line => {
      Some(line.toString())
    })

    try {
      val tmpdf = events.map(_.split(" ")).filter(_.length >= 11).map(x => Tracklog(x(0), x(1), x(2), x(3), x(4), x(5), x(6), x(7), x(8), x(9), x(10), x(11)))
      tmpdf.foreachRDD { rdd =>
        val sqlContext = new org.apache.spark.sql.SQLContext(sc)

        import sqlContext.implicits._
        val df = rdd.toDF().registerTempTable("tracklog")
        //自定义函数
        sqlContext.udf.register("strLen", (str: String) => str.length())

        sqlContext.udf.register("concat", (str1: String, str2: String, str3: String) => str1 + str2 + str3)

        sqlContext.udf.register("regexp_extract", (str: String, pattern: String) => {
          val matcher = Pattern.compile(pattern, 1).matcher(str)
          var res = ""
          while (matcher.find()) {
            res = matcher.group()
          }
          res
        })

        val rcount = sqlContext.sql("SELECT  substring(t.requesturl,strLen(regexp_extract(t.requesturl,'(.*?[^0-9][0|3|6][0][0-9][0-9][0-9][0-9]).*?'))-5,6) stock_code," +
          "concat('http://stockdata.stock.hexun.com/', substring(t.requesturl,strLen(regexp_extract(t.requesturl,'(.*?[^0-9][0|3|6][0][0-9][0-9][0-9][0-9]).*?'))-5,6),'.shtml') url," +
          "count(*) clickcnt " +
          "FROM " +
          "(select distinct dateday,datetime,ip,cookieid,userid,logserverip,referer,requesturl,remark1,remark2,alexaflag,ua from  tracklog where strLen(datetime)=12) t " +
          "WHERE  " +
          "regexp_extract(t.requesturl,'(.*?[^0-9][0|3|6][0][0-9][0-9][0-9][0-9]).*?') <>'' " +
          "and t.requesturl like 'http://stockdata.stock.hexun.com/%shtml' " +
          "and t.requesturl not like '%index%' " +
          "and t.requesturl not like '%fund%' " +
          "group by substring(t.requesturl,strLen(regexp_extract(t.requesturl,'(.*?[^0-9][0|3|6][0][0-9][0-9][0-9][0-9]).*?'))-5,6)  " +
          "order by clickcnt desc " +
          "limit 150")

        var flag:Int = 0


        rcount.collect().foreach(data => {
          flag = 1;
          val stockerId = data.get(0).toString;
          val cnt = smap.get(stockerId)

          println("stockerId: " + stockerId + ", cnt:" + cnt)

          if (cnt == null || cnt.toString.equals("None")) {
            smap += (stockerId -> Integer.parseInt(data.get(2).toString))
          } else if (cnt != null && !cnt.toString.equals("None")) {
            val cntI = smap(stockerId)
            val sum: Integer = Integer.parseInt(data.get(2).toString) + cntI
            smap += (stockerId -> sum)
          }
        })


        if(flag == 1){
          // sort by value
          var idx: Int = 1

          val sortMap = ListMap(smap.toSeq.sortWith(_._2 > _._2): _*)

          val stattime = DateFormatUtils.format(new Date, "yyyy-MM-dd HH:mm:ss")

          val conn: Connection = DriverManager.getConnection(url, user, password)
          val pstat = conn.prepareStatement("INSERT INTO  stock_realtime_analysis_spark (stockId,url,clickcnt,type,recordtime) VALUES (?,?,?,?,?)")

          sortMap foreach {
            case (key, value) =>
              if (idx <= 150) {
                println(key + ",http://stockdata.stock.hexun.com/" + key + ".shtml," + value + "," + stattime)

                pstat.setString(1, key)
                pstat.setString(2, "http://stockdata.stock.hexun.com/" + key + ".shtml")
                pstat.setInt(3, value)
                pstat.setString(4, "01")
                pstat.setString(5, stattime)

                pstat.executeUpdate()
              }
              idx = idx + 1
          }

          pstat.close
          conn.close

          flag == 0
        }

      }
    } catch {
      case e: Exception =>
    }

    ssc.start()
    ssc.awaitTermination()
  }
}