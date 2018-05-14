package com.horizon.ml

import com.horizon.ml.classification.TreeRegression
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext

/**
  * Created by taos on 2017/6/28.
  * // 建立多层感知器分类器MLPC模型
// 传统神经网络通常，层数<=5，隐藏层数<=3
// layers 指定神经网络的图层
// MaxIter 最大迭代次数
// stepSize 每次优化的迭代步长，仅适用于solver="gd"
// blockSize 用于在矩阵中堆叠输入数据的块大小以加速计算。 数据在分区内堆叠。
如果块大小大于分区中的剩余数据，则将其调整为该数据的大小。 建议大小介于10到1000之间。默认值：128
// initialWeights 模型的初始权重
// solver 算法优化。 支持的选项：“gd”（minibatch梯度下降）或“l-bfgs”。 默认值：“l-bfgs”
  */
object MLPCDemo {
  def main(args: Array[String]): Unit = {
    // Load the data stored in LIBSVM format as a DataFrame.
    val conf = new SparkConf().setMaster("local").setAppName("PipelineDemo").set("spark.driver.extraJavaOptions",
      "-Xms1512m -Xmx2048m -XX:PermSize=512M -XX:MaxPermSize=512M")

    val sc =  new SparkContext(conf)
    val sqlContext =  new SQLContext(sc)
    val root = TreeRegression.getClass.getResource("/")
    val data = sqlContext.read.format("libsvm")
      .load(root + "sample_multiclass_classification_data.txt")
    // Split the data into train and test
    val splits = data.randomSplit(Array(0.6, 0.4), seed = 1234L)
    val train = splits(0)
    val test = splits(1)
    // specify layers for the neural network:
    // input layer of size 4 (features), two intermediate of size 5 and 4
    // and output of size 3 (classes)
    val layers = Array[Int](4, 5, 4, 3)
    // create the trainer and set its parameters
    val trainer = new MultilayerPerceptronClassifier()
      .setLayers(layers)
      .setBlockSize(128)
      .setSeed(1234L)
      .setMaxIter(100)
    // train the model
    val model = trainer.fit(train)
    // compute precision on the test set
    val result = model.transform(test)
    val predictionAndLabels = result.select("prediction", "label")
    val evaluator = new MulticlassClassificationEvaluator()
      .setMetricName("precision")
    println("Precision:" + evaluator.evaluate(predictionAndLabels))
    Thread.sleep(100000000)
  }
}
