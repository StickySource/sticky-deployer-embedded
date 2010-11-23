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
    URL url = getResource('/' + j.getJarPath());
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
          return load(current.getName(), i, current);

      i.closeEntry();
      current = i.getNextJarEntry();
    }

    return null;
  }

  private Class<?> load(String name, JarInputStream i, JarEntry current) throws IOException {
    if (current.getSize() > Integer.MAX_VALUE)
      throw new RuntimeException("Why is your class so big?");

    int size = (int)current.getSize();
    byte[] b = new byte[size];
    int read = i.read(b, 0, size);
    System.out.println("Read " + read);
    for (int k = 0; k < read; k++) {
      System.out.print(Integer.toString( ( b[k] & 0xff ) + 0x100, 16).substring( 1 ));
    }

    return defineClass(name, b, 0, read);
  }
}
