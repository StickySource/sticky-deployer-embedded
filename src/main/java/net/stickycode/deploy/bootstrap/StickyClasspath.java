package net.stickycode.deploy.bootstrap;

import java.util.List;

public interface StickyClasspath {

  /**
   * Return all the libraries that are part of this classpath
   */
  List<StickyLibrary> getLibraries();

  /**
   * Return the library of the given path
   */
  StickyLibrary getLibrary(String path);

  /**
   * Return true if this classpath contains on one Main
   */
  boolean hasSingularMain();

  /**
   * Return the one main method of the classpath
   */
  String getSingularMain();

  /**
   * Return the libraries with a main matching the given shortname
   */
  List<StickyLibrary> getLibrariesByMain(String shortName);

}
