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

import org.junit.Ignore;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;


public class BootstrapFunctionalTest {

  private File target = new File("target/samples");

  @Test
  public void boot() throws ClassNotFoundException {
//    File application = new File(target, "sticky-deployer-sample-2jar-sample.zip");
    StickyEmbedder b = new StickyEmbedder() {
      @Override
      protected File deriveApplicationFile() {
        return new File("target/sticky-deployer-embedded-sample.jar");
      }
    };
//    assertThat(b.getLibraries()).containsExactly(
//        "RED-INF/lib/sticky-deployer-tomcat6-2.2.jar",
//        "RED-INF/lib/coyote-6.0.29.jar",
//        "RED-INF/lib/catalina-6.0.29.jar",
//        "RED-INF/lib/servlet-api-6.0.29.jar",
//        "RED-INF/lib/juli-6.0.29.jar",
//        "RED-INF/lib/sticky-cli-1.3.jar",
//        "RED-INF/lib/sticky-reflector-1.1.jar",
//        "RED-INF/lib/sticky-configured-1.2.jar",
//        "RED-INF/lib/sticky-exception-1.3.jar",
//        "RED-INF/lib/sticky-coercion-1.1.jar",
//        "RED-INF/lib/sticky-stereotype-1.1.jar",
//        "RED-INF/lib/javax.inject-1.jar"
//        );
    assertThat(b.getLibraries()).hasSize(12);
    assertThat(b.getLibraries().iterator().next().getClasses()).hasSize(7);
    assertThat(b.getLibraries().iterator().next().getResources()).hasSize(7);

//    b.launch();
//    Class<?> klass = b.load("net.stickycode.stereotype.Configured");
//    assertThat(klass).isNotNull();
//    Class<?> k2 = b.load("net.stickycode.exception.PermanentException");
//    assertThat(k2).isNotNull();
//    b.load("net.stickcode.deploy.tomcat.DeploymentConfiguration");
  }

}
