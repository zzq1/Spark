package com.neu.zzq.homework
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.feature.{HashingTF, IDF, StringIndexer, Tokenizer}
import org.apache.spark.sql.{SQLContext, SaveMode, SparkSession}

import scala.util.matching.Regex

/**
  * Created by zzq on 2017/12/26.
  */
object Recruitment {
  var h_money : List[Double] = List()
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().master("local").appName("Recruitment").getOrCreate()

    //将数据进行结构化
    val jsDF1 = spark.read.json("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第七次课\\data\\jobarea=010000&industrytype=01.json")
    val jsDF2 = spark.read.json("C:\\Users\\zzq\\Desktop\\spark\\Spark\\第七次课\\data\\jobarea=010000&industrytype=31.json")
    val jsDF = jsDF1.union(jsDF2)
    jsDF.createOrReplaceTempView("jsDB")
    val DBCate = spark.sql("SELECT t.cate,t.slry,t.expr FROM jsDB t")
    jsDF.coalesce(1).write.mode(SaveMode.Overwrite).csv("C:\\Users\\zzq\\Desktop\\DataMining\\Data\\rrr")
    spark.udf.register("get_hour",(a: String) => (a.split("元")(0).toDouble*8*22)/1000)
    spark.udf.register("get_day",(a: String) => (a.split("元")(0).toDouble*22)/1000)
    spark.udf.register("get_month",(s: String) => {
      val str  = s.split("/")
      val num = new Regex("\\d+\\.\\d+|\\d+")
      val Un = new Regex(".$")
      val b = (num findAllIn str(0)).toList
      val c = (Un findAllIn str(0)).mkString(",")
      if(c.contains("万")){
        (b(0).toDouble + b(1).toDouble)*5
      }
      else if (c.contains("千")){
        (b(0).toDouble + b(1).toDouble)/2
      }
      else if(str(0).contains("万")){
        b(0).toDouble * 10
      }
      else if(str(0).contains("千")){
        b(0).toDouble
      }
      else 0.0
    })
    spark.udf.register("get_year",(s: String) => {
      val str  = s.split("/")
      val num = new Regex("\\d+\\.\\d+|\\d+")
      val Un = new Regex(".$")
      val b = (num findAllIn str(0)).toList
      val c = (Un findAllIn str(0)).mkString(",")
      if(c.contains("万")){
        if(b.length == 1) {
         b(0).toDouble * 10 / 12
        }
        else {
          (b(0).toDouble + b(1).toDouble)*5 / 12
        }
      }
      else {
        b(0).toDouble*10 / 12
      }
    })

    val hour = DBCate.filter(_.get(1).toString.contains("小时"))
    val hour_t = hour.selectExpr("cate","get_hour(slry)","expr").toDF("cate","slry","expr")

    val day = DBCate.filter(_.get(1).toString.contains("天"))
    val day_t = day.selectExpr("cate","get_day(slry)","expr").toDF("cate","slry","expr")

    val month = DBCate.filter(_.get(1).toString.contains("月"))
    val month_t = month.selectExpr("cate","get_month(slry)","expr").toDF("cate","slry","expr")

    val year = DBCate.filter(_.get(1).toString.contains("年"))
    val year_t = year.selectExpr("cate","get_year(slry)","expr").toDF("cate","slry","expr")

    val target = hour_t.union(day_t).union(month_t).union(year_t)
    //target.coalesce(1).write.mode(SaveMode.Overwrite).csv("C:\\Users\\zzq\\Desktop\\DataMining\\Data\\rr")

