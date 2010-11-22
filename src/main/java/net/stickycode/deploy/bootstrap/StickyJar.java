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
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


public class StickyJar {

  private ZipFile zipFile;
  private String jarPath;

  public StickyJar(ZipFile zipFile, String jarPath) {
    super();
    this.zipFile = zipFile;
    this.jarPath = jarPath;
  }

  @Override
  public String toString() {
    return jarPath;
  }

  public StickyClass locate(String name) {
    try {
      return safeLocate(name);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private StickyClass safeLocate(String name) throws IOException {
    String searchFor = name.replace('.', '/') + ".class";
    ZipEntry entry = zipFile.getEntry(jarPath);
    JarInputStream i = new JarInputStream(zipFile.getInputStream(entry));
    JarEntry current = i.getNextJarEntry();
    while (current != null) {
      if (!current.isDirectory())
        if (current.getName().equals(searchFor))
          return load(i, current);

      current = i.getNextJarEntry();
    }
    return null;
  }

  private StickyClass load(JarInputStream i, JarEntry current) throws IOException {
    if (current.getSize() > Integer.MAX_VALUE)
      throw new RuntimeException("Why is your class so big?");

    int size = (int)current.getSize();
    byte[] b = new byte[size];
    int read = i.read(b, 0, size);
    System.out.println("Read " + read);
    for (int k = 0; k < read; k++) {
      System.out.print(Integer.toString( ( b[k] & 0xff ) + 0x100, 16).substring( 1 ));
    }
//    return b;
    return new StickyClass(b, read);
  }

}
