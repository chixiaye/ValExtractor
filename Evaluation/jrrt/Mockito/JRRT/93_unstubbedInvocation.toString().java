package org.mockito.internal.stubbing.defaultanswers;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import org.mockito.Mockito;
import org.mockito.exceptions.Reporter;
import org.mockito.internal.debugging.LocationImpl;
import org.mockito.invocation.Location;
import org.mockito.internal.util.ObjectMethodsGuru;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ReturnsSmartNulls implements Answer<Object>, Serializable  {
  final private static long serialVersionUID = 7618312406617949441L;
  final private Answer<Object> delegate = new ReturnsMoreEmptyValues();
  public Object answer(final InvocationOnMock invocation) throws Throwable {
    Object defaultReturnValue = delegate.answer(invocation);
    if(defaultReturnValue != null) {
      return defaultReturnValue;
    }
    Class<?> type = invocation.getMethod().getReturnType();
    if(!type.isPrimitive() && !Modifier.isFinal(type.getModifiers())) {
      final Location location = new LocationImpl();
      return Mockito.mock(type, new ThrowsSmartNullPointer(invocation, location));
    }
    return null;
  }
  
  private static class ThrowsSmartNullPointer implements Answer  {
    final private InvocationOnMock unstubbedInvocation;
    final private Location location;
    public ThrowsSmartNullPointer(InvocationOnMock unstubbedInvocation, Location location) {
      super();
      this.unstubbedInvocation = unstubbedInvocation;
      this.location = location;
    }
    public Object answer(InvocationOnMock currentInvocation) throws Throwable {
      if(new ObjectMethodsGuru().isToString(currentInvocation.getMethod())) {
        String var_93 = unstubbedInvocation.toString();
        return "SmartNull returned by this unstubbed method call on a mock:\n" + var_93;
      }
      new Reporter().smartNullPointerException(unstubbedInvocation.toString(), location);
      return null;
    }
  }
}