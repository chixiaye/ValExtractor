package org.mockito.internal.creation.instance;
import static org.mockito.internal.util.StringJoiner.join;
import java.lang.reflect.Constructor;
import org.mockito.internal.util.reflection.AccessibilityChanger;

public class ConstructorInstantiator implements Instantiator  {
  final private Object outerClassInstance;
  public ConstructorInstantiator(Object outerClassInstance) {
    super();
    this.outerClassInstance = outerClassInstance;
  }
  private static  <T extends java.lang.Object> InstantiationException paramsException(Class<T> cls, Exception e) {
    return new InstantiationException(join("Unable to create instance of \'" + cls.getSimpleName() + "\'.", "Please ensure that the outer instance has correct type and that the target class has 0-arg constructor."), e);
  }
  @SuppressWarnings(value = {"unchecked", }) private static  <T extends java.lang.Object> T invokeConstructor(Constructor<?> constructor, Object ... params) throws java.lang.InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
    AccessibilityChanger accessibility = new AccessibilityChanger();
    accessibility.enableAccess(constructor);
    return (T)constructor.newInstance(params);
  }
  public  <T extends java.lang.Object> T newInstance(Class<T> cls) {
    if(outerClassInstance == null) {
      return noArgConstructor(cls);
    }
    return withParams(cls, outerClassInstance);
  }
  private static  <T extends java.lang.Object> T noArgConstructor(Class<T> cls) {
    try {
      return invokeConstructor(cls.getDeclaredConstructor());
    }
    catch (Throwable t) {
      throw new InstantiationException(join("Unable to create instance of \'" + cls.getSimpleName() + "\'.", "Please ensure it has 0-arg constructor which invokes cleanly."), t);
    }
  }
  private static  <T extends java.lang.Object> T withParams(Class<T> cls, Object ... params) {
    try {
      for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
        Class<?>[] types = constructor.getParameterTypes();
        if(paramsMatch(types, params)) {
          return invokeConstructor(constructor, params);
        }
      }
    }
    catch (Exception e) {
      throw paramsException(cls, e);
    }
    throw paramsException(cls, null);
  }
  private static boolean paramsMatch(Class<?>[] types, Object[] params) {
    int var_19 = params.length;
    if(var_19 != types.length) {
      return false;
    }
    for(int i = 0; i < params.length; i++) {
      if(!types[i].isInstance(params[i])) {
        return false;
      }
    }
    return true;
  }
}