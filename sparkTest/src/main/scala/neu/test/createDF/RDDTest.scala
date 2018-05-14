package neu.test.createDF

import org.apache.spark.sql.SparkSession

/**
  * Created by zzq on 2017/11/29.
  */
object RDDTest {
  def main(args: Array[String]): Unit = {
    //val spark = SparkSession.builder().master("local[4]").appName("AccumulatorBaisc").getOrCreate()
   // val sc = spark.sparkContext

    val spark = SparkSession.builder().master("local[4]").appName("AccumulatorBaisc").getOrCreate()
    val sc = spark.sparkContext
    //val accum = sc.longAccumulator("My Accumulator")
    //val sc=new SparkContext(conf)
    val statesPopulationRDD = sc.textFile("C:\\Users\\zzq\\Desktop\\Scala-and-Spark-for-Big-Data-Analytics-master\\data\\data\\statesPopulation.csv")
    statesPopulationRDD.take(5)
    val acc1 = sc.longAccumulator("acc1")
    val someRDD = statesPopulationRDD.map(x => {acc1.add(1); x})
    println(acc1.value)
    println(someRDD.count)
    println(acc1.value)
    println(acc1)
  }
}
