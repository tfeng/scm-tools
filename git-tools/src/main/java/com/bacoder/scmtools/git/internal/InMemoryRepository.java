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

import java.io.IOException;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.util.FS;

class InMemoryRepository extends org.eclipse.jgit.internal.storage.dfs.InMemoryRepository {

  public InMemoryRepository(DfsRepositoryDescription repoDesc) {
    super(repoDesc);
  }

  @Override
  public FS getFS() {
    return FS.DETECTED;
  }

  @Override
  public boolean isBare() {
    return false;
  }

  @Override
  public DirCache lockDirCache() throws NoWorkTreeException, CorruptObjectException, IOException {
    DirCache dc = new InMemoryDirCache(getIndexFile(), getFS());
    dc.setRepository(this);
    return dc;
  }

  @Override
  public DirCache readDirCache() throws NoWorkTreeException, CorruptObjectException,
      IOException {
    DirCache dc = new InMemoryDirCache(getIndexFile(), getFS());
    dc.read();
    dc.setRepository(this);
    return dc;
  }
}
