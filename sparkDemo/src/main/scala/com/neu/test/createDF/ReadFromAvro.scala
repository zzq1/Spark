package com.neu.test.createDF

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SQLContext, SparkSession}
/**
  * Created by taos on 2017/6/10.
  */
  object ReadFromAvro {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("ReadFromAvro")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    //val root = ReadFromAvro.getClass.getResource("/")
    //val spark = SparkSession.builder().master("local[4]").appName("ReadFromAvro").getOrCreate()
    //val sc = spark.sparkContext
    //val sqlContext = new SQLContext(sc)
    val df1 = sqlContext.read.format("com.databricks.spark.avro").load("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第四次课\\data\\users.avro")
    df1.show()
  }
}
