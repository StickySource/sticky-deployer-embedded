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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class StickyClassLoader
    extends ClassLoader {

  private List<StickyJar> jars = new ArrayList<StickyJar>();

  public StickyClassLoader() {
    super();
  }

  public StickyClassLoader(ClassLoader parent) {
    super(parent);
  }

  public void add(StickyJar jar) {
    System.out.println("adding " + jar);
    jars.add(jar);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    for (StickyJar j : jars) {
      System.out.println("trying " + j + " for " + name);
      StickyClass klass = j.locate(name);
      if (klass != null)
        return defineClass(name, klass.getBytes(), 0, klass.getLength());
    }
    throw new ClassNotFoundException(name);
  }

}
