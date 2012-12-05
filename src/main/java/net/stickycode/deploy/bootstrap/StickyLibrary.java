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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class StickyLibrary {
  
  private StickyLogger log = StickyLogger.getLogger(getClass());

  private final String jarPath;

  private final Collection<String> classes = new ArrayList<String>();

  private final Collection<String> resources = new ArrayList<String>();

  private String mainClass;

  public StickyLibrary(String jarPath) {
    this.jarPath = jarPath;
  }

  @Override
  public String toString() {
    return jarPath;
  }

  void add(String name) {
    if (name.endsWith(".class"))
      addClass(name.substring(0, name.length() - 6).replace('/', '.'));
    else
      addResource(name);
  }

  private boolean addResource(String name) {
    return resources.add(name);
  }

  private void addClass(String name) {
    classes.add(name);
  }

  public String getJarPath() {
    return jarPath;
  }

  public Collection<String> getClasses() {
    return classes;
  }

  public Collection<String> getResources() {
    return resources;
  }

  public String getMainClass() {
    return mainClass;
  }

  public InputStream getInputStream(ClassLoader loader, String path) {
    URL url = getJarStream(loader);
    try {
      JarInputStream jar = new JarInputStream(url.openStream());
      try {
        return loadStream(jar, path);
      }
      finally {
        jar.close();
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  URL getJarStream(ClassLoader loader) {
//    log.debug("opening jar %s", jarPath);
    URL url = loader.getResource(jarPath);
    if (url == null)
      throw new RuntimeException("Where did " + jarPath + " go?");

    return url;
  }

  private InputStream loadStream(JarInputStream jar, String path)
      throws IOException {
    JarEntry current = jar.getNextJarEntry();
    while (current != null) {
      if (!current.isDirectory())
        if (current.getName().equals(path))
          return new ByteArrayInputStream(load(path, jar, current));

      jar.closeEntry();
      current = jar.getNextJarEntry();
    }

    jar.closeEntry();
    return null;
  }

  private byte[] load(String name, JarInputStream in, JarEntry current)
      throws IOException {
    int size = deriveEntrySize(current);
    ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
    byte[] buf = new byte[2048];
    while (baos.size() < (Integer.MAX_VALUE - 2048)) {
      int len = in.read(buf);
      if (len < 0)
        return baos.toByteArray();

      baos.write(buf, 0, len);
    }

    throw new TheEntryBeingLoadedWasBiggerThan2GWhichSeemsWrongException(current.getName(), baos.size(), current.getCompressedSize(), current.getSize());
  }

  /**
   * Return the size of the class so that the byte array output stream is optimally sized and no copies are needed.
   * 
   * If the jar is dodgy and does not have proper sizes for the classes then return 2048 which is a reasonably guess for the average
   * class.
   */
  private int deriveEntrySize(JarEntry current) {
    if (current.getSize() >= Integer.MAX_VALUE)
      throw new TheUncompressedSizeListedInJarIsGreaterThan2GbWhichSeemsWrongException(current.getName(), current.getCompressedSize(), current.getSize());

    int size = (int) current.getSize();
    if (size < 0)
      size = 2048;
    return size;
  }

  public boolean hasMainClass() {
    return mainClass != null;
  }

  void addMain(String mainClass) {
    this.mainClass = mainClass;
  }
  
}
