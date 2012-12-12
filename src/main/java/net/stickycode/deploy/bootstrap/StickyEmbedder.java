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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Pattern;

public class StickyEmbedder {

  private StickyLogger log = StickyLogger.getLogger(getClass());

  private final String[] args;

  private StickyClassLoader classLoader;

  private StickyClasspath classpath;

  private Pattern main = Pattern.compile("[A-Z][a-zA-Z0-9_]*");

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
      launchClassExplosively("net.stickycode.deploy.Embedded");
    }
    catch (ClassNotFoundException e) {
      log.info("net.stickycode.deploy.Embedded not found");
      loadByMain();
    }
  }

  private void loadByMain() {
    if (classpath.hasSingularMain()) {
      log.info("One main found, launching %s", classpath.getSingularMain());
      launchClass(classpath.getSingularMain());
    }
    else {
      if (args.length < 0)
        usage();
      else
        bootSelection();
    }
  }

  private void bootSelection() {
    StickyLibrary selected = selectMain();
    if (selected != null)
      launchClass(selected.getMainClass());
  }

  private void usage() {
    System.out.println();
    String path = getJarPath();
    System.out.println("Usage: java -jar " + path + " Command arg1 arg2 arg3");
    for (StickyLibrary l : classpath.getLibraries()) {
      if (l.hasMainClass()) {
        System.out.print("  ");
        String mainClass = l.getMainClass();
        System.out.println(mainClass.substring(mainClass.lastIndexOf('.') + 1) + " - " + l.getDescription());
      }
    }
    System.out.println();
  }

  private String getJarPath()
  {
    String path = StickyEmbedder.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    try {
      return URLDecoder.decode(path, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException("UTF-8 is always supported, right?", e);
    }
  }

  private StickyLibrary selectMain() {
    for (String arg : args)
      if (main.matcher(arg).matches())
        for (StickyLibrary l : classpath.getLibrariesByMain(arg))
          return l;

    System.err.println("No command was specified");
    usage();
    return null;
  }

  private void launchClass(String className) {
    try {
      launchClassExplosively(className);
    }
    catch (ClassNotFoundException e1) {
      log.info("Could not find %s to launch", e1.getMessage());
    }
  }

  private void launchClassExplosively(String className)
      throws ClassNotFoundException {
    log.info("Attempting to load %s", className);
    Class<?> e = classLoader.loadClass(className);
    if (Runnable.class.isAssignableFrom(e))
      launchRunnable(classLoader, e);
    else
      launchMain(classLoader, e);
  }

  private void launchMain(StickyClassLoader l, Class<?> klass) {
    log.info("Loading %s by main method with args %s", klass, args);
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
    for (StickyLibrary l : classpath.getLibraries()) {
      if (l.getJarPath().equals(jarPath))
        return l;
    }

    throw new RuntimeException(jarPath + " not found");
  }

}
