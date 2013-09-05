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
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.URIish;

import com.bacoder.scmtools.git.GitConfig;
import com.bacoder.scmtools.git.GitEntryProcessor;

class CloneAndProcessCommand extends CloneCommand {

  private boolean cloneSubmodules;

  private GitConfig config;

  private FetchResult fetchResult;

  private String pathPrefix;

  private GitEntryProcessor processor;

  private Repository repository;

  private byte[] submodulesConfig;

  public CloneAndProcessCommand(GitConfig config) {
    this(null, config);
  }

  public CloneAndProcessCommand(String pathPrefix, GitConfig config) {
    this.pathPrefix = pathPrefix;
    setConfig(config);
  }

  public void checkout() throws IOException, GitAPIException {
    if (config.getCommitRevision() == null) {
      checkout(repository, fetchResult);
    } else {
      RevCommit revCommit = new RevWalk(repository).parseCommit(config.getCommitRevision());
      doCheckout(repository, revCommit);
    }
  }

  public CloneAndProcessCommand createNewCommand() {
    CloneAndProcessCommand newCommand = new CloneAndProcessCommand(pathPrefix, config);
    newCommand.repository = repository;
    newCommand.fetchResult = fetchResult;
    return newCommand;
  }

  public void fetch() throws GitAPIException, IOException, URISyntaxException {
    URIish u = new URIish(uri);
    fetchResult = fetch(repository, u);
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

  public boolean isInMemory() {
    return directory == null;
  }

  @Override
  public CloneAndProcessCommand setCloneSubmodules(boolean cloneSubmodules) {
    super.setCloneSubmodules(cloneSubmodules);
    this.cloneSubmodules = cloneSubmodules;
    return this;
  }

  public CloneAndProcessCommand setProcessor(GitEntryProcessor processor) {
    this.processor = processor;
    return this;
  }

  @Override
  protected void cloneSubmodules(Repository repository) throws IOException, GitAPIException {
    SubmoduleWalk generator;
    if (isInMemory()) {
      generator = new InMemorySubmoduleWalk(repository, submodulesConfig);
      try {
        DirCache index = repository.readDirCache();
        generator.setTree(new DirCacheIterator(index));
      } catch (IOException e) {
        generator.release();
        throw e;
      }
    } else {
      generator = SubmoduleWalk.forIndex(repository);
    }

    try {
      while (generator.next()) {
        if (generator.getConfigUrl() != null) {
          continue;
        }

        String path = generator.getPath();
        String url = generator.getRemoteUrl();

        CloneAndProcessCommand command =
            new CloneAndProcessCommand(path, config).setProcessor(processor);
        command.setURI(url).setCloneSubmodules(true);
        command.call();
      }
    } catch (ConfigInvalidException e) {
      throw new IOException("Config invalid", e);
    }
  }

  @Override
  protected void doCheckout(final Repository repository, RevCommit commit)
      throws MissingObjectException, IncorrectObjectTypeException,
      IOException, GitAPIException {
    DirCache dirCache;
    if (isInMemory()) {
      dirCache = new InMemoryDirCache(repository.getIndexFile(), repository.getFS());
      dirCache.setRepository(repository);
    } else {
      dirCache = repository.lockDirCache();
    }
    CloneAndProcessDirCacheCheckout checkout =
        new CloneAndProcessDirCacheCheckout(repository, dirCache, commit.getTree(), pathPrefix);
    checkout.setProcessor(processor);
    checkout.checkout();

    submodulesConfig = checkout.getSubmodulesConfig();
    if (cloneSubmodules) {
      cloneSubmodules(repository);
    }
  }

  @Override
  protected Repository init(URIish u) throws GitAPIException {
    if (isInMemory()) {
      return new InMemoryRepository(new DfsRepositoryDescription());
    } else {
      return super.init(u);
    }
  }

  protected void setConfig(GitConfig config) {
    this.config = config;
    setBranch(config.getBranch());
    setCloneSubmodules(config.getIncludeSubmodule());
    setDirectory(config.getDirectory());
    setProgressMonitor(config.getProgressMonitor());
  }
}
