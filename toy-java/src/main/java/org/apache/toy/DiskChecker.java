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

package org.apache.toy;

import org.apache.toy.common.FileLineIterator;
import org.apache.toy.common.Parameter;

import java.util.BitSet;
import java.util.List;

public class DiskChecker extends AbstractJavaToy {

  private final Parameter<String> disk_check_file =
      Parameter.<String>newBuilder()
               .setKey("disk_check_file").setRequired(true)
               .setType(String.class).setDescription("file contains disk information to be checked")
               .opt();
  private final Parameter<Integer> num_of_disk =
      Parameter.<Integer>newBuilder()
               .setKey("number_of_disk").setRequired(true)
               .setType(Integer.class).setDescription("number of disks")
               .opt();
  private final Parameter<String> disk_seperator =
      Parameter.<String>newBuilder()
               .setKey("disk_seperator").setRequired(true)
               .setType(String.class).setDescription("prefix of disk")
               .opt();
  private final Parameter<String> section_delimiter =
      Parameter.<String>newBuilder()
               .setKey("section_delimiter").setRequired(true)
               .setType(String.class).setDescription("section separator")
               .opt();

  @Override
  protected void requisite(@SuppressWarnings("rawtypes") List<Parameter> requisites) {
    requisites.add(disk_check_file);
    requisites.add(num_of_disk);
    requisites.add(disk_seperator);
    requisites.add(section_delimiter);
  }

  @Override
  protected void buildToy(Configuration configuration) throws Exception {
    // no-op
  }

  @Override
  public int haveFun() {
    try (FileLineIterator fli = new FileLineIterator(disk_check_file.value())) {
      boolean first_section = true;
      MachineDisk md = new MachineDisk(num_of_disk.value());

      while (fli.hasNext()) {
        String line = fli.next();

        // Deal with secion header
        if (line.contains(section_delimiter.value())) {
          if (first_section) {
            first_section = false;
            md.hostname = line.split(section_delimiter.value())[1].trim();
            continue;
          } else if (md.abnormal()) {
            md.listAbnormalOnly();
          }
          md.reset();
          md.hostname = line.split(section_delimiter.value())[1].trim();
          continue;
        }

        // Deal with processes lines
        md.diskExist(getDiskIndex(line));
      }
    }
    return RETURN_CODE.SUCCESS.code();
  }

  @Override
  protected void destroyToy() throws Exception {
    // no-op
  }

  private int getDiskIndex(String line) {
    int idx = -1;
    if (DISK.SSD.typeMatch(line)) {
      return num_of_disk.value() - 1;
    } else if (DISK.SATA.typeMatch(line)) {
      idx = line.lastIndexOf(disk_seperator.value());
    }
    return idx < 0 ? -1 : Integer.parseInt(line.substring(idx + 1));
  }

  private enum DISK {

    SATA("disk"),
    SSD("ssd");

    String type;

    DISK(String type) { this.type = type; }

    boolean typeMatch(String line) {
      return line.contains(type);
    }

  }

  private class MachineDisk {

    String hostname = "";
    BitSet normal_disk;
    int size;

    MachineDisk(int num_of_disk) {
      size = num_of_disk;
      normal_disk = new BitSet(num_of_disk);
    }

    void diskExist(int num_disk) {
      if (num_disk < 0) return;
      normal_disk.set(num_disk);
    }

    void reset() {
      normal_disk.clear(0, size);
    }

    boolean abnormal() {
      int expect_idx = 0;
      for (int i = normal_disk.nextSetBit(0);
           i != -1 && expect_idx == i;
           i = normal_disk.nextSetBit(i + 1), expect_idx++) {
      }
      return expect_idx != size;
    }

    void listAbnormalOnly() {
      StringBuilder builder = new StringBuilder(hostname + " lacks ");
      for (int i = normal_disk.nextClearBit(0); i < size; i = normal_disk.nextClearBit(i + 1)) {
        builder.append(i).append("\\s");
      }
      System.out.println(builder.toString());
    }
  }

}
