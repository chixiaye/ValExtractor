package org.apache.commons.lang3.concurrent;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ConcurrentUtils  {
  private ConcurrentUtils() {
    super();
  }
  public static ConcurrentException extractCause(final ExecutionException ex) {
    if(ex == null || ex.getCause() == null) {
      return null;
    }
    throwCause(ex);
    return new ConcurrentException(ex.getMessage(), ex.getCause());
  }
  public static ConcurrentRuntimeException extractCauseUnchecked(final ExecutionException ex) {
    Throwable var_340 = ex.getCause();
    if(ex == null || var_340 == null) {
      return null;
    }
    throwCause(ex);
    return new ConcurrentRuntimeException(ex.getMessage(), ex.getCause());
  }
  public static  <T extends java.lang.Object> Future<T> constantFuture(final T value) {
    return new ConstantFuture<T>(value);
  }
  public static  <T extends java.lang.Object> T initialize(final ConcurrentInitializer<T> initializer) throws ConcurrentException {
    return initializer != null ? initializer.get() : null;
  }
  public static  <T extends java.lang.Object> T initializeUnchecked(final ConcurrentInitializer<T> initializer) {
    try {
      return initialize(initializer);
    }
    catch (final ConcurrentException cex) {
      throw new ConcurrentRuntimeException(cex.getCause());
    }
  }
  static Throwable checkedException(final Throwable ex) {
    if(ex != null && !(ex instanceof RuntimeException) && !(ex instanceof Error)) {
      return ex;
    }
    else {
      throw new IllegalArgumentException("Not a checked exception: " + ex);
    }
  }
  public static  <K extends java.lang.Object, V extends java.lang.Object> V createIfAbsent(final ConcurrentMap<K, V> map, final K key, final ConcurrentInitializer<V> init) throws ConcurrentException {
    if(map == null || init == null) {
      return null;
    }
    final V value = map.get(key);
    if(value == null) {
      return putIfAbsent(map, key, init.get());
    }
    return value;
  }
  public static  <K extends java.lang.Object, V extends java.lang.Object> V createIfAbsentUnchecked(final ConcurrentMap<K, V> map, final K key, final ConcurrentInitializer<V> init) {
    try {
      return createIfAbsent(map, key, init);
    }
    catch (final ConcurrentException cex) {
      throw new ConcurrentRuntimeException(cex.getCause());
    }
  }
  public static  <K extends java.lang.Object, V extends java.lang.Object> V putIfAbsent(final ConcurrentMap<K, V> map, final K key, final V value) {
    if(map == null) {
      return null;
    }
    final V result = map.putIfAbsent(key, value);
    return result != null ? result : value;
  }
  public static void handleCause(final ExecutionException ex) throws ConcurrentException {
    final ConcurrentException cex = extractCause(ex);
    if(cex != null) {
      throw cex;
    }
  }
  public static void handleCauseUnchecked(final ExecutionException ex) {
    final ConcurrentRuntimeException crex = extractCauseUnchecked(ex);
    if(crex != null) {
      throw crex;
    }
  }
  private static void throwCause(final ExecutionException ex) {
    if(ex.getCause() instanceof RuntimeException) {
      throw (RuntimeException)ex.getCause();
    }
    if(ex.getCause() instanceof Error) {
      throw (Error)ex.getCause();
    }
  }
  final static class ConstantFuture<T extends java.lang.Object> implements Future<T>  {
    final private T value;
    ConstantFuture(final T value) {
      super();
      this.value = value;
    }
    @Override() public T get() {
      return value;
    }
    @Override() public T get(final long timeout, final TimeUnit unit) {
      return value;
    }
    @Override() public boolean cancel(final boolean mayInterruptIfRunning) {
      return false;
    }
    @Override() public boolean isCancelled() {
      return false;
    }
    @Override() public boolean isDone() {
      return true;
    }
  }
}