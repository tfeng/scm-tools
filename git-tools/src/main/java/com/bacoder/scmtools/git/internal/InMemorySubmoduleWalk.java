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

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.submodule.SubmoduleWalk;

class InMemorySubmoduleWalk extends SubmoduleWalk {

  private Repository repository;
  private byte[] submodulesConfig;

  public InMemorySubmoduleWalk(Repository repository, byte[] submodulesConfig) throws IOException {
    super(repository);
    this.repository = repository;
    this.submodulesConfig = submodulesConfig;
  }

  @Override
  public SubmoduleWalk loadModulesConfig() throws IOException, ConfigInvalidException {
    File modulesFile = new File(repository.getWorkTree(), Constants.DOT_GIT_MODULES);
    FileBasedConfig config = new FileBasedConfig(modulesFile, repository.getFS()) {
      @Override
      protected byte[] readFully() throws IOException {
        return submodulesConfig;
      }
    };
    config.load();
    setModulesConfig(config);
    return this;
  }
}
