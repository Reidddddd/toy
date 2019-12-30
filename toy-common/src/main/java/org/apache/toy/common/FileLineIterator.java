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

package org.apache.toy.common;

import org.apache.toy.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Optional;

/**
 * Tool class for read file line by line.
 */
public final class FileLineIterator implements AutoCloseable, Iterator<String> {

  private BufferedReader reader;
  @Nullable private Optional<String> line;

  public FileLineIterator(String file) {
    this(new File(file));
  }

  public FileLineIterator(File file) {
    try {
      reader = new BufferedReader(new FileReader(file));
      line = Optional.ofNullable(reader.readLine());
    } catch (Exception e) {
      line = Optional.empty();
    }
  }

  @Override
  public void close() {
    try {
      reader.close();
    } catch (Exception e) {
    }
  }

  @Override
  public boolean hasNext() {
    return line.isPresent();
  }

  @Override
  public String next() {
    String result = line.get();
    try {
      line = Optional.ofNullable(reader.readLine());
    } catch (Exception e) {
      line = Optional.empty();
    }
    return result;
  }

}
