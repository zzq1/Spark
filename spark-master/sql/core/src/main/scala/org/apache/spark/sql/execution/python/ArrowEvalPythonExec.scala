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

package org.apache.spark.sql.execution.python

import scala.collection.JavaConverters._

import org.apache.spark.TaskContext
import org.apache.spark.api.python.{ChainedPythonFunctions, PythonEvalType}
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions._
import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.types.StructType

/**
 * Grouped a iterator into batches.
 * This is similar to iter.grouped but returns Iterator[T] instead of Seq[T].
 * This is necessary because sometimes we cannot hold reference of input rows
 * because the some input rows are mutable and can be reused.
 */
private class BatchIterator[T](iter: Iterator[T], batchSize: Int)
  extends Iterator[Iterator[T]] {

  override def hasNext: Boolean = iter.hasNext

  override def next(): Iterator[T] = {
    new Iterator[T] {
      var count = 0

      override def hasNext: Boolean = iter.hasNext && count < batchSize

      override def next(): T = {
        if (!hasNext) {
          Iterator.empty.next()
        } else {
          count += 1
          iter.next()
        }
      }
    }
  }
}

/**
 * A physical plan that evaluates a [[PythonUDF]],
 */
case class ArrowEvalPythonExec(udfs: Seq[PythonUDF], output: Seq[Attribute], child: SparkPlan)
  extends EvalPythonExec(udfs, output, child) {

  private val batchSize = conf.arrowMaxRecordsPerBatch
  private val sessionLocalTimeZone = conf.sessionLocalTimeZone
  private val pandasRespectSessionTimeZone = conf.pandasRespectSessionTimeZone

  protected override def evaluate(
      funcs: Seq[ChainedPythonFunctions],
      bufferSize: Int,
      reuseWorker: Boolean,
      argOffsets: Array[Array[Int]],
      iter: Iterator[InternalRow],
      schema: StructType,
      context: TaskContext): Iterator[InternalRow] = {

    val schemaOut = StructType.fromAttributes(output.drop(child.output.length).zipWithIndex
      .map { case (attr, i) => attr.withName(s"_$i") })

    // DO NOT use iter.grouped(). See BatchIterator.
    val batchIter = if (batchSize > 0) new BatchIterator(iter, batchSize) else Iterator(iter)

    val columnarBatchIter = new ArrowPythonRunner(
        funcs, bufferSize, reuseWorker,
        PythonEvalType.SQL_PANDAS_SCALAR_UDF, argOffsets, schema,
        sessionLocalTimeZone, pandasRespectSessionTimeZone)
      .compute(batchIter, context.partitionId(), context)

    new Iterator[InternalRow] {

      private var currentIter = if (columnarBatchIter.hasNext) {
        val batch = columnarBatchIter.next()
        assert(schemaOut.equals(batch.schema),
          s"Invalid schema from pandas_udf: expected $schemaOut, got ${batch.schema}")
        batch.rowIterator.asScala
      } else {
        Iterator.empty
      }

      override def hasNext: Boolean = currentIter.hasNext || {
        if (columnarBatchIter.hasNext) {
          currentIter = columnarBatchIter.next().rowIterator.asScala
          hasNext
        } else {
          false
        }
      }

      override def next(): InternalRow = currentIter.next()
    }
  }
}
