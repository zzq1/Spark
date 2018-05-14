package neu.zzq

import org.apache.spark.{SparkConf, SparkContext}

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint


import scala.collection.mutable.ArrayBuffer
/**
  * Created by taos on 2017/6/1.
  */
object WordCount {
   def main(args: Array[String]): Unit = {
//     if (args.length < 2) {
//       System.err.println("Usage: MyScalaWordCout <input> <output>")
//       System.exit(1)
//     }
     val conf=new SparkConf().setMaster("local[4]").setAppName("net")
     val sc=new SparkContext(conf)
     val lines=sc.textFile("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第二次课\\net.gz")
     lines.cache()
     //val lb:RDD[LabeledPoint] = MLUtils.loadLibSVMFile(sc,"C:\\Users\\zzq\\Desktop\\spark\\Spark\\第二次课\\net.gz")
     ///println(lb)
     lines.flatMap(line=>line.split(",").lastOption)
       .map(word=>(word,1))
       .reduceByKey(_+_)
       .zipWithUniqueId()
       .map(x=>(x._1,x._2.toDouble))
       .map(x=>LabeledPoint(x._2,Vectors.dense(x._1.x._2)))
       .foreach(println)

     //     val conf = new SparkConf().setMaster("local[4]").setAppName("net")
//     val sc = new SparkContext(conf)
//     //val res = sc.broadcast()

//     rdd.flatMap(_.split(",").drop(41)).map((_,1)).reduceByKey(_+_).sortBy(_._1).collect().foreach(println)
     //rdd.flatMap(_.split(",").drop(41)).map((_,1)).reduceByKey(_+_).sortBy(_._1).collect().foreach(println)
   }
}
