package com.neu.zzq.KafkaHBase

import com.alibaba.fastjson.JSON
import kafka.serializer.StringDecoder
import org.apache.hadoop.hbase.client.{ConnectionFactory, Put}
import org.apache.hadoop.hbase.{HBaseConfiguration, HColumnDescriptor, HTableDescriptor, TableName}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.util.Try

object NetworkWordCount {
  Logger.getLogger("org").setLevel(Level.ERROR)
  case class wordcount(word:String,count:Long)
  def main(args: Array[String]) {
    val sparkConf = new SparkConf().setAppName("NetworkWordCount").setMaster("local[2]")
    val sc = new SparkContext(sparkConf)
    val ssc = new StreamingContext(sc,Seconds(1))
    val topics = KafkaProperties.KAFKA_USER_TOPIC.split("\\,").toSet
    val brokers = KafkaProperties.KAFKA_ADDR
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers, "serializer.class" -> "kafka.serializer.StringEncoder")
    val kafkaStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)
    val events = kafkaStream.flatMap(line => {
      println(s"Line ${line}.")
      val data = JSON.parseObject(line._2)
      Some(data)
    })
    val words = events.map(x => {
      new wordcount(x.getString("uid"),x.getLong("click_count"))
    })
    val res = words.foreachRDD(rdd => {
      val sqlContex = new SQLContext(sc)
      import sqlContex.implicits._
      val df = rdd.toDF().registerTempTable("wordcount")
      val rr = sqlContex.sql("SELECT word,SUM(count) as sss FROM wordcount GROUP BY word ORDER BY sss DESC  LIMIT 5")
      rr.rdd.foreach(println)
      var r = 0
      rr.rdd.map(x => {
        r += 1
        (r,x.get(0).toString,x.get(1).toString)
      }).foreachPartition(partitionRecords => {
        val conf = HBaseConfiguration.create()
        conf.set("hbase.zookeeper.quorum","172.17.11.156,172.17.11.155,172.17.11.154")
        conf.set("hbase.zookeeper.property.clientPort","2181")
        val conn=ConnectionFactory.createConnection(conf)
        val admin=conn.getAdmin
        val tableName=TableName.valueOf("spark_test")
        if(admin.tableExists(tableName)){
          println("Table exists!")
        }else{
          val tableDesc=new HTableDescriptor(tableName)
          tableDesc.addFamily(new HColumnDescriptor("cf".getBytes))
          admin.createTable(tableDesc)
          println("Create table success!")
        }
        partitionRecords.foreach(s => {
          val table = conn.getTable(tableName)
          val p = new Put(s._1.toString.getBytes())
          p.addColumn("cf".getBytes,"word".getBytes,s._2.toString.getBytes)
          p.addColumn("cf".getBytes,"count".getBytes,s._3.toString.getBytes)
          Try(table.put(p)).getOrElse(table.close())
          table.close()
        })
      })
    })
    ssc.start()
    ssc.awaitTermination()
  }
}
