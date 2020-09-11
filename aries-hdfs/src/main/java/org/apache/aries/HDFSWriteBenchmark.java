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
  private final Parameter<String[]> iofile_buffer_size =
      StringArrayParameter.newBuilder("bm.hdfs.write.io_file_buffer_size").setDescription("The size of buffer for use in sequence files").opt();


  @Override
  protected void requisite(List<Parameter> requisites) {
    super.requisite(requisites);
    requisites.add(write_size);
    requisites.add(write_buffer_size);
    requisites.add(write_packet);
    requisites.add(iofile_buffer_size);
  }

  @Override
  protected void decorateOptions(ChainedOptionsBuilder options_builder) {
    super.decorateOptions(options_builder);
    options_builder.param("tcp_no_delay", "true", "false");
    setParameters(options_builder, write_packet, "write_packet_size", s -> String.valueOf(Integer.valueOf(s) * Constants.ONE_KB));
    setParameters(options_builder, iofile_buffer_size, "io_file_buffer_size", s -> String.valueOf(Integer.valueOf(s) * Constants.ONE_KB));
  }
  private void setParameters(ChainedOptionsBuilder options_builder, Parameter<String[]> parameter, String para_key, StrConvert cvt) {
    if (!parameter.empty()) {
      for (String value : parameter.value()) {
        value = cvt.convert(value);
      }
      options_builder.param(para_key, parameter.value());
    }
  }
  private interface StrConvert {
    String convert(String in);
  }

  private long size_in_bytes;
  private byte[] bytes;

  final Random random = new Random();
  private byte[] flipOneByte(byte[] bytes) {
    int len = bytes.length;
    bytes[random.nextInt(len)] = ToyUtils.generateRandomString(1).getBytes()[0];
    return bytes;
  }


  // The size of buffer for use in sequence files. The size of this buffer should probably be a multiple of hardware page size (4096 on Intel x86),
  // and it determines how much data is buffered during read and write operations.
  @Param({"4096"})
  String io_file_buffer_size;

  @Override
  public void setup() throws Exception {
    super.setup();
    size_in_bytes = write_size.value() * Constants.ONE_MB;
    bytes = ToyUtils.generateRandomString(write_buffer_size.value()).getBytes();
  }

  @Override
  void injectConfiguration() {
    conf.set("io.file.buffer.size", io_file_buffer_size);
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
