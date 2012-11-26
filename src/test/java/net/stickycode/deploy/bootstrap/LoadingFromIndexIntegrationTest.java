package net.stickycode.deploy.bootstrap;

import org.junit.Test;


public class LoadingFromIndexIntegrationTest {

  
  @Test
  public void testInEclipse() {
    System.out.println(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
    for (String name  : System.getProperties().stringPropertyNames()) {
//      if (name.contains("LIP"))
        System.out.println(name + " = " + System.getProperty(name));
        
    }
  }
}
