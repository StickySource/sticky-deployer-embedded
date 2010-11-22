/**
 * Copyright (c) 2010 RedEngine Ltd, http://www.redengine.co.nz. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package net.stickycode.deploy.bootstrap;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;


public class StickyBootstrap {

  private final File application;
  private StickyClassLoader classLoader;

  public StickyBootstrap(File application) {
    super();
    this.application = application;
    this.classLoader = new StickyClassLoader(getClass().getClassLoader());
  }

  public void boot() {
    try {
      ZipFile file = new ZipFile(application);
      loadEntries(file);
    }
    catch (ZipException e) {
      throw new RuntimeException(e);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  private void loadEntries(ZipFile file) throws MalformedURLException {
    Enumeration<? extends ZipEntry> entries = file.entries();
    while (entries.hasMoreElements()) {
      ZipEntry zipEntry = (ZipEntry) entries.nextElement();
      if (zipEntry.getName().endsWith(".jar"))
        classLoader.add(new StickyJar(file, zipEntry.getName()));
    }
  }

  public Class<?> load(String name) throws ClassNotFoundException {
    return classLoader.loadClass(name);
  }

  public static void main(String[] args) {
    for (String string : args) {
      System.out.println(string);
    }
  }

}
