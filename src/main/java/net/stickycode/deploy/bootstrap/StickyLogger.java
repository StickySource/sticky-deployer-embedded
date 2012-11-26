package net.stickycode.deploy.bootstrap;

public class StickyLogger {

  private boolean debug = new Boolean(System.getProperty("launcher.debug"));

  private boolean verbose = new Boolean(System.getProperty("launcher.verbose"));

  private Class<?> callee;

  public StickyLogger(Class<?> callee) {
    this.callee = callee;
  }

  public static StickyLogger getLogger(Class<?> callee) {
    return new StickyLogger(callee);
  }

  public void debug(String message, Object... args) {
    if (debug)
      out("D ", message, args);
  }

  public void info(String message, Object... args) {
    if (verbose || debug)
      out("I ", message, args);
  }

  public void error(String message, Object... args) {
    err("E ", message, args);
  }

  public void error(Throwable t, String message, Object... args) {
    err("E ", message, args);
    t.printStackTrace();
  }

  private void out(String s, String message, Object... args) {
    System.out.print(String.format("%s %-60s ", s, callee.getName()));
    System.out.println(String.format(message, args));
  }

  private void err(String message, String s, Object... args) {
    System.err.print(s);
    System.err.print(callee.getSimpleName());
    System.err.print(" ");
    System.err.println(String.format(message, args));
  }
}
