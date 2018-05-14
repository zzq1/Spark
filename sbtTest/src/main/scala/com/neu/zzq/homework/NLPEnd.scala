package com.neu.zzq.homework

import java.io.File

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.feature._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.types.StructType
import scala.collection.mutable
import scala.io.Source

/**
  * Created by taos on 2017/12/24.
  */
object NLPEnd {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().master("local[4]").appName("NLPEnd").getOrCreate()
    val root = NLPEnd.getClass.getResource("/")
       val userSchema = new StructType().add("title", "string").add("dscr", "string").add("cate", "string").add("indu", "string")
       val data = spark.read.format("com.databricks.spark.csv")
      .schema(userSchema)
      .option("header", "true") //这里如果在csv第一行有属性的话，没有就是"false"
      .option("delimiter","\t")  //分隔符，默认为 ,
//    .option("inferSchema", true) //这是自动推断属性列的数据类型。
      .load(root + "wordslp.csv") //文件的路径
    data.show(false)
    //定义要去重的列
    val colArray = Array("dscr")
    val datas = data.dropDuplicates(colArray)
    datas.createOrReplaceTempView("qcwy")

    showDesc(spark)
    //三个分类， java  大数据  数据分析 看摘要
  }

  val splitContent = (str: String) => {
    //val strs = str.replaceAll("\\]", "").replaceAll("\\[", "")
    seg(str)
  }

  def showDesc(spark: SparkSession) = {
    //自定义函数
    spark.udf.register("splitContent", splitContent)
    val dscrDF = spark.sql("select  splitContent(dscr)  from qcwy ")
    val strStop = Source.fromFile(new File("E:\\stopword.txt"))("utf-8").getLines()
    val stops = strStop.toArray
    val remover = new StopWordsRemover()
      .setInputCol("UDF:splitContent(dscr)")
      .setOutputCol("filtered")
    remover.setStopWords(stops)
    val df = remover.transform(dscrDF)

    val cvModel: CountVectorizerModel = new CountVectorizer()
      .setInputCol("filtered")
      .setOutputCol("features")
      .setVocabSize(30000)
      .setMinDF(2)
      .fit(df)
    //cv result
    val featurizedData = cvModel.transform(df)
    //tf-idf begin
    val idf = new IDF().setInputCol("features").setOutputCol("rawFeatures")
    val idfModel = idf.fit(featurizedData)
    var rescaledData = idfModel.transform(featurizedData)
    //Kmeans begin
    val assembler = new VectorAssembler().setInputCols(Array("rawFeatures")).setOutputCol("featuresas")
    val kmeans = new KMeans().setK(30).setFeaturesCol("featuresas").setPredictionCol("prediction")
    //pipline
    val pipeline = new Pipeline().setStages(Array(assembler, kmeans))

    val kMeansPredictionModel = pipeline.fit(rescaledData)

    val predictionResult = kMeansPredictionModel.transform(rescaledData)
    predictionResult.show(200)
    predictionResult.show(false)
    val root = NLPEnd.getClass.getResource("/")
//ArrayType(StringType,true),true
    val dscra = udf((ft:mutable.WrappedArray[String])=>{
        ft.toString
    })
    var predictionResulta = predictionResult.withColumn("dscra", dscra(predictionResult.col("UDF:splitContent(dscr)")))
    val prediction = udf((ft:Int)=>{
      ft.toString
    })
    predictionResulta = predictionResulta.withColumn("predictiona", prediction(predictionResulta.col("prediction")))
    predictionResulta.show()
    // 保存数据框到文件
    predictionResulta.select("dscra","predictiona").orderBy(col("predictiona")).repartition(1).write.format("csv").save(root + "/nlp/wordResult.csv")
  }


  //udf

  //探索title
  def showTitle(spark: SparkSession) = {
    val titleDF = spark.sql("select title from qcwy where title like '%java%' or title like '%数据%' ")
    //val t = udf { desc: String => desc.replaceAll("\t","") }
    //dscrDF.select(col("dscr"), t(col("dscr")).as("desc"))
    titleDF.collect().foreach(println(_))
  }

  def seg(str: String): Array[String] = {
    val aa = str.split(",")
    aa(0) = aa(0).replace("[","")
    aa(aa.length-1) = aa(aa.length-1).replace("]","")
    aa
  }

}
//+------------+----------------------+--------------------+--------------------+--------------------+
//|          id|UDF:splitContent(dscr)|            filtered|            features|         rawFeatures|
//+------------+----------------------+--------------------+--------------------+--------------------+
//|QCWY96705288|                    []|                  []|       (27343,[],[])|       (27343,[],[])|
//|QCWY96705341|  [计算机 , 、 , 电子 , 等...|[计算机 , 、 , 电子 , 等...|(27343,[0,1,2,4,6...|(27343,[0,1,2,4,6...|
//|QCWY96708404|  [岗位 , 职责 , ： , 1....|[岗位 , 职责 , ： , 1....|(27343,[0,1,2,3,4...|(27343,[0,1,2,3,4...|
//|QCWY60418351|  [岗位 , 职责 , ： ,   ...|[岗位 , 职责 , ： ,   ...|(27343,[0,1,2,3,4...|(27343,[0,1,2,3,4...|
//|QCWY96706140|  [主要 , 工作 , 内容 , ：...|[主要 , 工作 , 内容 , ：...|(27343,[1,2,3,4,5...|(27343,[1,2,3,4,5...|
//|QCWY96705993|  [教育 , 经历 , ： , 计算...|[教育 , 经历 , ： , 计算...|(27343,[0,1,2,3,4...|(27343,[0,1,2,3,4...|
//|QCWY94938532|                 [职责 ]|               [职责 ]|  (27343,[44],[1.0])|(27343,[44],[0.49...|
//|QCWY93101578|  [负责 , 软件 , 项目 , 的...|[负责 , 软件 , 项目 , 的...|(27343,[0,1,2,3,4...|(27343,[0,1,2,3,4...|
//|QCWY96706264|  [招聘 , 岗位 , ： , 项目...|[招聘 , 岗位 , ： , 项目...|(27343,[0,1,2,3,4...|(27343,[0,1,2,3,4...|
//|QCWY96706618|  [职位 , 描述 , ： , 1....|[职位 , 描述 , ： , 1....|(27343,[0,1,2,4,5...|(27343,[0,1,2,4,5...|
//|QCWY87967407|  [参与 , 项目 , 需求 , 分...|[参与 , 项目 , 需求 , 分...|(27343,[0,1,2,3,4...|(27343,[0,1,2,3,4...|
//|QCWY96706934|  [岗位 , 职责 , ： , 负责...|[岗位 , 职责 , ： , 负责...|(27343,[0,1,2,3,4...|(27343,[0,1,2,3,4...|
//|QCWY96706832|  [熟练 , 掌握 , mysql ...|[熟练 , 掌握 , mysql ...|(27343,[0,1,2,3,5...|(27343,[0,1,2,3,5...|
//|QCWY96707059|  [负责 , 大 , 客户 , 和 ...|[负责 , 大 , 客户 , 和 ...|(27343,[0,1,2,4,5...|(27343,[0,1,2,4,5...|
//|QCWY83787232|                    []|                  []|       (27343,[],[])|       (27343,[],[])|
//|QCWY96707299|                    []|                  []|       (27343,[],[])|       (27343,[],[])|
//|QCWY96707278|  [cv ,   , in ,   ...|[cv ,   , in ,   ...|(27343,[3,53,61,1...|(27343,[3,53,61,1...|
//|QCWY96707827|  [岗位 , 职责 , ： , 赴 ...|[岗位 , 职责 , ： , 赴 ...|(27343,[0,1,2,3,4...|(27343,[0,1,2,3,4...|
//|QCWY68181642|                    []|                  []|       (27343,[],[])|       (27343,[],[])|
//|QCWY73705171|  [职位 , 要求 , ： , 1....|[职位 , 要求 , ： , 1....|(27343,[0,1,2,3,4...|(27343,[0,1,2,3,4...|
//+------------+----------------------+--------------------+--------------------+--------------------+