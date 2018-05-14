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

package org.apache.spark.sql.execution.datasources.v2

import scala.collection.JavaConverters._

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.encoders.{ExpressionEncoder, RowEncoder}
import org.apache.spark.sql.catalyst.expressions._
import org.apache.spark.sql.execution.LeafExecNode
import org.apache.spark.sql.execution.metric.SQLMetrics
import org.apache.spark.sql.sources.v2.reader._
import org.apache.spark.sql.types.StructType

/**
 * Physical plan node for scanning data from a data source.
 */
case class DataSourceV2ScanExec(
    fullOutput: Seq[AttributeReference],
    @transient reader: DataSourceV2Reader) extends LeafExecNode with DataSourceReaderHolder {

  override def canEqual(other: Any): Boolean = other.isInstanceOf[DataSourceV2ScanExec]

  override def references: AttributeSet = AttributeSet.empty

  override lazy val metrics = Map(
    "numOutputRows" -> SQLMetrics.createMetric(sparkContext, "number of output rows"))

  override protected def doExecute(): RDD[InternalRow] = {
    val readTasks: java.util.List[ReadTask[UnsafeRow]] = reader match {
      case r: SupportsScanUnsafeRow => r.createUnsafeRowReadTasks()
      case _ =>
        reader.createReadTasks().asScala.map {
          new RowToUnsafeRowReadTask(_, reader.readSchema()): ReadTask[UnsafeRow]
        }.asJava
    }

    val inputRDD = new DataSourceRDD(sparkContext, readTasks)
      .asInstanceOf[RDD[InternalRow]]
    val numOutputRows = longMetric("numOutputRows")
    inputRDD.map { r =>
      numOutputRows += 1
      r
    }
  }
}

class RowToUnsafeRowReadTask(rowReadTask: ReadTask[Row], schema: StructType)
  extends ReadTask[UnsafeRow] {

  override def preferredLocations: Array[String] = rowReadTask.preferredLocations

  override def createDataReader: DataReader[UnsafeRow] = {
    new RowToUnsafeDataReader(
      rowReadTask.createDataReader, RowEncoder.apply(schema).resolveAndBind())
  }
}

class RowToUnsafeDataReader(rowReader: DataReader[Row], encoder: ExpressionEncoder[Row])
  extends DataReader[UnsafeRow] {

  override def next: Boolean = rowReader.next

  override def get: UnsafeRow = encoder.toRow(rowReader.get).asInstanceOf[UnsafeRow]

  override def close(): Unit = rowReader.close()
}
