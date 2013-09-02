/**
 * Copyright 2013 Huining (Thomas) Feng (tfeng@berkeley.edu)
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
package com.bacoder.scmtools.git.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.util.FS;

class InMemoryDirCache extends DirCache {

  private static byte[] index;
  private static long lastModified;

  public InMemoryDirCache(File indexLocation, FS fs) {
    super(indexLocation, fs);
  }

  @Override
  public boolean commit() {
    return true;
  }

  @Override
  protected long getLastModified() {
    return lastModified;
  }

  @Override
  public void read() throws IOException {
    ByteArrayInputStream inputStream = new ByteArrayInputStream(index);
    try {
      readFrom(inputStream);
    } finally {
      inputStream.close();
    }
  }

  @Override
  public void write() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      writeTo(outputStream);
    } finally {
      outputStream.close();
    }
    index = outputStream.toByteArray();
    lastModified = System.currentTimeMillis();
  }
}
