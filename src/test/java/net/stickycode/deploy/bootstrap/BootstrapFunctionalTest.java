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

import static org.fest.assertions.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

import org.junit.Test;

public class BootstrapFunctionalTest {

  static {
    System.setProperty("debug", "true");
    System.setProperty("verbose", "true");
  }

  @Test
  public void jarsWithDodgyStructures()
      throws ClassNotFoundException {
    StickyClasspath b = new ZipScanningStickyClasspath().loadZip(new File("src/test/samples/sticky-deployer-embedded-sample.jar"));
    assertThat(b.getLibraries()).hasSize(2);
    assertThat(b.getLibraries().iterator().next().getClasses()).hasSize(1);
    assertThat(b.getLibraries().iterator().next().getResources()).hasSize(4);
  }

  @Test
  public void jars()
      throws ClassNotFoundException {
    StickyClasspath b = new ZipScanningStickyClasspath().loadZip(new File("src/test/samples/sticky-deployer-sample-2jar-1.2-sample.jar"));
    assertThat(b.getLibraries()).hasSize(2);
    assertThat(b.getLibraries().get(0).getClasses()).hasSize(1);
    assertThat(b.getLibraries().get(0).getResources()).hasSize(8);
    assertThat(b.getLibraries().get(1).getClasses()).hasSize(1);
    assertThat(b.getLibraries().get(1).getResources()).hasSize(5);
  }

  @Test
  public void lookingUpResources()
      throws IOException, ClassNotFoundException {
    File file = new File("src/test/samples/sticky-deployer-sample-2jar-1.2-sample.jar");
    StickyClasspath classpath = new ZipScanningStickyClasspath().loadZip(file);
    StickyEmbedder b = new StickyEmbedder();
    b.initialise(new URLClassLoader(new URL[] { new URL("file://" + file.getAbsolutePath()) }, ClassLoader.getSystemClassLoader()), classpath);

    assertThat(classpath.getLibraries()).hasSize(2);
    assertThat(classpath.getLibraries().iterator().next().getClasses()).hasSize(1);
    assertThat(classpath.getLibraries().iterator().next().getResources()).hasSize(8);

    URL url = b.getClassLoader().findResource("net/stickycode/deploy/sample/babysteps/run.properties");
    assertThat(url).isNotNull();
    InputStream i = url.openStream();
    assertThat(i).isNotNull();
    assertThat(new BufferedReader(new InputStreamReader(i)).readLine()).isEqualTo("run=running is step 3");

    Enumeration<URL> e = b.getClassLoader().findResources("net/stickycode/deploy/sample/babysteps/run.properties");
    assertThat(e.hasMoreElements()).isTrue();
    assertThat(e.nextElement()).isNotNull();
    assertThat(e.hasMoreElements()).isFalse();

    Enumeration<URL> manifests = b.getClassLoader().findResources("net/stickycode/deploy/sample/duplicate.properties");
    assertThat(manifests.hasMoreElements()).isTrue();
    assertThat(manifests.nextElement()).isNotNull();
    assertThat(manifests.hasMoreElements()).isTrue();
    assertThat(manifests.nextElement()).isNotNull();
    assertThat(manifests.hasMoreElements()).isFalse();

    assertThat(b.getClassLoader().findResource("nothing/here")).isNull();
  }

  @Test
  public void lookingUpClasses()
      throws IOException, ClassNotFoundException {
    File file = new File("src/test/samples/sticky-deployer-sample-2jar-1.2-sample.jar");
    StickyClasspath classpath = new ZipScanningStickyClasspath().loadZip(file);
    StickyEmbedder b = new StickyEmbedder();
    b.initialise(new URLClassLoader(new URL[] { new URL("file://" + file.getAbsolutePath()) }, ClassLoader.getSystemClassLoader()), classpath);

    assertThat(classpath.getLibraries()).hasSize(2);
    assertThat(classpath.getLibraries().iterator().next().getClasses()).hasSize(1);
    assertThat(classpath.getLibraries().iterator().next().getResources()).hasSize(8);

    Class<?> type = b.getClassLoader().findClass("net.stickycode.deploy.sample.helloworld.HelloWorld");
    assertThat(type.getSimpleName()).isEqualTo("HelloWorld");
  }

  @Test(expected = ClassNotFoundException.class)
  public void classNotFound()
      throws IOException, ClassNotFoundException {
    File file = new File("src/test/samples/sticky-deployer-sample-2jar-1.2-sample.jar");
    StickyClasspath classpath = new ZipScanningStickyClasspath().loadZip(file);
    StickyEmbedder b = new StickyEmbedder();
    b.initialise(new URLClassLoader(new URL[] { new URL("file://" + file.getAbsolutePath()) }, ClassLoader.getSystemClassLoader()), classpath);

    assertThat(classpath.getLibraries()).hasSize(2);
    assertThat(classpath.getLibraries().iterator().next().getClasses()).hasSize(1);
    assertThat(classpath.getLibraries().iterator().next().getResources()).hasSize(8);

    b.getClassLoader().findClass("net.stickycode.deploy.sample.helloworld.HelloWorldNotHere");
  }
}
