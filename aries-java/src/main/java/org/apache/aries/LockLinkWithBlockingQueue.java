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
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class LockLinkWithBlockingQueue extends AbstractBenchmarkToy {

  private ByteBuffer[] onheap = new ByteBuffer[10000];

  private ReentrantLock lock = new ReentrantLock();
  private Queue<ByteBuffer> q1 = new LinkedList();
  private BlockingQueue<ByteBuffer> q2 = new LinkedBlockingQueue<>();

  @Override protected void decorateOptions(ChainedOptionsBuilder options_builder) {
  }

  @Setup
  public void initial() {
    for (int i = 0; i < onheap.length; i++) {
      onheap[i] = ByteBuffer.allocate(Constants.ONE_KB);
    }
  }

  @Benchmark
  public void testBOffer() {
    for (int i = 0; i < onheap.length; i++) {
      q2.offer(onheap[i]);
    }
  }

  @Benchmark
  public void testBPull() {
    for (int i = 0; i < onheap.length; i++) {
      q2.poll();
    }
  }

  @Benchmark
  public void testLOffer() {
    for (int i = 0; i < onheap.length; i++) {
      lock.lock();
      try {
        q1.offer(onheap[i]);
      } finally {
        lock.unlock();
      }
    }
  }

  @Benchmark
  public void testLPull() {
    for (int i = 0; i < onheap.length; i++) {
      lock.lock();
      try {
        q1.poll();
      } finally {
        lock.unlock();
      }
    }
  }

}
