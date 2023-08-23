package org.mockito.internal.progress;
import org.mockito.internal.listeners.MockingProgressListener;
import org.mockito.invocation.Invocation;
import org.mockito.verification.VerificationMode;
import java.io.Serializable;

@SuppressWarnings(value = {"unchecked", }) public class ThreadSafeMockingProgress implements MockingProgress, Serializable  {
  final private static long serialVersionUID = 6839454041642082618L;
  final private static ThreadLocal<MockingProgress> mockingProgress = new ThreadLocal<MockingProgress>();
  public ArgumentMatcherStorage getArgumentMatcherStorage() {
    return threadSafely().getArgumentMatcherStorage();
  }
  public IOngoingStubbing pullOngoingStubbing() {
    return threadSafely().pullOngoingStubbing();
  }
  static MockingProgress threadSafely() {
    MockingProgress var_80 = mockingProgress.get();
    if(var_80 == null) {
      mockingProgress.set(new MockingProgressImpl());
    }
    return mockingProgress.get();
  }
  public String toString() {
    return threadSafely().toString();
  }
  public VerificationMode pullVerificationMode() {
    return threadSafely().pullVerificationMode();
  }
  public void mockingStarted(Object mock, Class classToMock) {
    threadSafely().mockingStarted(mock, classToMock);
  }
  public void reportOngoingStubbing(IOngoingStubbing iOngoingStubbing) {
    threadSafely().reportOngoingStubbing(iOngoingStubbing);
  }
  public void reset() {
    threadSafely().reset();
  }
  public void resetOngoingStubbing() {
    threadSafely().resetOngoingStubbing();
  }
  public void setListener(MockingProgressListener listener) {
    threadSafely().setListener(listener);
  }
  public void stubbingCompleted(Invocation invocation) {
    threadSafely().stubbingCompleted(invocation);
  }
  public void stubbingStarted() {
    threadSafely().stubbingStarted();
  }
  public void validateState() {
    threadSafely().validateState();
  }
  public void verificationStarted(VerificationMode verify) {
    threadSafely().verificationStarted(verify);
  }
}