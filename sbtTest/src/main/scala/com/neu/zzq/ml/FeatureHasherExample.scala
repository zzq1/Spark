/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.examples.ml

// $example on$
import org.apache.spark.ml.feature.HashingTF
import org.apache.spark.ml.feature.IDF
import org.apache.spark.ml.feature.Tokenizer
// $example off$
import org.apache.spark.sql.SparkSession

object FeatureHasherExample {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .appName("FeatureHasherExample")
      .getOrCreate()

    val sentenceData = spark.createDataFrame(Seq(
      (0.0, "Hi I heard about Spark"),
      (0.0, "I wish Java could use case classes"),
      (1.0, "Logistic regression models are neat")
    )).toDF("label", "sentence")

    val tokenizer = new Tokenizer().setInputCol("sentence").setOutputCol("words")
    val wordsData = tokenizer.transform(sentenceData)

    val hashingTF = new HashingTF()
      .setInputCol("words").setOutputCol("rawFeatures").setNumFeatures(20)

    val featurizedData = hashingTF.transform(wordsData)
    // alternatively, CountVectorizer can also be used to get term frequency vectors

    val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
    val idfModel = idf.fit(featurizedData)

    val rescaledData = idfModel.transform(featurizedData)
    rescaledData.select("label", "features").show()//    // Load documents (one per line).
//    val documents: RDD[Seq[String]] = sc.textFile("data/mllib/kmeans_data.txt")
//      .map(_.split(" ").toSeq)
//
//    val hashingTF = new HashingTF()
//    val tf: RDD[Vector] = hashingTF.transform(documents)
//
//    // While applying HashingTF only needs a single pass to the data, applying IDF needs two passes:
//    // First to compute the IDF vector and second to scale the term frequencies by IDF.
//    tf.cache()
//    val idf = new IDF().fit(tf)
//    val tfidf: RDD[Vector] = idf.transform(tf)
//
//    // spark.mllib IDF implementation provides an option for ignoring terms which occur in less than
//    // a minimum number of documents. In such cases, the IDF for these terms is set to 0.
//    // This feature can be used by passing the minDocFreq value to the IDF constructor.
//    val idfIgnore = new IDF(minDocFreq = 2).fit(tf)
//    val tfidfIgnore: RDD[Vector] = idfIgnore.transform(tf)
//
////    // $example on$
////    val dataset = spark.createDataFrame(Seq(
////      (2.2, true, "1", "foo"),
////      (3.3, false, "2", "bar"),
////      (4.4, false, "3", "baz"),
////      (5.5, false, "4", "foo")
////    )).toDF("real", "bool", "stringNum", "string")
////
////    val hasher = new HashingTF()
////      .setInputCols("real", "bool", "stringNum", "string")
////      .setOutputCol("features")
////
////    val featurized = hasher.transform(dataset)
////    featurized.show(false)
////    // $example off$

    spark.stop()
  }
}
