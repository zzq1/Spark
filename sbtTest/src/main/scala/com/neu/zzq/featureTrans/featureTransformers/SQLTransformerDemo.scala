package com.neu.zzq.featureTrans.featureTransformers

import org.apache.spark.ml.feature.SQLTransformer
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by taos on 2017/6/24.
  */
object SQLTransformerDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("SQLTransformerDemo")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    val df = sqlContext.createDataFrame(
      Seq((0, 1.0, 3.0), (2, 2.0, 5.0))).toDF("id", "v1", "v2")
    //改表名
    df.registerTempTable("a")

    val sqlTrans = new SQLTransformer().setStatement(
      "SELECT *, (v1 + v2) AS v3, (v1 * v2) AS v4 FROM a")

    sqlTrans.transform(df).show()
  }
}
