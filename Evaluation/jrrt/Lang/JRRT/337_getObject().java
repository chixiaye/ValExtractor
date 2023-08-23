package org.apache.commons.lang3.concurrent;
import org.apache.commons.lang3.ObjectUtils;
public class ConstantInitializer<T extends java.lang.Object> implements ConcurrentInitializer<T>  {
  final private static String FMT_TO_STRING = "ConstantInitializer@%d [ object = %s ]";
  final private T object;
  public ConstantInitializer(final T obj) {
    super();
    object = obj;
  }
  @Override() public String toString() {
    return String.format(FMT_TO_STRING, Integer.valueOf(System.identityHashCode(this)), String.valueOf(getObject()));
  }
  @Override() public T get() throws ConcurrentException {
    return getObject();
  }
  final public T getObject() {
    return object;
  }
  @Override() public boolean equals(final Object obj) {
    if(this == obj) {
      return true;
    }
    if(!(obj instanceof ConstantInitializer<?>)) {
      return false;
    }
    final ConstantInitializer<?> c = (ConstantInitializer<?>)obj;
    return ObjectUtils.equals(getObject(), c.getObject());
  }
  @Override() public int hashCode() {
    T var_337 = getObject();
    return var_337 != null ? getObject().hashCode() : 0;
  }
}