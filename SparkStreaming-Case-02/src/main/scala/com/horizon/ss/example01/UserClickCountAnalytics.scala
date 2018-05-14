package com.horizon.ss.example01

import org.apache.spark.SparkConf
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.kafka.KafkaUtils

import kafka.serializer.StringDecoder
import com.alibaba.fastjson.JSON
import com.horizon.ss.example01.util.{KafkaRedisProperties, RedisClient}

object UserClickCountAnalytics {
  def main(args: Array[String]): Unit = {
    var masterUrl = "local[2]"
    if (args.length > 0) {
      masterUrl = args(0)
    }

    // Create a StreamingContext with the given master URL
    val conf = new SparkConf().setMaster(masterUrl).setAppName("UserClickCountStat")
    val ssc = new StreamingContext(conf, Seconds(5))

    // Kafka configurations
    val topics = KafkaRedisProperties.KAFKA_USER_TOPIC.split("\\,").toSet
    println(s"Topics: ${topics}.")

    val brokers = KafkaRedisProperties.KAFKA_ADDR
    val kafkaParams = Map[String, String](
      "metadata.broker.list" -> brokers,
      "serializer.class" -> "kafka.serializer.StringEncoder"
    )

    val clickHashKey = "app::users::click"

    // Create a direct stream
    val kafkaStream = KafkaUtils
        .createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

    val events = kafkaStream.flatMap(line => {
      println(s"Line ${line}.")
      val data = JSON.parseObject(line._2)
      Some(data)
    })

    // Compute user click times
    val userClicks = events.map(x => (x.getString("uid"), x.getLong("click_count"))).reduceByKey(_ + _)
    userClicks.foreachRDD(rdd => {
      rdd.foreachPartition(partitionOfRecords => {
        val jedis = RedisClient.pool.getResource
        partitionOfRecords.foreach(pair => {
          val uid = pair._1
          val clickCount = pair._2
          jedis.hincrBy(clickHashKey, uid, clickCount)
          println(s"Update uid ${uid} to ${clickCount}.")
        })
      })
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
