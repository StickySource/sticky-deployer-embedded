package net.stickycode.deploy.bootstrap;

import java.io.File;
import java.io.InputStream;

public class StickyLauncher {

  void configure(String[] args) {
    for (String s : args) {
      if ("--launcher-debug".equals(s))
        System.setProperty("launcher.debug", "true");

      if ("--launcher-verbose".equals(s))
        System.setProperty("launcher.verbose", "true");
    }
  }

  StickyClasspath buildClasspath() {
    InputStream classpath = StickyLauncher.class.getClassLoader().getResourceAsStream("META-INF/sticky/application.classpath");
    if (classpath != null) {
      return new PredefinedStickyClasspath().load(classpath);
    }

    File application = new File(ZipScanningStickyClasspath.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    return new ZipScanningStickyClasspath().loadZip(application);
  }

  void embed(ClassLoader systemClassLoader, String[] args) {
    StickyEmbedder embedder = new StickyEmbedder(args);
    StickyClasspath buildClasspath = buildClasspath();
    embedder.initialise(systemClassLoader, buildClasspath);
    embedder.launch();
  }

  public static void main(String[] args) throws InterruptedException {
    StickyLauncher launcher = new StickyLauncher();
    launcher.configure(args);
    launcher.embed(ClassLoader.getSystemClassLoader(), args);
  }

}
