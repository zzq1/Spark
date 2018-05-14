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

package org.apache.spark.sql.sources.v2.reader;

import org.apache.spark.sql.sources.v2.reader.Offset;
import org.apache.spark.sql.execution.streaming.BaseStreamingSource;

import java.util.Optional;

/**
 * A mix-in interface for {@link DataSourceV2Reader}. Data source readers can implement this
 * interface to indicate they allow micro-batch streaming reads.
 */
public interface MicroBatchReader extends DataSourceV2Reader, BaseStreamingSource {
    /**
     * Set the desired offset range for read tasks created from this reader. Read tasks will
     * generate only data within (`start`, `end`]; that is, from the first record after `start` to
     * the record with offset `end`.
     *
     * @param start The initial offset to scan from. If not specified, scan from an
     *              implementation-specified start point, such as the earliest available record.
     * @param end The last offset to include in the scan. If not specified, scan up to an
     *            implementation-defined endpoint, such as the last available offset
     *            or the start offset plus a target batch size.
     */
    void setOffsetRange(Optional<Offset> start, Optional<Offset> end);

    /**
     * Returns the specified (if explicitly set through setOffsetRange) or inferred start offset
     * for this reader.
     *
     * @throws IllegalStateException if setOffsetRange has not been called
     */
    Offset getStartOffset();

    /**
     * Return the specified (if explicitly set through setOffsetRange) or inferred end offset
     * for this reader.
     *
     * @throws IllegalStateException if setOffsetRange has not been called
     */
    Offset getEndOffset();

    /**
     * Deserialize a JSON string into an Offset of the implementation-defined offset type.
     * @throws IllegalArgumentException if the JSON does not encode a valid offset for this reader
     */
    Offset deserializeOffset(String json);
}
