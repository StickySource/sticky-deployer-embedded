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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class StickyClassLoader
    extends ClassLoader {

  private StickyLogger log = StickyLogger.getLogger(getClass());

  private StickyEmbeddedUrlStreamHandler urlFactory;

  private StickyClasspath classpath;

  public StickyClassLoader(ClassLoader loader, StickyClasspath classpath) {
    super(loader);
    this.classpath = classpath;
    this.urlFactory = new StickyEmbeddedUrlStreamHandler(classpath, loader);
  }

  @Override
  protected Class<?> findClass(String name)
      throws ClassNotFoundException {
    log.debug("looking up class %s", name);
    for (StickyLibrary j : classpath.getLibraries()) {
      if (j.getClasses().contains(name)) {
        log.debug("loading %s from %s", name, j);
        return loadClass(j, name);
      }
    }

    throw new ClassNotFoundException(name);
  }

  @Override
  protected URL findResource(String name) {
    Object[] parameters = { name };
    log.debug("Looking up resource %s", parameters);
    for (StickyLibrary j : classpath.getLibraries()) {
      if (j.getResources().contains(name)) {
        URL url = urlFactory.createResourceUrl(name, j);
        log.info("define url %s for %s in %s", url, name, j);
        return url;
      }
    }

    return null;
  }

  @Override
  protected Enumeration<URL> findResources(String name)
      throws IOException {
    LinkedList<URL> list = new LinkedList<URL>();
    Object[] parameters = { name };
    log.debug("Looking up resources %s", parameters);
    for (StickyLibrary j : classpath.getLibraries()) {
      if (j.getResources().contains(name)) {
        URL url = urlFactory.createResourceUrl(name, j);
        log.info("define url %s for %s in %s", url, name, j);
        list.add(url);
      }
    }

    return Collections.enumeration(list);
  }

  private Class<?> loadClass(StickyLibrary j, String name)
      throws ClassNotFoundException {
    URL url = j.getJarStream(getParent());
    try {
      InputStream openStream = url.openStream();
      try {
        JarInputStream jar = new JarInputStream(openStream);
        return loadClass(jar, name);
      }
      finally {
        openStream.close();
      }
    }
    catch (IOException e) {
      throw new ClassNotFoundException("Failed to load " + name, e);
    }
  }

  private Class<?> loadClass(JarInputStream i, String name)
      throws IOException {
    String searchFor = name.replace('.', '/') + ".class";
    JarEntry current = i.getNextJarEntry();
    while (current != null) {
      if (!current.isDirectory())
        if (current.getName().equals(searchFor))
          return load(name, i, current);

      i.closeEntry();
      current = i.getNextJarEntry();
    }

    return null;
  }

  private Class<?> load(String name, JarInputStream i, JarEntry current)
      throws IOException {
    byte[] b = copy(i, current);
    return defineClass(name, b, 0, b.length);
  }

  protected byte[] copy(InputStream in, JarEntry current)
      throws IOException {
    int size = deriveClassSize(current);
    ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
    byte[] buf = new byte[2048];
    while (baos.size() < (Integer.MAX_VALUE - 2048)) {
      int len = in.read(buf);
      if (len < 0)
        return baos.toByteArray();

      baos.write(buf, 0, len);
    }

    throw new TheEntryBeingLoadedWasBiggerThan2GWhichSeemsWrongException(current.getName(), baos.size(), current.getCompressedSize(),
        current.getSize());
  }

  /**
   * Return the size of the class so that the byte array output stream is optimally sized and no copies are needed.
   * 
   * If the jar is dodgy and does not have proper sizes for the classes then return 2048 which is a reasonable guess for the average
   * class.
   */
  private int deriveClassSize(JarEntry current) {
    if (current.getSize() >= Integer.MAX_VALUE)
      throw new TheUncompressedSizeListedInJarIsGreaterThan2GbWhichSeemsWrongException(current.getName(), current.getCompressedSize(),
          current.getSize());

    int size = (int) current.getSize();
    if (size < 0)
      size = 2048;
    return size;
  }
}
