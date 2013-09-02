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
import java.net.URI;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import com.bacoder.scmtools.core.InitializationException;
import com.bacoder.scmtools.core.InternalRuntimeException;
import com.bacoder.scmtools.core.ProcessingException;
import com.bacoder.scmtools.core.SCMRepository;
import com.bacoder.scmtools.git.GitEntry;
import com.bacoder.scmtools.git.GitEntryProcessor;

public class InMemoryGitRepository extends SCMRepository<GitEntry, GitEntryProcessor> {

  private InMemoryCloneCommand command;

  public InMemoryGitRepository(URI uri) throws InitializationException {
    super(uri);

    try {
      command = new InMemoryCloneCommand();
      command.setURI(uri.toString());
      command.init();
    } catch (Throwable t) {
      throw new InitializationException("Unable to initialize Git repository " + uri, t);
    }
  }

  @Override
  public void process(GitEntryProcessor processor) throws ProcessingException {
    process(Constants.HEAD, processor);
  }

  public void process(String branch, GitEntryProcessor processor) throws ProcessingException {
    InMemoryCloneCommand command = this.command.createNewCommand();
    command.setBranch(branch);
    command.setProcessor(processor);
    try {
      command.checkout();
    } catch (IOException | GitAPIException e) {
      throw new ProcessingException(e);
    } catch (InternalRuntimeException e) {
      throw new ProcessingException(e.getCause());
    }
  }

  public void process(ObjectId commit, GitEntryProcessor processor) throws ProcessingException {
    InMemoryCloneCommand command = this.command.createNewCommand();

    command.setProcessor(processor);
    try {
      RevCommit revCommit = new RevWalk(command.getRepository()).parseCommit(commit);
      command.doCheckout(command.getRepository(), revCommit);
    } catch (IOException | GitAPIException e) {
      throw new ProcessingException(e);
    } catch (InternalRuntimeException e) {
      throw new ProcessingException(e.getCause());
    }
  }
}
