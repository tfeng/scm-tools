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
package com.bacoder.scmtools.git.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.jgit.lib.ObjectId;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.bacoder.scmtools.core.InitializationException;
import com.bacoder.scmtools.core.ProcessingException;
import com.bacoder.scmtools.git.GitEntry;
import com.bacoder.scmtools.git.GitEntryProcessor;
import com.bacoder.scmtools.git.GitRepository;
import com.bacoder.scmtools.testutil.BaseTest;

@Test
public class TestRepository1 extends BaseTest {

  private GitRepository gitRepository;

  @BeforeTest
  public void setup() throws InitializationException, URISyntaxException {
    URL url = getClass().getClassLoader().getResource("TestRepository1/TestRepository1.bundle");
    gitRepository = new GitRepository(url.toURI());
  }

  public void testBranch() throws ProcessingException {
    gitRepository.process("test-branch", new GitEntryProcessor() {
      @Override
      public void process(GitEntry entry) throws IOException {
        writeln(String.format("*** %s @ %s ***",
            new File(entry.getSubmodulePrefix(), entry.getPath()),
            entry.getObjectId().name()));
        writeln(entry.open().getBytes());
      }
    });
    verify(getOutput(), getClass().getClassLoader().getResourceAsStream(
        "TestRepository1/TestRepository1Branch.txt"));
  }

  public void testMaster() throws ProcessingException {
    gitRepository.process(ObjectId.fromString("f28590f812223d3e62586473cbc46e675e83fbc7"),
        new GitEntryProcessor() {
          @Override
          public void process(GitEntry entry) throws IOException {
            writeln(String.format("*** %s @ %s ***",
                new File(entry.getSubmodulePrefix(), entry.getPath()),
                entry.getObjectId().name()));
            writeln(entry.open().getBytes());
          }
        });
    verify(getOutput(), getClass().getClassLoader().getResourceAsStream(
        "TestRepository1/TestRepository1Master.txt"));
  }
}
