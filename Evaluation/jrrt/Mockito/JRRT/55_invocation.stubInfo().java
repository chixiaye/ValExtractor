package org.mockito.internal.listeners;
import org.mockito.invocation.DescribedInvocation;
import org.mockito.invocation.Invocation;
import org.mockito.listeners.MethodInvocationReport;
import static org.mockito.internal.matchers.Equality.areEqual;

public class NotifiedMethodInvocationReport implements MethodInvocationReport  {
  final private Invocation invocation;
  final private Object returnedValue;
  final private Throwable throwable;
  public NotifiedMethodInvocationReport(Invocation invocation, Object returnedValue) {
    super();
    this.invocation = invocation;
    this.returnedValue = returnedValue;
    this.throwable = null;
  }
  public NotifiedMethodInvocationReport(Invocation invocation, Throwable throwable) {
    super();
    this.invocation = invocation;
    this.returnedValue = null;
    this.throwable = throwable;
  }
  public DescribedInvocation getInvocation() {
    return invocation;
  }
  public Object getReturnedValue() {
    return returnedValue;
  }
  public String getLocationOfStubbing() {
    org.mockito.invocation.StubInfo var_55 = invocation.stubInfo();
    return (var_55 == null) ? null : invocation.stubInfo().stubbedAt().toString();
  }
  public Throwable getThrowable() {
    return throwable;
  }
  public boolean equals(Object o) {
    if(this == o) 
      return true;
    if(o == null || getClass() != o.getClass()) 
      return false;
    NotifiedMethodInvocationReport that = (NotifiedMethodInvocationReport)o;
    return areEqual(invocation, that.invocation) && areEqual(returnedValue, that.returnedValue) && areEqual(throwable, that.throwable);
  }
  public boolean threwException() {
    return throwable != null;
  }
  public int hashCode() {
    int result = invocation != null ? invocation.hashCode() : 0;
    result = 31 * result + (returnedValue != null ? returnedValue.hashCode() : 0);
    result = 31 * result + (throwable != null ? throwable.hashCode() : 0);
    return result;
  }
}