    val gm = target.groupBy("cate","expr").agg(("slry","avg")).toDF("cate","expr","avg_slry(K)")
    val gm_sort = gm.orderBy(gm("avg_slry(K)").desc)
    //gm_sort.show()
    //gm.filter(_.get(0).toString.contains("大数据")).show()
   //val month_t = month.selectExpr("cate","get_day(slry)","expr").toDF("cate","slry","expr")
//    hour.createOrReplaceTempView("hour_table")
//    val hh =hour.foreach{ x=>{
//      val slry_to_string = x.get(1).toString.split("元")
//      h_money = h_money :+ (slry_to_string(0).toDouble * 8 * 22)
//    }}
//    import spark.implicits._
//    val h_df = h_money.toDF("v")
//    val h = hour.withColumn("ss",h_df("v"))
//    h.show()
    import spark.implicits._
    //h_money = h_money.toDF()
//    val seq = h_money.zipWithIndex.map{case (r: Double, id: Int) => (id,r)}.toSeq
//    val df1 = spark.createDataFrame(seq).toDF("id","money")
//    df1.show()
//   hour.show()
//    slry.foreach(x => {
//      //以“/”分割字符串
//      val str = x.toString().split("/")
//      //使用正则表达式分离出数字,计数单位
//      val num = new Regex("\\d+\\.\\d+|\\d+")
//      val Un = new Regex(".$")
//      //依据情况做不同处理
//      if (str(1).contains("小时")) {
//        println(str(1))
//        println(str(0))
//        val line = Some(str(1), (num findAllIn str(0)).mkString(","), (Un findAllIn str(0)).mkString(","))
//        line.foreach(println)
//      }
//    })
//    val indexer = new StringIndexer()
//      .setInputCol("cate")
//      .setOutputCol("label")
//
//    val indexed = indexer.fit(DBCate).transform(DBCate)
//    indexed.createOrReplaceTempView("indexedTable")
//    val tar = spark.sql("SELECT i.label,i.slry FROM indexedTable i")
//    // cateIndex|      slry|
//    //+---------+----------+
//   // |      8.0|0.8-1.2万/月|
//    //|      1.0|  1-1.8万/月|
//    //|     78.0|1.5-2.5万/月|
//    val tokenizer = new Tokenizer().setInputCol("slry").setOutputCol("words")
//    val wordsData = tokenizer.transform(tar)
////    +---------+----------+------------+
////    |cateIndex|      slry|       words|
////    +---------+----------+------------+
////    |      8.0|0.8-1.2万/月|[0.8-1.2万/月]|
////    |      1.0|  1-1.8万/月|  [1-1.8万/月]|
//    val hashingTF = new HashingTF().setInputCol("words").setOutputCol("rawFeatures").setNumFeatures(100)
//    val featurizedData = hashingTF.transform(wordsData)
//    val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
//    val idfModel = idf.fit(featurizedData)
//    val rescaledData = idfModel.transform(featurizedData)
//    rescaledData.createOrReplaceTempView("rescaledData")
//    val label_f = spark.sql("SELECT r.label,r.features FROM rescaledData r")
////    rescaledData.show()
////    +-----+----------+------------+--------------------+--------------------+
////    |label|      slry|       words|         rawFeatures|            features|
////    +-----+----------+------------+--------------------+--------------------+
////    |  8.0|0.8-1.2万/月|[0.8-1.2万/月]|(20000,[3243],[1.0])|(20000,[3243],[4....|
////    |  1.0|  1-1.8万/月|  [1-1.8万/月]|(20000,[9079],[1.0])|(20000,[9079],[5....|
////    | 78.0|1.5-2.5万/月|[1.5-2.5万/月]| (20000,[844],[1.0])|(20000,[844],[4.2...|
////    |  9.0|  0.8-1万/月|  [0.8-1万/月]|(20000,[14598],[1...|(20000,[14598],[2...|
////    |  1.0|(3,[0,1,2],[0.1,0...|
//
//    //rescaledData.foreach(x => println(x))
//    //[80.0,1-1.2万/月,WrappedArray(1-1.2万/月),(20000,[12779],[1.0]),(20000,[12779],[6.459538123510063])]
////    rescaledData.select("features", "label").take(3).foreach(println)
////    |label|            features|
////    +-----+--------------------+
////    |  0.0|           (3,[],[])|
////    |  1.0|(3,[0,1,2],[0.1,0...|
//////
//    val kmeans = new KMeans().setK(3).setSeed(1L)
//    val model = kmeans.fit(label_f)
//
//    // Make predictions
//    val predictions = model.transform(label_f)
//
//    // Evaluate clustering by computing Within Set Sum of Squared Errors.
//    val WSSSE = model.computeCost(label_f)
//    println(s"Within Set Sum of Squared Errors = $WSSSE")
//
//    // Shows the result.
//    println("Cluster Centers: ")
//    println(model.summary)
//    //label_f.foreach(println(_))
//    label_f.show(3)
  }
}
