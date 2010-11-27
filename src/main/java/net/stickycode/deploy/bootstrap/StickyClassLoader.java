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
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class StickyClassLoader
    extends ClassLoader {

  private final StickyEmbedder embedder;

  public StickyClassLoader(ClassLoader parent, StickyEmbedder stickyEmbedder) {
    super(parent);
    this.embedder = stickyEmbedder;
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    for (StickyLibrary j : embedder.getLibraries()) {
      if (j.getClasses().contains(name)) {
        embedder.debug("loading %s from %s", j, name);
        return loadClass(j, name);
      }
    }
    throw new ClassNotFoundException(name);
  }

  private Class<?> loadClass(StickyLibrary j, String name) {
    URL url = embedder.getClass().getResource("/" + j.getJarPath());
    try {
      JarInputStream jar = new JarInputStream(url.openStream());
      return loadClass(jar, name);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Class<?> loadClass(JarInputStream i, String name) throws IOException {
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

  private Class<?> load(String name, JarInputStream i, JarEntry current) throws IOException {
    byte[] b = copy(i, current);
    return defineClass(name, b, 0, b.length);
  }

  protected byte[] copy(InputStream in, JarEntry current) throws IOException {
    int size = deriveClassSize(current);
    ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
    byte[] buf = new byte[2048];
    while (baos.size() < (Integer.MAX_VALUE - 2048)) {
      int len = in.read(buf);
      if (len < 0)
        return baos.toByteArray();

      baos.write(buf, 0, len);
    }

    throw new TheClassBeingLoadedWasBiggerThan2GWhichSeemsWrong(current.getName(), baos.size(), current.getCompressedSize(), current.getSize());
  }

  /**
   * Return the size of the class so that the byte array output stream is optimally sized and no copies are needed.
   *
   * If the jar is dodgy and does not have proper sizes for the classes then return 2048 which is a reasonably guess for the average
   * class.
   */
  private int deriveClassSize(JarEntry current) {
    if (current.getSize() >= Integer.MAX_VALUE)
      throw new TheUncompressedSizeListedInJarIsGreaterThan2GbWhichSeemsWrong(current.getName(), current.getCompressedSize(), current.getSize());

    int size = (int) current.getSize();
    if (size < 0)
      size = 2048;
    return size;
  }
}
