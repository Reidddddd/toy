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

import org.apache.aries.common.Parameter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Options;
import org.apache.hadoop.hdfs.client.impl.DfsClientConf;
import org.apache.hadoop.hdfs.protocol.datatransfer.PacketHeader;
import org.apache.hadoop.hdfs.protocol.datatransfer.PacketReceiver;

import java.util.List;

public class RelatedSizeInHDFSWrite extends AbstractHDFSToy {

  @Override protected String getParameterPrefix() {
    return "rs";
  }

  @Override protected void requisite(List<Parameter> requisites) {
  }

  @Override protected void exampleConfiguration() {
  }

  DfsClientConf client_conf;
  Configuration conf;
  @Override protected void buildToy(ToyConfiguration configuration) throws Exception {
    super.buildToy(configuration);
    conf = file_system.getConf();
    client_conf = file_system.getClient().getConf();
  }

  @Override protected int haveFun() throws Exception {
    Options.ChecksumOpt checksumOpt = client_conf.getDefaultChecksumOpt();
    LOG.info("Internal buffer before checksum: " + (checksumOpt.getBytesPerChecksum() * 9));
    LOG.info("Internal buffer for storing checksum: " + (checksumOpt.getChecksumType().size * 9));
    int write_packet_size = client_conf.getWritePacketSize();
    write_packet_size = Math.min(write_packet_size, PacketReceiver.MAX_PACKET_SIZE);
    LOG.info("Write packet size: " + write_packet_size);
    int bodySize = write_packet_size - PacketHeader.PKT_MAX_HEADER_LEN;
    int chunkSize = checksumOpt.getBytesPerChecksum() + checksumOpt.getChecksumType().size;
    int chunksPerPacket = Math.min(bodySize / chunkSize, 1);
    LOG.info("Chunks per packet: " + chunksPerPacket);
    LOG.info("Packet size: " + chunkSize * chunksPerPacket);
    return 0;
  }

}
