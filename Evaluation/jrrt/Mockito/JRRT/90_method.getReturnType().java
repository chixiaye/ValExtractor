package org.mockito.internal.stubbing.answers;
import org.mockito.internal.invocation.AbstractAwareMethod;
import org.mockito.internal.util.Primitives;
import org.mockito.invocation.Invocation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MethodInfo implements AbstractAwareMethod  {
  final private Method method;
  public MethodInfo(Invocation theInvocation) {
    super();
    this.method = theInvocation.getMethod();
  }
  public Method getMethod() {
    return method;
  }
  public String getMethodName() {
    return method.getName();
  }
  public String printMethodReturnType() {
    return method.getReturnType().getSimpleName();
  }
  public boolean isAbstract() {
    return (method.getModifiers() & Modifier.ABSTRACT) != 0;
  }
  public boolean isDeclaredOnInterface() {
    return method.getDeclaringClass().isInterface();
  }
  public boolean isValidException(Throwable throwable) {
    Class<?>[] exceptions = method.getExceptionTypes();
    Class<?> throwableClass = throwable.getClass();
    for (Class<?> exception : exceptions) {
      if(exception.isAssignableFrom(throwableClass)) {
        return true;
      }
    }
    return false;
  }
  public boolean isValidReturnType(Class clazz) {
    Class<?> var_90 = method.getReturnType();
    if(var_90.isPrimitive() || clazz.isPrimitive()) {
      return Primitives.primitiveTypeOf(clazz) == Primitives.primitiveTypeOf(method.getReturnType());
    }
    else {
      return method.getReturnType().isAssignableFrom(clazz);
    }
  }
  public boolean isVoid() {
    return this.method.getReturnType() == Void.TYPE;
  }
  public boolean returnsPrimitive() {
    return method.getReturnType().isPrimitive();
  }
}