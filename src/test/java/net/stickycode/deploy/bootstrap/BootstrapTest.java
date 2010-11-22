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

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;


public class BootstrapTest {

  private File target = new File("target/samples");

  @Test
  public void boot() throws ClassNotFoundException {
    File application = new File("/home/michael/src/stickycode/trunk/net.stickycode.examples/sticky-example-boostrap/target/sticky-example-bootstrap-1.1-SNAPSHOT-sample.jar");
//    File application = new File(target, "sticky-deployer-sample-2jar-sample.zip");
    StickyBootstrap b = new StickyBootstrap(application);
    b.boot();
    Class<?> klass = b.load("net.stickycode.stereotype.Configured");
    assertThat(klass).isNotNull();
    Class<?> k2 = b.load("net.stickycode.exception.PermanentException");
    assertThat(k2).isNotNull();
//    b.load("net.stickcode.deploy.tomcat.DeploymentConfiguration");
  }

}
