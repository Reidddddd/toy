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

public class HDFSWriteBenchmark extends HDFSBenchmark {

  private final Parameter<Integer> write_size =
      IntParameter.newBuilder("bm.hdfs.write.file_size_in_mb").setDefaultValue(128).setDescription("What is the size of a file to be writtien on HDFS").opt();

  private final Parameter<String[]> write_packet =
      StringArrayParameter.newBuilder("bm.hdfs.write.write_packet_size").setDescription("Packet size for clients to write (in KB)").opt();
  private final Parameter<String[]> checksum_bytes =
      StringArrayParameter.newBuilder("bm.hdfs.write.bytes_per_checksum").setDescription("The number of bytes per checksum").opt();
  private final Parameter<String[]> max_packets =
      StringArrayParameter.newBuilder("bm.hdfs.write.max_packets_in_flight").setDescription("The maximum number of DFSPackets allowed in flight").opt();
  private final Parameter<String[]> tcp_no_delay =
      StringArrayParameter.newBuilder("bm.hdfs.write.tcp_no_delay").setDescription("set TCP_NODELAY to sockets for transferring data from DFS client").opt();
  private final Parameter<String[]> socket_send_buffer =
      StringArrayParameter.newBuilder("bm.hdfs.write.socket_buffer_kb").setDescription("Socket send buffer size for a write pipeline in DFSClient side in KB").opt();
  private final Parameter<String[]> checksum =
      StringArrayParameter.newBuilder("bm.hdfs.write.checksum_type").setDescription("Checksum type").opt();


  @Override
  protected void requisite(List<Parameter> requisites) {
    super.requisite(requisites);
    requisites.add(write_size);
    requisites.add(write_packet);
    requisites.add(checksum_bytes);
    requisites.add(max_packets);
    requisites.add(tcp_no_delay);
    requisites.add(socket_send_buffer);
    requisites.add(checksum);
  }

  @Override
  protected void decorateOptions(ChainedOptionsBuilder options_builder) {
    super.decorateOptions(options_builder);
    setParameters(options_builder, write_packet, "write_packet_size", s -> String.valueOf(Integer.valueOf(s) * Constants.ONE_KB));
    setParameters(options_builder, checksum_bytes, "bytes_per_checksum", s -> s);
    setParameters(options_builder, max_packets, "max_packets_in_flight", s -> s);
    setParameters(options_builder, tcp_no_delay, "tcp_nodelay", s -> s);
    setParameters(options_builder, socket_send_buffer, "socket_buffer", s -> String.valueOf(Integer.valueOf(s) * Constants.ONE_KB));
    setParameters(options_builder, checksum, "checksum_type", s -> s);
  }
  private void setParameters(ChainedOptionsBuilder options_builder, Parameter<String[]> parameter, String para_key, StrConvert cvt) {
    if (!parameter.empty()) {
      String[] values = parameter.value();
      for (int i = 0; i < values.length; i++) {
        values[i] = cvt.convert(values[i]);
      }
      options_builder.param(para_key, values);
    }
  }
  private interface StrConvert {
    String convert(String in);
  }

  private long size_in_bytes;
  private byte[] io_buffer;


  @Param({"512"})
  String bytes_per_checksum;
  @Param({"64"})
  String write_packet_size;
  @Param({"80"})
  String max_packets_in_flight;
  @Param({"true"})
  String tcp_nodelay;
  @Param({"0"})
  String socket_buffer;
  @Param({"CRC32C"})
  String checksum_type;

  @Override
  public void setup() throws Exception {
    super.setup();
    size_in_bytes = write_size.value() * Constants.ONE_MB;
    io_buffer = ToyUtils.generateRandomString(conf.getInt("io.file.buffer.size", 4096)).getBytes();
  }

  @Override
  void injectConfiguration() {
    conf.set("dfs.checksum.type", checksum_type);
    conf.set("dfs.bytes-per-checksum", bytes_per_checksum);
    conf.set("dfs.client-write-packet-size", write_packet_size);
    conf.set("dfs.client.write.max-packets-in-flight", max_packets_in_flight);
    conf.set("dfs.data.transfer.client.tcpnodelay", tcp_nodelay);
    conf.set("dfs.client.socket.send.buffer.size", socket_buffer);
  }

  @Benchmark
  public void testHDFSWrite() throws Exception {
    FSDataOutputStream os = null;
    try {
      os = file_system.create(new Path(work_dir, ToyUtils.generateRandomString(10)));
      long written_bytes = 0;
      while (written_bytes < size_in_bytes) {
        os.write(io_buffer);
        written_bytes += io_buffer.length;
      }
    } finally {
      if (os != null) {
        os.close();
      }
    }
  }

}

