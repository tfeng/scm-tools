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
package com.bacoder.scmtools.testutil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Scanner;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

public abstract class BaseTest {

  protected final Charset CHARSET = Charset.forName("UTF-8");

  protected ByteArrayOutputStream outputStream;

  @BeforeMethod
  protected void clearOutput() {
    outputStream = new ByteArrayOutputStream();
  }

  protected String getOutput() {
    return new String(outputStream.toByteArray(), CHARSET);
  }

  protected void verify(String output, InputStream expected) {
    Scanner scanner = new Scanner(expected, "UTF-8");
    String expectedString = "";
    try {
      expectedString = scanner.useDelimiter("\\Z").next().trim();
    } finally {
      scanner.close();
    }

    Assert.assertEquals(output.trim(), expectedString.trim());
  }

  protected void write(byte[] output) {
    try {
      outputStream.write(output);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void write(String output) {
    write(output.getBytes(CHARSET));
  }

  protected void writeln(byte[] output) {
    write(output);
    write("\n");
  }

  protected void writeln(String output) {
    write(output);
    write("\n");
  }
}
