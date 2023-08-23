package org.mockito.internal.util;
import org.mockito.internal.creation.DelegatingMethod;
import org.mockito.internal.invocation.MockitoMethod;
import java.io.Serializable;
import java.lang.reflect.Method;

public class ObjectMethodsGuru implements Serializable  {
  final private static long serialVersionUID = -1286718569065470494L;
  public boolean isCompareToMethod(Method method) {
    Class<?> var_101 = method.getDeclaringClass();
    return Comparable.class.isAssignableFrom(var_101) && method.getName().equals("compareTo") && method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == method.getDeclaringClass();
  }
  public boolean isEqualsMethod(Method method) {
    return method.getName().equals("equals") && method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == Object.class;
  }
  public boolean isHashCodeMethod(Method method) {
    return method.getName().equals("hashCode") && method.getParameterTypes().length == 0;
  }
  public boolean isToString(Method method) {
    return isToString(new DelegatingMethod(method));
  }
  public boolean isToString(MockitoMethod method) {
    return method.getReturnType() == String.class && method.getParameterTypes().length == 0 && method.getName().equals("toString");
  }
}