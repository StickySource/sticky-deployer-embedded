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
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;


public class StickyEmbedder {

  private final List<StickyLibrary> libraries = new LinkedList<StickyLibrary>();
  private final File application;

  public StickyEmbedder() {
    application = deriveApplicationFile();
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

  protected File deriveApplicationFile() {
    return new File(StickyEmbedder.class.getProtectionDomain().getCodeSource().getLocation().getPath());
  }

  protected void loadEntries(ZipFile file) throws MalformedURLException {
    Enumeration<? extends ZipEntry> entries = file.entries();
    while (entries.hasMoreElements()) {
      ZipEntry zipEntry = (ZipEntry) entries.nextElement();
      if (zipEntry.getName().endsWith(".jar")) {
        System.out.println(zipEntry.getName());
        libraries.add(new StickyLibrary(file, zipEntry.getName()));
      }
    }
  }

  public static void main(String[] args) {
    System.out.println("Starting StickyEmbedder");
    StickyEmbedder embedder = new StickyEmbedder();
  }


  public List<StickyLibrary> getLibraries() {
    return libraries;
  }

  public void launch() {
    StickyClassLoader l = new StickyClassLoader(ClassLoader.getSystemClassLoader(), this);
    try {
      Class<?> e = l.loadClass("net.stickycode.exception.PermanentException");
      System.out.println(e);
    }
    catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }




}
