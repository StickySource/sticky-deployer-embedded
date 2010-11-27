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
import java.net.URL;
import java.net.URLConnection;


public class StickyEmbeddedUrlConnection
    extends URLConnection {

  private String path;
  private InputStream inputStream;

  protected StickyEmbeddedUrlConnection(URL url, String path, InputStream i) {
    super(url);
    this.path = url.getPath();
    this.inputStream = i;
  }

  @Override
  public void connect() throws IOException {

  }

  @Override
  public InputStream getInputStream() throws IOException {
    return inputStream;
  }

  @Override
  public String getContentType() {
    String contentType = URLConnection.getFileNameMap().getContentTypeFor(path);
    if (contentType != null)
      return contentType;

    return "text/plain";
  }

}
