package com.horizon.ss

import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by Administrator on 2017/6/10.
  */
object TextFileStreaming {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("TextFileStreaming").setMaster("local[2]")
    val ssc = new StreamingContext(sparkConf, Seconds(1))

    val wordcount = ssc.textFileStream("hdfs://idh104:8020/data")
      .flatMap(_.split(" ")) //all words
      .map(word => (word, 1)) //to pair
      .reduceByKey(_ + _) //count

    wordcount.print()

    ssc.start()
    ssc.awaitTermination()
  }
}
