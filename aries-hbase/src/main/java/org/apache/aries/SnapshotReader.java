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

import org.apache.aries.common.BoolParameter;
import org.apache.aries.common.Parameter;
import org.apache.aries.common.StringParameter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellScanner;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SnapshotReader extends AbstractHBaseToy {

  Parameter<String> snapshot = StringParameter.newBuilder("sr.snapshot_name").setRequired().setDescription("Snapshot name to be read").opt();
  Parameter<String> restoreDir = StringParameter.newBuilder("sr.restore_dir").setRequired().setDescription("Restore dir for snapshot reading").opt();
  Parameter<Boolean> dependency = BoolParameter.newBuilder("sr.dependency_or_not", false).setDescription("If we add dependency").opt();

  @Override
  protected String getParameterPrefix() {
    return "sr";
  }

  @Override
  protected void requisite(List<Parameter> requisites) {
    requisites.add(snapshot);
    requisites.add(restoreDir);
    requisites.add(dependency);
  }

  @Override
  protected void exampleConfiguration() {
    example(snapshot.key(), "table-snapshot");
    example(restoreDir.key(), "hdfs://hdfs/restore_dir");
    example(dependency.key(), "true");
  }

  @Override
  protected int haveFun() throws Exception {
    Configuration conf = connection.getConfiguration();
    Iterator<Map.Entry<String, String>> it = conf.iterator();
    while (it.hasNext()) {
      Map.Entry<String, String> e = it.next();
      LOG.info(e.getKey() + "=" + e.getValue());
    }
    Job job = Job.getInstance(conf, "HBaseSnapshotReader");
    job.setJarByClass(SnapshotReader.class);
    Scan scan = new Scan();
    scan.setCacheBlocks(false);
    TableMapReduceUtil.initTableSnapshotMapperJob(
        snapshot.value(),
        scan, Mapper.class,
        ImmutableBytesWritable.class,
        NullWritable.class,
        job,
        dependency.value(),
        new Path(restoreDir.value()));
    job.waitForCompletion(true);
    return 0;
  }

  private static class Mapper extends TableMapper<ImmutableBytesWritable, NullWritable> {

    @Override
    protected void map(ImmutableBytesWritable row, Result value, Context context) throws IOException, InterruptedException {
      LOG.info("Processing row: " + Bytes.toString(row.get(), row.getOffset(), row.getLength()));
      CellScanner scanner = value.cellScanner();
      while (scanner.advance()) {
        Cell cell = scanner.current();
        LOG.info(CellUtil.toString(cell, false));
      }
      context.write(row, NullWritable.get());
    }

  }

}
