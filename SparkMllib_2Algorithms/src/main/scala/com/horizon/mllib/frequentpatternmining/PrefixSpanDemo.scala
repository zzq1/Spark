package com.horizon.mllib.frequentpatternmining
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.fpm.PrefixSpan
/**
  * Created by taos on 2017/12/15.
  */
object PrefixSpanDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[4]").setAppName("PrefixSpanDemo")
    val sc = new SparkContext(conf)
    val sequences = sc.parallelize(Seq(
      Array(Array(1, 2), Array(3)),
      Array(Array(1), Array(3, 2), Array(1, 2)),
      Array(Array(1, 2), Array(5)),
      Array(Array(6))
    ), 2).cache()
    val prefixSpan = new PrefixSpan()
      .setMinSupport(0.5)
      .setMaxPatternLength(5)
    val model = prefixSpan.run(sequences)
    model.freqSequences.collect().foreach { freqSequence =>
      println(
        freqSequence.sequence.map(_.mkString("[", ", ", "]")).mkString("[", ", ", "]") +
          ", " + freqSequence.freq)
    }
  }
}
