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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;


public class StickyEmbedder {

  private final List<StickyLibrary> libraries = new LinkedList<StickyLibrary>();
  private final File application;
  private final String[] args;

  public StickyEmbedder(String... args) {
    this.args = args;
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
        loadAll(file, zipEntry.getName());
      }
    }
  }

  public static void main(String[] args) {
    System.out.println("Starting StickyEmbedder");
    StickyEmbedder embedder = new StickyEmbedder(args);
    embedder.launch();
  }

  public List<StickyLibrary> getLibraries() {
    return libraries;
  }

  void loadAll(ZipFile zipFile, String jarPath) {
    try {
      JarInputStream file = processEntry(zipFile, jarPath);
      JarInputStream internal = processInternal(jarPath);
      processJarInputStream(file, internal);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  JarInputStream processEntry(ZipFile zipFile, String jarPath) throws IOException {
    ZipEntry entry = zipFile.getEntry(jarPath);
    return new JarInputStream(zipFile.getInputStream(entry));
  }
  JarInputStream processInternal(String jarPath) throws IOException {
    URL url = getClass().getResource("/" + jarPath);
    return new JarInputStream(url.openStream());
  }

  private void processJarInputStream(JarInputStream file, JarInputStream internal) throws IOException {
    JarEntry cf = file.getNextJarEntry();
    JarEntry ci = internal.getNextJarEntry();
    while (cf != null) {
      System.out.println("f" + cf.getName() + " cs " + cf.getCompressedSize() + " s " + cf.getSize());
      System.out.println("i" + ci.getName() + " cs " + ci.getCompressedSize() + " s " + ci.getSize());
      if (!cf.isDirectory())
        if (cf.getName().endsWith(".class")) {
          int fsize = load(cf.getName().substring(0, cf.getName().length() - 6), file, cf);
          int isize = load(ci.getName().substring(0, ci.getName().length() - 6), internal, ci);
          if (fsize != isize)
            throw new RuntimeException("ug");
        }

      file.closeEntry();
      internal.closeEntry();
      cf = file.getNextJarEntry();
      ci = internal.getNextJarEntry();
    }
  }

  private int load(String name, JarInputStream i, JarEntry current) throws IOException {
    if (current.getSize() > Integer.MAX_VALUE)
      throw new RuntimeException("Why is your class so big?");

    int size = (int)current.getSize();
    if (size < 0)
      size = 2048;

    ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
    int read = copy(i, baos);
//    byte[] b = new byte[size];
//    int read = i.read(b, 0, size);
    byte[] b = baos.toByteArray();
    System.out.println(name + ":"+ read);
//    for (int k = 0; k < read; k++) {
//      System.out.print(Integer.toString( ( b[k] & 0xff ) + 0x100, 16).substring( 1 ));
//    }
//    System.out.println();
    return read;
  }

  protected int copy(InputStream in, OutputStream out) throws IOException {
    byte[] buf = new byte[2048];
    int read = 0;
    while (true) {
        int len = in.read(buf);
        if (len < 0)
          return read;
        read += len;
        out.write(buf, 0, len);
    }
}


  public void launch() {
    StickyClassLoader l = new StickyClassLoader(ClassLoader.getSystemClassLoader(), this);
    Thread.currentThread().setContextClassLoader(l);
    try {
      Class<?> e = l.loadClass("net.stickycode.deploy.sample.helloworld.HelloWorld");
      Runnable r = constructRunnable(e);
      Thread t = new Thread(r);
      t.setContextClassLoader(l);
      t.setDaemon(false);
      t.start();
    }
    catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private Runnable constructRunnable(Class<?> e) {
    try {
      Object o = contructEmbedded(e);
      if (Runnable.class.isInstance(o))
        return Runnable.class.cast(o);

      throw new RuntimeException("net.stickycode.deploy.Embedded is not Runnable!");
    }
    catch (InstantiationException e1) {
      throw new RuntimeException(e1);
    }
    catch (IllegalAccessException e1) {
      throw new RuntimeException(e1);
    }
  }

  private Object contructEmbedded(Class<?> embedded) throws InstantiationException, IllegalAccessException {
    try {
      Constructor<?> c = embedded.getConstructor(new Class[] {String[].class});
      return c.newInstance(new Object[] {args});
    }
    catch (SecurityException e1) {
      throw new RuntimeException(e1);
    }
    catch (NoSuchMethodException e1) {
      throw new RuntimeException(e1);
    }
    catch (IllegalArgumentException e) {
      throw new RuntimeException(e);
    }
    catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

}
