package org.mockito.internal.handler;
import org.mockito.exceptions.Reporter;
import org.mockito.internal.InternalMockHandler;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.invocation.MatchersBinder;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.mockito.internal.stubbing.InvocationContainer;
import org.mockito.internal.stubbing.InvocationContainerImpl;
import org.mockito.internal.stubbing.OngoingStubbingImpl;
import org.mockito.internal.stubbing.StubbedInvocationMatcher;
import org.mockito.internal.stubbing.VoidMethodStubbableImpl;
import org.mockito.internal.stubbing.answers.AnswersValidator;
import org.mockito.internal.verification.MockAwareVerificationMode;
import org.mockito.internal.verification.VerificationDataImpl;
import org.mockito.invocation.Invocation;
import org.mockito.mock.MockCreationSettings;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.VoidMethodStubbable;
import org.mockito.verification.VerificationMode;
import java.util.List;
class MockHandlerImpl<T extends java.lang.Object> implements InternalMockHandler<T>  {
  final private static long serialVersionUID = -2917871070982574165L;
  InvocationContainerImpl invocationContainerImpl;
  MatchersBinder matchersBinder = new MatchersBinder();
  MockingProgress mockingProgress = new ThreadSafeMockingProgress();
  final private MockCreationSettings mockSettings;
  public MockHandlerImpl(MockCreationSettings mockSettings) {
    super();
    this.mockSettings = mockSettings;
    this.mockingProgress = new ThreadSafeMockingProgress();
    this.matchersBinder = new MatchersBinder();
    this.invocationContainerImpl = new InvocationContainerImpl(mockingProgress, mockSettings);
  }
  public InvocationContainer getInvocationContainer() {
    return invocationContainerImpl;
  }
  public MockCreationSettings getMockSettings() {
    return mockSettings;
  }
  public Object handle(Invocation invocation) throws Throwable {
    if(invocationContainerImpl.hasAnswersForStubbing()) {
      InvocationMatcher var_34 = matchersBinder.bindMatchers(mockingProgress.getArgumentMatcherStorage(), invocation);
      InvocationMatcher invocationMatcher = var_34;
      invocationContainerImpl.setMethodForStubbing(invocationMatcher);
      return null;
    }
    VerificationMode verificationMode = mockingProgress.pullVerificationMode();
    InvocationMatcher invocationMatcher = matchersBinder.bindMatchers(mockingProgress.getArgumentMatcherStorage(), invocation);
    mockingProgress.validateState();
    if(verificationMode != null) {
      if(((MockAwareVerificationMode)verificationMode).getMock() == invocation.getMock()) {
        VerificationDataImpl data = createVerificationData(invocationContainerImpl, invocationMatcher);
        verificationMode.verify(data);
        return null;
      }
      else {
        mockingProgress.verificationStarted(verificationMode);
      }
    }
    invocationContainerImpl.setInvocationForPotentialStubbing(invocationMatcher);
    OngoingStubbingImpl<T> ongoingStubbing = new OngoingStubbingImpl<T>(invocationContainerImpl);
    mockingProgress.reportOngoingStubbing(ongoingStubbing);
    StubbedInvocationMatcher stubbedInvocation = invocationContainerImpl.findAnswerFor(invocation);
    if(stubbedInvocation != null) {
      stubbedInvocation.captureArgumentsFrom(invocation);
      return stubbedInvocation.answer(invocation);
    }
    else {
      Object ret = mockSettings.getDefaultAnswer().answer(invocation);
      new AnswersValidator().validateDefaultAnswerReturnedValue(invocation, ret);
      invocationContainerImpl.resetInvocationForPotentialStubbing(invocationMatcher);
      return ret;
    }
  }
  private VerificationDataImpl createVerificationData(InvocationContainerImpl invocationContainerImpl, InvocationMatcher invocationMatcher) {
    if(mockSettings.isStubOnly()) {
      new Reporter().stubPassedToVerify();
    }
    return new VerificationDataImpl(invocationContainerImpl, invocationMatcher);
  }
  public VoidMethodStubbable<T> voidMethodStubbable(T mock) {
    return new VoidMethodStubbableImpl<T>(mock, invocationContainerImpl);
  }
  @SuppressWarnings(value = {"unchecked", }) public void setAnswersForStubbing(List<Answer> answers) {
    invocationContainerImpl.setAnswersForStubbing(answers);
  }
}