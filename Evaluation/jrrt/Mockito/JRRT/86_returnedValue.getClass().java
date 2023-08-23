package org.mockito.internal.stubbing.answers;
import org.mockito.exceptions.Reporter;
import org.mockito.invocation.Invocation;
import org.mockito.stubbing.Answer;

public class AnswersValidator  {
  final private Reporter reporter = new Reporter();
  public void validate(Answer<?> answer, Invocation invocation) {
    MethodInfo methodInfo = new MethodInfo(invocation);
    if(answer instanceof ThrowsException) {
      validateException((ThrowsException)answer, methodInfo);
    }
    if(answer instanceof Returns) {
      validateReturnValue((Returns)answer, methodInfo);
    }
    if(answer instanceof DoesNothing) {
      validateDoNothing((DoesNothing)answer, methodInfo);
    }
    if(answer instanceof CallsRealMethods) {
      validateMockingConcreteClass((CallsRealMethods)answer, methodInfo);
    }
    if(answer instanceof ReturnsArgumentAt) {
      ReturnsArgumentAt returnsArgumentAt = (ReturnsArgumentAt)answer;
      validateReturnArgIdentity(returnsArgumentAt, invocation);
    }
  }
  public void validateDefaultAnswerReturnedValue(Invocation invocation, Object returnedValue) {
    MethodInfo methodInfo = new MethodInfo(invocation);
    Class<? extends Object> var_86 = returnedValue.getClass();
    if(returnedValue != null && !methodInfo.isValidReturnType(var_86)) {
      reporter.wrongTypeReturnedByDefaultAnswer(invocation.getMock(), methodInfo.printMethodReturnType(), returnedValue.getClass().getSimpleName(), methodInfo.getMethodName());
    }
  }
  private void validateDoNothing(DoesNothing answer, MethodInfo methodInfo) {
    if(!methodInfo.isVoid()) {
      reporter.onlyVoidMethodsCanBeSetToDoNothing();
    }
  }
  private void validateException(ThrowsException answer, MethodInfo methodInfo) {
    Throwable throwable = answer.getThrowable();
    if(throwable == null) {
      reporter.cannotStubWithNullThrowable();
    }
    if(throwable instanceof RuntimeException || throwable instanceof Error) {
      return ;
    }
    if(!methodInfo.isValidException(throwable)) {
      reporter.checkedExceptionInvalid(throwable);
    }
  }
  private void validateMockingConcreteClass(CallsRealMethods answer, MethodInfo methodInfo) {
    if(methodInfo.isAbstract()) {
      reporter.cannotCallAbstractRealMethod();
    }
  }
  private void validateReturnArgIdentity(ReturnsArgumentAt returnsArgumentAt, Invocation invocation) {
    returnsArgumentAt.validateIndexWithinInvocationRange(invocation);
    MethodInfo methodInfo = new MethodInfo(invocation);
    if(!methodInfo.isValidReturnType(returnsArgumentAt.returnedTypeOnSignature(invocation))) {
      new Reporter().wrongTypeOfArgumentToReturn(invocation, methodInfo.printMethodReturnType(), returnsArgumentAt.returnedTypeOnSignature(invocation), returnsArgumentAt.wantedArgumentPosition());
    }
  }
  private void validateReturnValue(Returns answer, MethodInfo methodInfo) {
    if(methodInfo.isVoid()) {
      reporter.cannotStubVoidMethodWithAReturnValue(methodInfo.getMethodName());
    }
    if(answer.returnsNull() && methodInfo.returnsPrimitive()) {
      reporter.wrongTypeOfReturnValue(methodInfo.printMethodReturnType(), "null", methodInfo.getMethodName());
    }
    if(!answer.returnsNull() && !methodInfo.isValidReturnType(answer.getReturnType())) {
      reporter.wrongTypeOfReturnValue(methodInfo.printMethodReturnType(), answer.printReturnType(), methodInfo.getMethodName());
    }
  }
}