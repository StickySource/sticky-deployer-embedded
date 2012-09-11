package net.stickycode.deploy.bootstrap;

import java.util.List;


public interface StickyClasspath {

  List<StickyLibrary> getLibraries();
  
  StickyLibrary getLibrary(String path);

}
