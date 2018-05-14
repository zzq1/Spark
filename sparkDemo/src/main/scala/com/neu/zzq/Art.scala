package com.neu.zzq

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by zzq on 2017/11/26.
  */
  object Art {
    def main(args: Array[String]): Unit = {
      val conf=new SparkConf().setMaster("local[4]").setAppName("net")
      val sc=new SparkContext(conf)
      val rdd = sc.textFile("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第二次课\\art.txt.gz")
      rdd.cache()
      val res0 = rdd.flatMap { line =>
        val str = line.split('\t')
        if (str.length == 1) None
        else Some(str(0), str(1))
      }.collectAsMap()
      val res00 = rdd.flatMap { line =>
        val str = line.split('\t')
        if (str.length == 1) None
        else Some(str(0), str(1))
      }
      println(res0.size)
      val rddID = sc.textFile("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第二次课\\art_alias.txt.gz")
      rddID.cache()
      val res1 = rddID.flatMap { line =>
        val str = line.split('\t')
        if (str(0).isEmpty) None
        else Some(str(0), str(1))
      }.collectAsMap()
      val res = res0.flatMap(line =>
        if (!(res1.get(line._1).isEmpty)) Some(res1.get(line._1), line._2) else Some(line)
      )
      //res.foreach(println)
  }
}
