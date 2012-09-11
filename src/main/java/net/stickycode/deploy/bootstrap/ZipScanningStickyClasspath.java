package net.stickycode.deploy.bootstrap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipScanningStickyClasspath
    implements StickyClasspath {

  private StickyLogger log = StickyLogger.getLogger(getClass());

  private List<StickyLibrary> libraries = new ArrayList<StickyLibrary>();

  private Map<String, StickyLibrary> lookup = new HashMap<String, StickyLibrary>();

  StickyClasspath loadZip(File application) {
    try {
      ZipFile file = new ZipFile(application);
      try {
        loadEntries(file);
      }
      finally {
        file.close();
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

    return this;
  }

  protected void loadEntries(ZipFile applicationFile) {
    for (ZipEntry zipEntry : listZipEntries(applicationFile))
      if (zipEntry.getName().endsWith(".jar")) {
        log.info("Loading jar %s", zipEntry.getName());
        StickyLibrary library = new StickyLibrary(zipEntry.getName());
        libraries.add(library);
        lookup.put(zipEntry.getName(), library);
        indexLibrary(applicationFile, library);
      }
  }

  @SuppressWarnings("unchecked")
  private List<ZipEntry> listZipEntries(ZipFile applicationFile) {
    return (List<ZipEntry>) Collections.list(applicationFile.entries());
  }

  private void indexLibrary(ZipFile applicationFile, StickyLibrary library) {
    ZipEntry jar = applicationFile.getEntry(library.getJarPath());
    try {
      processEntry(applicationFile, jar, library);
      log.debug("Found %s classes and %s resources in jar %s", library.getClasses().size(), library.getResources().size(), library);
    }
    catch (IOException e) {
      log.error("failed to load", e);
    }
  }

  private void processEntry(ZipFile applicationFile, ZipEntry jar, StickyLibrary library)
      throws IOException {
    JarInputStream i = new JarInputStream(applicationFile.getInputStream(jar));
    processManifest(i, library);

    JarEntry current = i.getNextJarEntry();
    while (current != null) {
      if (!current.isDirectory())
        library.add(current.getName());

      i.closeEntry();
      current = i.getNextJarEntry();
    }

    i.closeEntry();
  }

  private void processManifest(JarInputStream i, StickyLibrary library) {
    Manifest manifest = i.getManifest();
    if (manifest != null) {
      String mainClass = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
      if (mainClass != null)
        library.addMain(mainClass);
    }
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
