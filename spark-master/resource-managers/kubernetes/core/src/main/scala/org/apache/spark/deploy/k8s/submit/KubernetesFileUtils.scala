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
package org.apache.spark.deploy.k8s.submit

import java.io.File

import org.apache.spark.util.Utils

private[spark] object KubernetesFileUtils {

  /**
   * For the given collection of file URIs, resolves them as follows:
   * - File URIs with scheme file:// are resolved to the given download path.
   * - File URIs with scheme local:// resolve to just the path of the URI.
   * - Otherwise, the URIs are returned as-is.
   */
  def resolveFileUris(
      fileUris: Iterable[String],
      fileDownloadPath: String): Iterable[String] = {
    fileUris.map { uri =>
      resolveFileUri(uri, fileDownloadPath, false)
    }
  }

  /**
   * If any file uri has any scheme other than local:// it is mapped as if the file
   * was downloaded to the file download path. Otherwise, it is mapped to the path
   * part of the URI.
   */
  def resolveFilePaths(fileUris: Iterable[String], fileDownloadPath: String): Iterable[String] = {
    fileUris.map { uri =>
      resolveFileUri(uri, fileDownloadPath, true)
    }
  }

  private def resolveFileUri(
      uri: String,
      fileDownloadPath: String,
      assumesDownloaded: Boolean): String = {
    val fileUri = Utils.resolveURI(uri)
    val fileScheme = Option(fileUri.getScheme).getOrElse("file")
    fileScheme match {
      case "local" =>
        fileUri.getPath
      case _ =>
        if (assumesDownloaded || fileScheme == "file") {
          val fileName = new File(fileUri.getPath).getName
          s"$fileDownloadPath/$fileName"
        } else {
          uri
        }
    }
  }
}
