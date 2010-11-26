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
import java.io.OutputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;


public class StickyClassLoader
    extends ClassLoader {

  private final StickyEmbedder embedder;

//  public StickyClassLoader() {
//    super();
//  }
//
//  public StickyClassLoader(ClassLoader parent) {
//    super(parent);
//  }

  public StickyClassLoader(ClassLoader parent, StickyEmbedder stickyEmbedder) {
    super(parent);
    this.embedder = stickyEmbedder;
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    for (StickyLibrary j : embedder.getLibraries()) {
      System.out.println("trying " + j + " for " + name);
      if (j.getClasses().contains(name)) {
        System.out.println("found");
        return loadClass(j, name);
      }
//      StickyClass klass = j.locate(name);
//      if (klass != null)
//        return defineClass(name, klass.getBytes(), 0, klass.getLength());
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
    if (current.getSize() > Integer.MAX_VALUE)
      throw new RuntimeException("Why is your class so big?");
    int size = (int)current.getSize();
    if (size < 0)
      size = 2048;

    byte[] b = copy(i, size);
//    int size = (int)current.getSize();
//    if (size < 0)
//      size = 10000;
//    byte[] b = new byte[size];
//    int read = i.read(b, 0, size);
    System.out.println("Read " + b.length);
//    for (int k = 0; k < read; k++) {
//      System.out.print(Integer.toString( ( b[k] & 0xff ) + 0x100, 16).substring( 1 ));
//    }

    return defineClass(name, b, 0, b.length);
  }


  protected byte[] copy(InputStream in, int size) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
    byte[] buf = new byte[2048];
    while (true) {
        int len = in.read(buf);
        if (len < 0)
          return baos.toByteArray();

        baos.write(buf, 0, len);
    }
}
}
