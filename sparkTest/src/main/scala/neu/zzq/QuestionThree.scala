package neu.zzq

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by zzq on 2017/11/29.
  */
object QuestionThree {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[4]").setAppName("net")
    val sc = new SparkContext(conf)
    val sougou = sc.textFile("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第三次课\\SogouQ2012.mini.tar.gz")
    sougou.cache()
    val sou = sougou.flatMap{
      line =>
        val a = line.split("\t")
        Some(a(5))
    }.map(x =>(x,1)).reduceByKey(_+_).sortBy(_._2,false).take(10)
    sou.foreach(println)
  }
}
