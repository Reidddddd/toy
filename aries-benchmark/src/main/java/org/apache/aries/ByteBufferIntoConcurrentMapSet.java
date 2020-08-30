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

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ByteBufferIntoConcurrentMapSet extends AbstractBenchmarkToy {

  private Map<ByteBuffer, ByteBuffer> onheapmap;
  private Map<ByteBuffer, ByteBuffer> offheapmap;
  privae
  private ByteBuffer[] onheap = new ByteBuffer[1000];
  private ByteBuffer[] offheap = new ByteBuffer[1000];
  private Random random = new Random();

  @Setup
  public void initial() {
    for (int i = 0; i < 1000; i++) {
      onheap[i] = ByteBuffer.allocate(Constants.ONE_KB);
    }
    for (int i = 0; i < 1000; i++) {
      offheap[i] = ByteBuffer.allocateDirect(Constants.ONE_KB);
    }

    onheapmap = new ConcurrentSkipListMap<>();
    offheapmap = new ConcurrentSkipListMap<>();
  }

  @Benchmark
  public void onheapPut() {
    ByteBuffer bf = onheap[random.nextInt(1000)];
    onheapmap.put(bf, bf);
  }

  @Benchmark
  public void offheapPut() {
    ByteBuffer bf = offheap[random.nextInt(1000)];
    offheapmap.put(bf, bf);
  }

}
