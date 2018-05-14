package neu.zzq.classfour

import org.apache.spark.sql.{SaveMode, SparkSession}

/**
  * Created by zzq on 2017/12/1.
  */
object QuestionTwo {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().master("local[4]").appName("QuestionTwo").getOrCreate()
    val df_198 = spark.read.csv("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第四次课\\basketball\\leagues_NBA_198*").select("_c3").toDF("age")
      .filter((_.get(0) != "Age"))
    val df_199 = spark.read.csv("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第四次课\\basketball\\leagues_NBA_199*").select("_c3").toDF("age")
      .filter((_.get(0) != "Age"))
    val df_20 = spark.read.csv("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第四次课\\basketball\\leagues_NBA_20*").select("_c3").toDF("age")
      .filter((_.get(0) != "Age"))
    df_198.cache()
    df_199.cache()
    df_20.cache()
    val res_198 = df_198.groupBy("age").count().sort("age")
    val res_199 = df_199.groupBy("age").count().sort("age")
    val res_20 = df_20.groupBy("age").count().sort("age")
    val res = res_20.union(res_199).union(res_198)
    val ageSort = res.groupBy("age").agg(("count","sum")).sort("age").toDF("age","count")
    ageSort.coalesce(1).write.mode(SaveMode.Overwrite).csv("C:\\Users\\zzq\\Desktop\\spark第八次作业-20155120-周志强\\ageSort")
//    ageSort.show(50)
//    res_198.show(50)
//    res_199.show(50)
//    res_20.show(50)

  }
}

