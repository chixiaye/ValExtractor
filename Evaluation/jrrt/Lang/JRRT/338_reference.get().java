package org.apache.commons.lang3.concurrent;
import java.util.concurrent.atomic.AtomicReference;
abstract public class AtomicInitializer<T extends java.lang.Object> implements ConcurrentInitializer<T>  {
  final private AtomicReference<T> reference = new AtomicReference<T>();
  @Override() public T get() throws ConcurrentException {
    T var_338 = reference.get();
    T result = var_338;
    if(result == null) {
      result = initialize();
      if(!reference.compareAndSet(null, result)) {
        result = reference.get();
      }
    }
    return result;
  }
  abstract protected T initialize() throws ConcurrentException;
}