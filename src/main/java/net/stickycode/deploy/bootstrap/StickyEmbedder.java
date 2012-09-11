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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class StickyEmbedder {

  private StickyLogger log = StickyLogger.getLogger(getClass());

  private final List<StickyLibrary> libraries = new LinkedList<StickyLibrary>();

  private final String[] args;

  private StickyClassLoader classLoader;

  private StickyClasspath classpath;

  public StickyEmbedder(String... args) {
    this.args = args;
  }

  public void initialise(ClassLoader loader, StickyClasspath classpath) {
    this.classpath = classpath;
    classLoader = new StickyClassLoader(loader, classpath);
  }

  public List<StickyLibrary> getLibraries() {
    return classpath.getLibraries();
  }

  public void launch() {
    try {
      launchClass("net.stickycode.deploy.Embedded");
    }
    catch (ClassNotFoundException e) {
      Object[] parameters = {};
      log.info("net.stickycode.deploy.Embedded not found", parameters);
      for (StickyLibrary library : libraries) {
        if (library.hasMainClass())
          try {
            launchClass(library.getMainClass());
          }
          catch (ClassNotFoundException e1) {
            Object[] parameters1 = {};
            log.info(e1.getMessage(), parameters1);
          }
      }
    }
  }

  private void launchClass(String className)
      throws ClassNotFoundException {
    Object[] parameters = { className };
    log.info("Attempting to load %s", parameters);
    Class<?> e = classLoader.loadClass(className);
    if (Runnable.class.isAssignableFrom(e))
      launchRunnable(classLoader, e);
    else
      launchMain(classLoader, e);
  }

  private void launchMain(StickyClassLoader l, Class<?> klass) {
    Object[] parameters = {};
    log.info("Loading net.stickycode.deploy.Embedded by main method", parameters);
    try {
      Method main = klass.getMethod("main", new Class[] { String[].class });
      main.invoke(null, new Object[] { args });
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
    Object[] parameters = {};
    log.info("Loading net.stickycode.deploy.Embedded as Runnable", parameters);
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

  private Object contructEmbedded(Class<?> embedded)
      throws InstantiationException, IllegalAccessException {
    try {
      Constructor<?> c = embedded.getConstructor(new Class[] { String[].class });
      return c.newInstance(new Object[] { args });
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
