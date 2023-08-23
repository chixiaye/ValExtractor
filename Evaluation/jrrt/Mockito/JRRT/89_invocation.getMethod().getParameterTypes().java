package org.mockito.internal.stubbing.answers;
import org.mockito.exceptions.Reporter;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import java.io.Serializable;

public class ReturnsArgumentAt implements Answer<Object>, Serializable  {
  final private static long serialVersionUID = -589315085166295101L;
  final public static int LAST_ARGUMENT = -1;
  final private int wantedArgumentPosition;
  public ReturnsArgumentAt(int wantedArgumentPosition) {
    super();
    this.wantedArgumentPosition = checkWithinAllowedRange(wantedArgumentPosition);
  }
  public Class returnedTypeOnSignature(InvocationOnMock invocation) {
    int actualArgumentPosition = actualArgumentPosition(invocation);
    if(!invocation.getMethod().isVarArgs()) {
      Class<?>[] var_89 = invocation.getMethod().getParameterTypes();
      return var_89[actualArgumentPosition];
    }
    Class<?>[] parameterTypes = invocation.getMethod().getParameterTypes();
    int varargPosition = parameterTypes.length - 1;
    if(actualArgumentPosition < varargPosition) {
      return parameterTypes[actualArgumentPosition];
    }
    else {
      return parameterTypes[varargPosition].getComponentType();
    }
  }
  public Object answer(InvocationOnMock invocation) throws Throwable {
    validateIndexWithinInvocationRange(invocation);
    return invocation.getArguments()[actualArgumentPosition(invocation)];
  }
  private boolean argumentPositionInRange(InvocationOnMock invocation) {
    int actualArgumentPosition = actualArgumentPosition(invocation);
    if(actualArgumentPosition < 0) {
      return false;
    }
    if(!invocation.getMethod().isVarArgs()) {
      return invocation.getArguments().length > actualArgumentPosition;
    }
    return true;
  }
  private boolean returningLastArg() {
    return wantedArgumentPosition == LAST_ARGUMENT;
  }
  private int actualArgumentPosition(InvocationOnMock invocation) {
    return returningLastArg() ? lastArgumentIndexOf(invocation) : argumentIndexOf(invocation);
  }
  private int argumentIndexOf(InvocationOnMock invocation) {
    return wantedArgumentPosition;
  }
  private int checkWithinAllowedRange(int argumentPosition) {
    if(argumentPosition != LAST_ARGUMENT && argumentPosition < 0) {
      new Reporter().invalidArgumentRangeAtIdentityAnswerCreationTime();
    }
    return argumentPosition;
  }
  private int lastArgumentIndexOf(InvocationOnMock invocation) {
    return invocation.getArguments().length - 1;
  }
  public int wantedArgumentPosition() {
    return wantedArgumentPosition;
  }
  public void validateIndexWithinInvocationRange(InvocationOnMock invocation) {
    if(!argumentPositionInRange(invocation)) {
      new Reporter().invalidArgumentPositionRangeAtInvocationTime(invocation, returningLastArg(), wantedArgumentPosition);
    }
  }
}