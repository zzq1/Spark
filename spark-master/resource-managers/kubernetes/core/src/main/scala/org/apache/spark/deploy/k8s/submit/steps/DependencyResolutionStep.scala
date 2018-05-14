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
package org.apache.spark.deploy.k8s.submit.steps

import java.io.File

import io.fabric8.kubernetes.api.model.ContainerBuilder

import org.apache.spark.deploy.k8s.Constants._
import org.apache.spark.deploy.k8s.submit.{KubernetesDriverSpec, KubernetesFileUtils}

/**
 * Step that configures the classpath, spark.jars, and spark.files for the driver given that the
 * user may provide remote files or files with local:// schemes.
 */
private[spark] class DependencyResolutionStep(
    sparkJars: Seq[String],
    sparkFiles: Seq[String],
    jarsDownloadPath: String,
    localFilesDownloadPath: String) extends DriverConfigurationStep {

  override def configureDriver(driverSpec: KubernetesDriverSpec): KubernetesDriverSpec = {
    val resolvedSparkJars = KubernetesFileUtils.resolveFileUris(sparkJars, jarsDownloadPath)
    val resolvedSparkFiles = KubernetesFileUtils.resolveFileUris(
      sparkFiles, localFilesDownloadPath)
    val sparkConfResolvedSparkDependencies = driverSpec.driverSparkConf.clone()
    if (resolvedSparkJars.nonEmpty) {
      sparkConfResolvedSparkDependencies.set("spark.jars", resolvedSparkJars.mkString(","))
    }
    if (resolvedSparkFiles.nonEmpty) {
      sparkConfResolvedSparkDependencies.set("spark.files", resolvedSparkFiles.mkString(","))
    }
    val resolvedClasspath = KubernetesFileUtils.resolveFilePaths(sparkJars, jarsDownloadPath)
    val driverContainerWithResolvedClasspath = if (resolvedClasspath.nonEmpty) {
      new ContainerBuilder(driverSpec.driverContainer)
        .addNewEnv()
        .withName(ENV_MOUNTED_CLASSPATH)
        .withValue(resolvedClasspath.mkString(File.pathSeparator))
        .endEnv()
        .build()
    } else {
      driverSpec.driverContainer
    }
    driverSpec.copy(
      driverContainer = driverContainerWithResolvedClasspath,
      driverSparkConf = sparkConfResolvedSparkDependencies)
  }
}
