package org.mockito.internal.util.reflection;
import java.lang.reflect.Field;

public class Whitebox  {
  private static Field getField(Class<?> clazz, String field) {
    try {
      return clazz.getDeclaredField(field);
    }
    catch (NoSuchFieldException e) {
      return null;
    }
  }
  private static Field getFieldFromHierarchy(Class<?> clazz, String field) {
    Field var_106 = getField(clazz, field);
    Field f = var_106;
    while(f == null && clazz != Object.class){
      clazz = clazz.getSuperclass();
      f = getField(clazz, field);
    }
    if(f == null) {
      throw new RuntimeException("You want me to get this field: \'" + field + "\' on this class: \'" + clazz.getSimpleName() + "\' but this field is not declared withing hierarchy of this class!");
    }
    return f;
  }
  public static Object getInternalState(Object target, String field) {
    Class<?> c = target.getClass();
    try {
      Field f = getFieldFromHierarchy(c, field);
      f.setAccessible(true);
      return f.get(target);
    }
    catch (Exception e) {
      throw new RuntimeException("Unable to get internal state on a private field. Please report to mockito mailing list.", e);
    }
  }
  public static void setInternalState(Object target, String field, Object value) {
    Class<?> c = target.getClass();
    try {
      Field f = getFieldFromHierarchy(c, field);
      f.setAccessible(true);
      f.set(target, value);
    }
    catch (Exception e) {
      throw new RuntimeException("Unable to set internal state on a private field. Please report to mockito mailing list.", e);
    }
  }
}