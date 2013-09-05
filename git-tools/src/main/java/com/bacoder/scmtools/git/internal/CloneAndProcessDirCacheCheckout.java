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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheCheckout;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.errors.IndexWriteException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;

import com.bacoder.scmtools.core.InternalRuntimeException;
import com.bacoder.scmtools.git.GitEntry;
import com.bacoder.scmtools.git.GitEntryProcessor;
import com.google.common.collect.Lists;

class CloneAndProcessDirCacheCheckout extends DirCacheCheckout {

  private DirCache dirCache;
  private String pathPrefix;
  private GitEntryProcessor processor;
  private Repository repository;
  private byte[] submodulesConfig;

  public CloneAndProcessDirCacheCheckout(Repository repository, DirCache dirCache,
      ObjectId mergeCommitTree, String pathPrefix) throws IOException {
    super(repository, dirCache, mergeCommitTree);
    this.repository = repository;
    this.dirCache = dirCache;
    this.pathPrefix = pathPrefix;
  }

  public byte[] getSubmodulesConfig() {
    return submodulesConfig;
  }

  public void setProcessor(GitEntryProcessor processor) {
    this.processor = processor;
  }

  @Override
  protected boolean checkoutEntries() throws IOException {
    Map<String, ObjectId> updated = getUpdated();
    List<String> paths = Lists.newArrayList(updated.keySet());
    Collections.sort(paths);

    ObjectReader objectReader = repository.getObjectDatabase().newReader();
    try {
      for (String path : paths) {
        DirCacheEntry entry = dirCache.getEntry(path);
        if (FileMode.GITLINK.equals(entry.getRawMode()))
          continue;

        boolean isSubmoduleConfig = path.equals(Constants.DOT_GIT_MODULES);
        ObjectId objectId = updated.get(path);
        if (isSubmoduleConfig) {
          ObjectLoader loader = objectReader.open(objectId);
          byte[] data = loader.getBytes();
          submodulesConfig = data;
        }
        if (processor != null) {
          GitEntry gitEntry = new GitEntry(repository, objectId, pathPrefix, path);
          try {
            processor.process(gitEntry);
          } catch (Exception e) {
            throw new InternalRuntimeException(e);
          }
        }
      }
      // commit the index builder - a new index is persisted
      if (!getBuilder().commit())
        throw new IndexWriteException();
    } finally {
      objectReader.release();
    }
    return true;
  }
}
