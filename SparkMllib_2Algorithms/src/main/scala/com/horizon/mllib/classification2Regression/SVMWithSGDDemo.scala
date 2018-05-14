package com.horizon.mllib.classification2Regression
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.classification.{SVMModel, SVMWithSGD}
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.util.MLUtils
/**
  * Created by taos on 2017/6/19.
  * input：样本数据，分类标签lable只能是1.0和0.0两种,feature为double类型
    numIterations: 迭代次数，默认为100
    stepSize: 迭代步长，默认为1.0
    miniBatchFraction: 每次迭代参与计算的样本比例，默认为1.0
    initialWeights:初始权重，默认为0向量
  */
object SVMWithSGDDemo {
  def main(args: Array[String]): Unit = {
    val conf =  new SparkConf().setAppName("SVMWithSGDDemo").setMaster("local")
    val sc = new SparkContext(conf)
    //val root = SVMWithSGDDemo.getClass.getResource("/")
    val root ="C:/Users/zzq/IdeaProjects/SparkMllib_2Algorithms/resource/"
    // Load training data in LIBSVM format.
    //输入类型要求是RDD[LabelPoint]
    val data = MLUtils.loadLibSVMFile(sc, root + "sample_libsvm_data.txt")

    // Split data into training (60%) and test (40%).
    val splits = data.randomSplit(Array(0.6, 0.4), seed = 11L)
    val training = splits(0).cache()
    val test = splits(1)

    // Run training algorithm to build the model
    val numIterations = 100
    val model = SVMWithSGD.train(training, numIterations)

    // Clear the default threshold.
    model.clearThreshold()

    // Compute raw scores on the test set.
    val scoreAndLabels = test.map { point =>
      val score = model.predict(point.features)
      (score, point.label)
    }

    // Get evaluation metrics.
    val metrics = new BinaryClassificationMetrics(scoreAndLabels)
    val auROC = metrics.areaUnderROC()

    println("Area under ROC = " + auROC)

    // Save and load model
    model.save(sc, "myModelPath")
    val sameModel = SVMModel.load(sc, "myModelPath")
  }
}
