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
import java.net.URISyntaxException;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.URIish;

import com.bacoder.scmtools.git.GitEntryProcessor;

class InMemoryCloneCommand extends CloneCommand {

  private boolean cloneSubmodules;

  private FetchResult fetchResult;

  private String pathPrefix;

  private GitEntryProcessor processor;

  private Repository repository;

  private byte[] submodulesConfig;

  public InMemoryCloneCommand() {
    this(null);
  }

  public InMemoryCloneCommand(String pathPrefix) {
    this.pathPrefix = pathPrefix;
  }

  public void checkout() throws IOException, GitAPIException {
    checkout(repository, fetchResult);
  }

  public InMemoryCloneCommand createNewCommand() {
    InMemoryCloneCommand newCommand = new InMemoryCloneCommand(pathPrefix);
    newCommand.repository = repository;
    newCommand.fetchResult = fetchResult;
    return newCommand;
  }

  public String getBranch() {
    return branch;
  }

  @Override
  public Repository getRepository() {
    return repository;
  }

  public void init() throws GitAPIException, IOException, URISyntaxException {
    URIish u = new URIish(uri);
    repository = init(u);
    fetchResult = fetch(repository, u);
  }

  public void fetch() throws GitAPIException, IOException, URISyntaxException {
    URIish u = new URIish(uri);
    fetchResult = fetch(repository, u);
  }

  @Override
  public InMemoryCloneCommand setCloneSubmodules(boolean cloneSubmodules) {
    super.setCloneSubmodules(cloneSubmodules);
    this.cloneSubmodules = cloneSubmodules;
    return this;
  }

  public InMemoryCloneCommand setProcessor(GitEntryProcessor processor) {
    this.processor = processor;
    return this;
  }

  @Override
  protected void cloneSubmodules(Repository repository) throws IOException, GitAPIException {
    SubmoduleWalk generator = new InMemorySubmoduleWalk(repository, submodulesConfig);
    try {
      DirCache index = repository.readDirCache();
      generator.setTree(new DirCacheIterator(index));
    } catch (IOException e) {
      generator.release();
      throw e;
    }

    try {
      while (generator.next()) {
        if (generator.getConfigUrl() != null) {
          continue;
        }

        String path = generator.getPath();
        String url = generator.getRemoteUrl();

        InMemoryCloneCommand command = new InMemoryCloneCommand(path).setProcessor(processor);
        command.setURI(url).setCloneSubmodules(true);
        command.call();
      }
    } catch (ConfigInvalidException e) {
      throw new IOException("Config invalid", e);
    }
  }

  protected void doCheckout(final Repository repository, ObjectId objectId)
      throws IOException, GitAPIException {
    InMemoryDirCache dirCache = new InMemoryDirCache(repository.getIndexFile(), repository.getFS());
    dirCache.setRepository(repository);
    InMemoryDirCacheCheckout checkout =
        new InMemoryDirCacheCheckout(repository, dirCache, objectId, pathPrefix);
    checkout.setProcessor(processor);
    checkout.checkout();

    submodulesConfig = checkout.getSubmodulesConfig();
    if (cloneSubmodules) {
      cloneSubmodules(repository);
    }
  }

  @Override
  protected void doCheckout(final Repository repository, RevCommit commit)
      throws MissingObjectException, IncorrectObjectTypeException,
      IOException, GitAPIException {
    InMemoryDirCache dirCache = new InMemoryDirCache(repository.getIndexFile(), repository.getFS());
    dirCache.setRepository(repository);
    InMemoryDirCacheCheckout checkout =
        new InMemoryDirCacheCheckout(repository, dirCache, commit.getTree(), pathPrefix);
    checkout.setProcessor(processor);
    checkout.checkout();

    submodulesConfig = checkout.getSubmodulesConfig();
    if (cloneSubmodules) {
      cloneSubmodules(repository);
    }
  }

  @Override
  protected Repository init(URIish u) throws GitAPIException {
    return new InMemoryRepository(new DfsRepositoryDescription());
  }
}
