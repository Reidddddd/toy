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

import org.apache.aries.common.Constants;
import org.apache.aries.common.IntParameter;
import org.apache.aries.common.Parameter;
import org.apache.aries.common.StringArrayParameter;
import org.apache.aries.common.ToyUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;

import java.util.List;
import java.util.Random;

public class HDFSWriteBenchmark extends HDFSBenchmark {

  private final Parameter<Integer> write_size =
      IntParameter.newBuilder("bm.hdfs.write.file_size_in_mb").setDefaultValue(128).setDescription("What is the size of a file to be writtien on HDFS").opt();
  private final Parameter<Integer> write_buffer_size =
      IntParameter.newBuilder("bm.hdfs.write.buffer_size_in_bytes").setDefaultValue(Constants.ONE_KB).setDescription("What is the size of each write's buffer").opt();

  private final Parameter<String[]> write_packet =
      StringArrayParameter.newBuilder("bm.hdfs.write.write_packet_size").setDescription("Packet size for clients to write (in KB)").opt();


  @Override
  protected void requisite(List<Parameter> requisites) {
    super.requisite(requisites);
    requisites.add(write_size);
    requisites.add(write_buffer_size);
    requisites.add(write_packet);
  }

  @Override
  protected void decorateOptions(ChainedOptionsBuilder options_builder) {
    super.decorateOptions(options_builder);
    options_builder.param("tcp_no_delay", "true", "false");
    if (!write_packet.empty()) {
      for (String packet : write_packet.value()) {
        packet = String.valueOf(Integer.valueOf(packet) * Constants.ONE_KB);
      }
      options_builder.param("write_packet_size", write_packet.value());
    }
  }


  private long size_in_bytes;
  private byte[] bytes;

  final Random random = new Random();
  private byte[] flipOneByte(byte[] bytes) {
    int len = bytes.length;
    bytes[random.nextInt(len)] = Byte.parseByte(ToyUtils.generateRandomString(1));
    return bytes;
  }



  @Param({"true", "false"})
  String tcp_no_delay;  // Use TCP_NODELAY flag to bypass Nagle's algorithm transmission delays
  @Param({"64", "128", "256", "512", "1024"})
  String write_packet_size; // Packet size for clients to write

  @Override
  public void setup() throws Exception {
    super.setup();
    size_in_bytes = write_size.value() * Constants.ONE_MB;
    bytes = ToyUtils.generateRandomString(write_buffer_size.value()).getBytes();
  }

  @Override
  void injectConfiguration() {
    conf.set("ipc.client.tcpnodelay", tcp_no_delay);
    conf.set("dfs.client-write-packet-size", write_packet_size);
  }

  @Benchmark
  public void testHDFSWrite() throws Exception {
    FSDataOutputStream os = null;
    try {
      Path out = new Path(work_dir, ToyUtils.generateRandomString(10));
      os = file_system.create(out);
      long written_bytes = 0;
      while (written_bytes < size_in_bytes) {
        os.write(flipOneByte(bytes));
        written_bytes += bytes.length;
      }
    } finally {
      if (os != null) {
        os.close();
      }
    }
  }

}
