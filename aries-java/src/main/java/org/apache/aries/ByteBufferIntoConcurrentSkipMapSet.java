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
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ByteBufferIntoConcurrentSkipMapSet extends AbstractBenchmarkToy {

  private ByteBuffer[] onheap = new ByteBuffer[1000];

  @State(Scope.Benchmark)
  public static class Index {
    int x = 0;

    public int getIndex() {
      return x == 1000 ? 0 : x++;
    }
  }

  @Override
  protected void decorateOptions(ChainedOptionsBuilder options_builder) {
  }

  private Map<ByteBuffer, Object> kmap;
  private Set<ByteBuffer> kset;
  private Map<ByteBuffer, ByteBuffer> kvmap;

  @Setup
  public void initial() {
    for (int i = 0; i < 1000; i++) {
      onheap[i] = ByteBuffer.allocate(Constants.ONE_KB);
    }

    kmap = new ConcurrentSkipListMap<>();
    kvmap = new ConcurrentSkipListMap<>();
    kset = new ConcurrentSkipListSet<>();
  }

  @Benchmark
  public void testKMap(Index index) {
    ByteBuffer bf = onheap[index.getIndex()];
    kmap.put(bf, true);
  }

  @Benchmark
  public void testKVMap(Index index) {
    ByteBuffer bf = onheap[index.getIndex()];
    kvmap.put(bf, bf);
  }

  @Benchmark
  public void testKSet(Index index) {
    ByteBuffer bf = onheap[index.getIndex()];
    if (kset.contains(bf)) kset.remove(bf);
    kset.add(bf);
  }

}
