package com.horizon.sparkmllib.statistics

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by taos on 2017/6/16.
  * 分层取样算法是直接集成到键值对类型 RDD[(K, V)]
  * 的 sampleByKey 和 sampleByKeyExact 方法，无需通过额外的 spark.mllib 库来支持。
  * 样本数目越多，抽样的个数和结果越接近理论值
  *
  *
  * sampleByKey 和 sampleByKeyExact 的区别在于 sampleByKey 每次都通过给定的概率以一种类似于掷硬币的方式来决定这个观察值是否被放入
  * 样本，因此一遍就可以过滤完所有数据，最后得到一个近似大小的样本，但往往不够准确。而 sampleByKeyExtra 会对全量数据做采样计算。
  * 对于每个类别，其都会产生 （fk⋅nk）个样本，其中fk是键为k的样本类别采样的比例；nk是键k所拥有的样本数。
  * sampleByKeyExtra 采样的结果会更准确，有99.99%的置信度，但耗费的计算资源也更多。
  */
object StratifiedSampingDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("")
    val sc = new SparkContext(conf)
    val data = sc.makeRDD(Array(("female","Lily"), ("female","Lucy"), ("female","Emily"), ("female","Kate"), ("female","Alice"),
      ("male","Tom"), ("male","Roy"), ("male","David"), ("male","Frank"), ("male","Jack")))
    //通过fractions 参数来定义分类条件和采样几率：
    val fractions : Map[String, Double]= Map("female"->0.6,"male"->0.4)
    //withReplacement 	每次抽样是否有放回  fractions 	控制不同key的抽样率
    val approxSample = data.sampleByKey(withReplacement = false, fractions, 1)
    approxSample.foreach(println)
    val exactSample = data.sampleByKeyExact(withReplacement = false, fractions, 1)
    exactSample.foreach(println)
  }
}
