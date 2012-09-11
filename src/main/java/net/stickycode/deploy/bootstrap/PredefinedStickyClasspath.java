package net.stickycode.deploy.bootstrap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PredefinedStickyClasspath
    implements StickyClasspath {

  private List<StickyLibrary> libraries = new ArrayList<StickyLibrary>();

  private Map<String, StickyLibrary> lookup = new HashMap<String, StickyLibrary>();

  /**
   * Load a file that looks like
   * 
   * <pre>
   * !WEB-INF/lib/blah-1.1.jar
   * path/to/some/Blah.class
   * path/to/some/resource.properties
   * !WEB-INF/lib/other-1.2.jar
   * path/to/other/Type.class
   * </pre>
   */
  StickyClasspath load(InputStream classpath) {
    StickyLibrary current = null;
    for (String line : new LineIterable(classpath)) {
      if (line.startsWith("!")) {
        current = new StickyLibrary(line);
        libraries.add(current);
        lookup.put(line.substring(1), current);
      }
      else
        current.add(line);
    }

    return this;
  }

  @Override
  public List<StickyLibrary> getLibraries() {
    return libraries;
  }

  @Override
  public StickyLibrary getLibrary(String path) {
    return lookup.get(path);
  }

}
