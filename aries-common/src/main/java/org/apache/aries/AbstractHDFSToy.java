/*
 * Copyright (c) 2019 R.C
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.aries;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hdfs.DistributedFileSystem;

/**
 * HBase toy's base implementation. HBase configuration is inititlized in this class.
 */
public abstract class AbstractHDFSToy extends AbstractToy {

  protected DistributedFileSystem file_system = new DistributedFileSystem();

  @Override
  protected void buildToy(ToyConfiguration configuration) throws Exception {
    Configuration hdfs_conf = ConfigurationFactory.createHDFSConfiguration(configuration);
    file_system.initialize(FileSystem.getDefaultUri(hdfs_conf), hdfs_conf);
    LOG.info("Cluster uri: " + FileSystem.getDefaultUri(hdfs_conf));
  }

  @Override
  protected void destroyToy() throws Exception {
    file_system.close();
  }

}
