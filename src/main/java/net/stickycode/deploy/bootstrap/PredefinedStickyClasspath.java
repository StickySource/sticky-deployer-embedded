package net.stickycode.deploy.bootstrap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PredefinedStickyClasspath
    implements StickyClasspath {

  private StickyLogger logger = StickyLogger.getLogger(getClass());

  private List<StickyLibrary> libraries = new ArrayList<StickyLibrary>();

  private Map<String, StickyLibrary> lookup = new HashMap<String, StickyLibrary>();

  private int mainCount = 0;

  /**
   * Load a file that looks like
   * 
   * <pre>
   * !WEB-INF/lib/blah-1.1.jar
   * path/to/some/Blah.class
   * path/to/some/resource.properties
   * !WEB-INF/lib/other-1.2.jar
   * *path/to/Main.class
   * path/to/other/Type.class
   * </pre>
   */
  StickyClasspath load(InputStream classpath) {
    StickyLibrary current = null;
    for (String line : new LineIterable(classpath)) {
      if (line.startsWith("!")) {
        current = foundLibrary(line.substring(1));
      }
      else
        if (line.startsWith("*")) {
          String main = line.substring(1);
          logger.debug("Found main %s", main);
          current.addMain(main);
          mainCount += 1;
        }
        else if (line.startsWith("\"")) {
          current.setDescription(line.substring(1));
        }
        else
          current.add(line);
    }

    return this;
  }

  private StickyLibrary foundLibrary(String library) {
    logger.debug("Found index for %s", library);
    StickyLibrary current = new StickyLibrary(library);
    libraries.add(current);
    lookup.put(library, current);
    return current;
  }

  @Override
  public List<StickyLibrary> getLibraries() {
    return libraries;
  }

  @Override
  public StickyLibrary getLibrary(String path) {
    return lookup.get(path);
  }

  @Override
  public boolean hasSingularMain() {
    return mainCount == 1;
  }

  @Override
  public String getSingularMain() {
    for (StickyLibrary l : libraries)
      if (l.hasMainClass())
        return l.getMainClass();

    throw new RuntimeException("Should have a main but can't find it");
  }

  @Override
  public List<StickyLibrary> getLibrariesByMain(String shortName) {
    for (StickyLibrary l : libraries)
      if (l.hasMainClass())
        if (l.getMainClass().endsWith(shortName))
          return Collections.singletonList(l);

    return Collections.emptyList();
  }

}
