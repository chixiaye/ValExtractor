package org.apache.commons.lang3.concurrent;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class BasicThreadFactory implements ThreadFactory  {
  final private AtomicLong threadCounter;
  final private ThreadFactory wrappedFactory;
  final private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
  final private String namingPattern;
  final private Integer priority;
  final private Boolean daemonFlag;
  private BasicThreadFactory(final Builder builder) {
    super();
    if(builder.wrappedFactory == null) {
      wrappedFactory = Executors.defaultThreadFactory();
    }
    else {
      wrappedFactory = builder.wrappedFactory;
    }
    namingPattern = builder.namingPattern;
    priority = builder.priority;
    daemonFlag = builder.daemonFlag;
    uncaughtExceptionHandler = builder.exceptionHandler;
    threadCounter = new AtomicLong();
  }
  final public Boolean getDaemonFlag() {
    return daemonFlag;
  }
  final public Integer getPriority() {
    return priority;
  }
  final public String getNamingPattern() {
    return namingPattern;
  }
  final public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
    return uncaughtExceptionHandler;
  }
  @Override() public Thread newThread(final Runnable r) {
    final Thread t = getWrappedFactory().newThread(r);
    initializeThread(t);
    return t;
  }
  final public ThreadFactory getWrappedFactory() {
    return wrappedFactory;
  }
  public long getThreadCount() {
    return threadCounter.get();
  }
  private void initializeThread(final Thread t) {
    if(getNamingPattern() != null) {
      final Long count = Long.valueOf(threadCounter.incrementAndGet());
      t.setName(String.format(getNamingPattern(), count));
    }
    Thread.UncaughtExceptionHandler var_343 = getUncaughtExceptionHandler();
    if(var_343 != null) {
      t.setUncaughtExceptionHandler(getUncaughtExceptionHandler());
    }
    if(getPriority() != null) {
      t.setPriority(getPriority().intValue());
    }
    if(getDaemonFlag() != null) {
      t.setDaemon(getDaemonFlag().booleanValue());
    }
  }
  
  public static class Builder implements org.apache.commons.lang3.builder.Builder<BasicThreadFactory>  {
    private ThreadFactory wrappedFactory;
    private Thread.UncaughtExceptionHandler exceptionHandler;
    private String namingPattern;
    private Integer priority;
    private Boolean daemonFlag;
    @Override() public BasicThreadFactory build() {
      final BasicThreadFactory factory = new BasicThreadFactory(this);
      reset();
      return factory;
    }
    public Builder daemon(final boolean f) {
      daemonFlag = Boolean.valueOf(f);
      return this;
    }
    public Builder namingPattern(final String pattern) {
      if(pattern == null) {
        throw new NullPointerException("Naming pattern must not be null!");
      }
      namingPattern = pattern;
      return this;
    }
    public Builder priority(final int prio) {
      priority = Integer.valueOf(prio);
      return this;
    }
    public Builder uncaughtExceptionHandler(final Thread.UncaughtExceptionHandler handler) {
      if(handler == null) {
        throw new NullPointerException("Uncaught exception handler must not be null!");
      }
      exceptionHandler = handler;
      return this;
    }
    public Builder wrappedFactory(final ThreadFactory factory) {
      if(factory == null) {
        throw new NullPointerException("Wrapped ThreadFactory must not be null!");
      }
      wrappedFactory = factory;
      return this;
    }
    public void reset() {
      wrappedFactory = null;
      exceptionHandler = null;
      namingPattern = null;
      priority = null;
      daemonFlag = null;
    }
  }
}