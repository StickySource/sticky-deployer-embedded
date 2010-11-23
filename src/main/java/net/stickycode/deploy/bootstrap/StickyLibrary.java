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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class StickyLibrary {

  private final String jarPath;
  private final Set<String> classes = new HashSet<String>();
  private final Set<String> resources = new HashSet<String>();

  public StickyLibrary(ZipFile zipFile, String jarPath) {
    super();
    this.jarPath = jarPath;
    index(zipFile);
  }

  @Override
  public String toString() {
    return jarPath;
  }


  private void index(ZipFile zipFile) {
    ZipEntry entry = zipFile.getEntry(jarPath);
    try {
      processEntry(zipFile, entry);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void processEntry(ZipFile zipFile, ZipEntry entry) throws IOException {
    JarInputStream i = new JarInputStream(zipFile.getInputStream(entry));
    JarEntry current = i.getNextJarEntry();
    while (current != null) {
      if (!current.isDirectory())
        processName(current.getName());

      i.closeEntry();
      current = i.getNextJarEntry();
    }
  }

  private void processName(String name) {
    if (name.endsWith(".class"))
      classes.add(name.substring(0, name.length() - 6).replace('/', '.'));
    else
      resources.add(name);
  }

  public String getJarPath() {
    return jarPath;
  }

  public Set<String> getClasses() {
    return classes;
  }


  public Set<String> getResources() {
    return resources;
  }

}
