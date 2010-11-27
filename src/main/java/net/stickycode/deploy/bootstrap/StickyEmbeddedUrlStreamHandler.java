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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;


public class StickyEmbeddedUrlStreamHandler
    extends URLStreamHandler {

  public static String PROTOCOL = "sticky-jar";

  private StickyEmbedder embedder;

  public StickyEmbeddedUrlStreamHandler(StickyEmbedder stickyEmbedder) {
    this.embedder = stickyEmbedder;
  }

  public URL createResourceUrl(String name, StickyLibrary j) {
    try {
      return new URL(null, PROTOCOL + ":" + j.getJarPath() + "!" + name, this);
    }
    catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }


  @Override
  protected URLConnection openConnection(URL u) throws IOException {
    String path = u.getPath();
    int separator = path.indexOf('!');
    StickyLibrary j = embedder.getLibrary(u.getPath().substring(0, separator));
    InputStream i = j.getInputStream(u.getPath().substring(separator + 1));
    return new StickyEmbeddedUrlConnection(u, u.getPath(), i);
  }

}
