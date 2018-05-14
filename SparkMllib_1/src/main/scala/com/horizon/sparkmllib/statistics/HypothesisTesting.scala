package com.horizon.sparkmllib.statistics

import org.apache.spark.mllib.linalg.{Matrices, Matrix, Vector, Vectors}
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by taos on 2017/6/16.
  * 分为3类，每类50个数据，每个数据包含4个属性。
  * 可通过花萼长度，花萼宽度，花瓣长度，花瓣宽度4个属性预测鸢尾花卉属于
  * （Setosa，Versicolour，Virginica）三个种类中的哪一类
  */
object HypothesisTesting {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("HypothesisTesting")
    conf.set("spark.driver.extraJavaOptions",
      " -XX:PermSize=256M -XX:MaxPermSize=256M")
     val sc = new SparkContext(conf)
     val root = HypothesisTesting.getClass.getResource("/")
     val v1: Vector = sc.textFile(root + "iris.data")
       .map(_.split(",")).map(p => Vectors.dense(p(0).toDouble, p(1).toDouble, p(2).toDouble, p(3).toDouble)).first
    //Goodness fo fit（适合度检验）: 验证一组观察值的次数分配是否异于理论上的分配
    println(v1)
    val goodnessOfFitTestResult = Statistics.chiSqTest(v1)
    println(goodnessOfFitTestResult)

     val v2: Vector = sc.textFile(root + "iris.data")
       .map(_.split(",")).map(p => Vectors.dense(p(0).toDouble, p(1).toDouble, p(2).toDouble, p(3).toDouble)).take(2).last


    val mat: Matrix = Matrices.dense(2,2,Array(v1(0),v1(1),v2(0),v2(1)))

    val a =Statistics.chiSqTest(mat)
    //在本例中pValue =0.91，说明无法拒绝“样本序号与数据值无关”的假设
    //因为v1和v2是从同一个样本抽取的两条数据，样本的序号与数据的取值应该是没有关系的。
    println(a)
//     val vec = Vectors.dense(Array(0.25,0.25,0.25,0.25,0))
//     val goodnessOfFitTestResult = Statistics.chiSqTest(vec)
//     println("===>>>"+goodnessOfFitTestResult)
  }

}
