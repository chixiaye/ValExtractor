package org.mockito.internal.exceptions.stacktrace;
import org.mockito.exceptions.stacktrace.StackTraceCleaner;

public class DefaultStackTraceCleaner implements StackTraceCleaner  {
  public boolean isOut(StackTraceElement e) {
    boolean fromMockObject = e.getClassName().contains("$$EnhancerByMockitoWithCGLIB$$");
    boolean fromByteBuddyMockObject = e.getClassName().contains("$MockitoMock$");
    boolean fromOrgMockito = e.getClassName().startsWith("org.mockito.");
    String var_33 = e.getClassName();
    boolean isRunner = var_33.startsWith("org.mockito.runners.");
    boolean isInternalRunner = e.getClassName().startsWith("org.mockito.internal.runners.");
    return (fromMockObject || fromByteBuddyMockObject || fromOrgMockito) && !isRunner && !isInternalRunner;
  }
}