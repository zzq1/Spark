//package com.horizon.ml
//
//import org.apache.spark.{SparkConf, SparkContext}
//import org.apache.spark.mllib.linalg.{VectorUDT, Vectors}
//import org.apache.spark.sql.{DataFrame, Row, SQLContext}
//import org.apache.spark.sql.types.{StructField, StructType}
//import org.apache.spark.ml.attribute.AttributeGroup
//import org.apache.spark.sql.{DataFrame, Row, SQLContext}
//import org.apache.spark.sql.functions._
///**
//  * Created by Administrator on 2017/6/29.
//  */
//object UDFDemo {
//
//  def vectorAssemble(oriDf:DataFrame,inputCols:Array[String],ouputCol:String):DataFrame={
//    val conf = new SparkConf().setMaster("local").setAppName("UDFDemo")
//    val sc =  new SparkContext(conf)
//    val sqlContext =  new SQLContext(sc)
//    val newSchema: StructType = oriDf.schema.add(
//      StructField(ouputCol, new VectorUDT, nullable = true))
//    val rddToUse = oriDf.map { row =>
//      val featuresSeq = inputCols.map {
//        col =>
//          row.getAs[Any](col).toString.toDouble
//      }
//      val featuresVec = Vectors.dense(featuresSeq.toArray)
//      val result = row.toSeq :+ featuresVec
//      Row.fromSeq(result)
//    }
//    sqlContext.createDataFrame(rddToUse, newSchema)
//  }
//
//  def vectorAssemble(oriDf: DataFrame, inputCols: Array[String], outputCol: String): DataFrame = {
//    val assembleFunc = udf((r: Row) => r.toSeq.map(_.toString))
//    val args = inputCols.map(oriDf(_))
//    oriDf.select(col("*"), assembleFunc(struct(args: _*)).as(outputCol,AttributeGroup()))
//  }
//
//  def main(args: Array[String]): Unit = {
//    val conf = new SparkConf().setMaster("local").setAppName("UDFDemo")
//    val sc =  new SparkContext(conf)
//    val sqlContext = new SQLContext(sc)
//    val df = sqlContext.read.csv("input/test.data")
//    vectorAssemble(df, Array("_c1", "_c2"), "_c3").select(col("_c3")(0)).show()
//    df.show()
//  }
//
//}
