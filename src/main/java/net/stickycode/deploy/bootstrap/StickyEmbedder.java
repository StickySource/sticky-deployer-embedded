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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;


public class StickyEmbedder {

  private boolean debug;
  private boolean trace;

  private final List<StickyLibrary> libraries = new LinkedList<StickyLibrary>();
  private final File application;
  private final String[] args;
  private StickyClassLoader classLoader;

  public StickyEmbedder(String... args) {
    this.args = args;
    for (String s : args) {
      if ("--debug".equals(s))
        this.debug = true;

      if ("--trace".equals(s))
        this.trace = true;
    }

    application = deriveApplicationFile();
  }

  public void initialise(ClassLoader parent) {
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
    classLoader = new StickyClassLoader(parent, this);
  }

  protected File deriveApplicationFile() {
    return new File(StickyEmbedder.class.getProtectionDomain().getCodeSource().getLocation().getPath());
  }

  protected void loadEntries(ZipFile file) throws MalformedURLException {
    Enumeration<? extends ZipEntry> entries = file.entries();
    while (entries.hasMoreElements()) {
      ZipEntry zipEntry = (ZipEntry) entries.nextElement();
      if (zipEntry.getName().endsWith(".jar")) {
        debug("Loading jar %s", zipEntry.getName());
        libraries.add(new StickyLibrary(this, file, zipEntry.getName()));
      }
    }
  }

  public static void main(String[] args) {
    System.out.println("Starting StickyEmbedder");
    StickyEmbedder embedder = new StickyEmbedder(args);
    embedder.initialise(ClassLoader.getSystemClassLoader());
    embedder.launch();
  }

  public List<StickyLibrary> getLibraries() {
    return libraries;
  }

  public void launch() {
    try {
      launchClass("net.stickycode.deploy.Embedded");
    }
    catch (ClassNotFoundException e) {
      debug("net.stickycode.deploy.Embedded not found");
      for (StickyLibrary library : libraries) {
        if (library.hasMainClass())
          try {
            launchClass(library.getMainClass());
          }
          catch (ClassNotFoundException e1) {
            debug(e1.getMessage());
          }
      }
    }
  }

  private void launchClass(String className) throws ClassNotFoundException {
    debug("Attempting to load %s", className);
    Class<?> e = classLoader.loadClass(className);
    if (Runnable.class.isAssignableFrom(e))
      launchRunnable(classLoader, e);
    else
      launchMain(classLoader, e);
  }

  private void launchMain(StickyClassLoader l, Class<?> klass) {
    debug("Loading net.stickycode.deploy.Embedded by main method");
    try {
      Method main = klass.getMethod("main", new Class[] {String[].class});
      main.invoke(null, new Object[] {args});
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
    catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }

  }

  private void launchRunnable(StickyClassLoader l, Class<?> e) {
    debug("Loading net.stickycode.deploy.Embedded as Runnable");
    Runnable r = constructRunnable(e);
    Thread t = new Thread(r);
    t.setContextClassLoader(l);
    t.setDaemon(false);
    t.start();
    try {
      t.join();
    }
    catch (InterruptedException e1) {
      throw new RuntimeException(e1);
    }
  }

  private Runnable constructRunnable(Class<?> e) {
    try {
      Object o = contructEmbedded(e);
      return Runnable.class.cast(o);
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

  public void debug(String message, Object... parameters) {
    if (debug)
      System.err.println(String.format(message, parameters));
  }

  public void trace(String message, Object... parameters) {
    if (trace)
      System.err.println(String.format(message, parameters));
  }


  public StickyClassLoader getClassLoader() {
    return classLoader;
  }

  public StickyLibrary getLibrary(String jarPath) {
    for (StickyLibrary l : libraries) {
      if (l.getJarPath().equals(jarPath))
        return l;
    }

    throw new RuntimeException(jarPath + " not found");
  }

}
