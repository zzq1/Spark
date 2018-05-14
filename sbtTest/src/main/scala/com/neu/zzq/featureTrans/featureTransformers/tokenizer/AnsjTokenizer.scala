package com.neu.zzq.featureTrans.featureTransformers.tokenizer

import org.ansj.splitWord.analysis.ToAnalysis

/**
  * Created by taos on 2017/12/11.
  */
object AnsjTokenizer {
  def main(args: Array[String]): Unit = {
    val str = " 欢迎使用ansj_seg,(ansj中文分词)在这里如果你遇到什么问题都可以联系我.我一定尽我所能.帮助大家.ansj_seg更快,更准,更自由"
    println(ToAnalysis.parse(str))
  }
}
