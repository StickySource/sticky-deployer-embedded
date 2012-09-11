package net.stickycode.deploy.bootstrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;

public class LineIterable
    implements Iterable<String> {

  public class LineIterator
      implements Iterator<String> {

    private final BufferedReader reader;

    private String nextLine;

    public LineIterator(BufferedReader bufferedReader) {
      this.reader = bufferedReader;
      this.nextLine = readLine();
    }

    private String readLine() {
      try {
        return reader.readLine();
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean hasNext() {
      return nextLine != null;
    }

    @Override
    public String next() {
      try {
        return nextLine;
      }
      finally {
        this.nextLine = readLine();
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("not implemented");
    }

  }

  private InputStream in;

  public LineIterable(InputStream classpath) {
    this.in = classpath;
  }

  @Override
  public Iterator<String> iterator() {
    return new LineIterator(new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8"))));
  }

}
