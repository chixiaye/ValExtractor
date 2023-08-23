package org.mockito.internal.verification;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.internal.util.Timer;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.verification.VerificationMode;

public class VerificationOverTimeImpl implements VerificationMode  {
  final private long pollingPeriodMillis;
  final private VerificationMode delegate;
  final private boolean returnOnSuccess;
  final private Timer timer;
  public VerificationOverTimeImpl(long pollingPeriodMillis, VerificationMode delegate, boolean returnOnSuccess, Timer timer) {
    super();
    this.pollingPeriodMillis = pollingPeriodMillis;
    this.delegate = delegate;
    this.returnOnSuccess = returnOnSuccess;
    this.timer = timer;
  }
  public VerificationOverTimeImpl(long pollingPeriodMillis, long durationMillis, VerificationMode delegate, boolean returnOnSuccess) {
    this(pollingPeriodMillis, delegate, returnOnSuccess, new Timer(durationMillis));
  }
  private AssertionError handleVerifyException(AssertionError e) {
    if(canRecoverFromFailure(delegate)) {
      sleep(pollingPeriodMillis);
      return e;
    }
    else {
      throw e;
    }
  }
  public VerificationOverTimeImpl copyWithVerificationMode(VerificationMode verificationMode) {
    return new VerificationOverTimeImpl(pollingPeriodMillis, timer.duration(), verificationMode, returnOnSuccess);
  }
  protected boolean canRecoverFromFailure(VerificationMode verificationMode) {
    return !(verificationMode instanceof AtMost || verificationMode instanceof NoMoreInteractions);
  }
  private void sleep(long sleep) {
    try {
      Thread.sleep(sleep);
    }
    catch (InterruptedException ie) {
      throw new RuntimeException("Thread sleep has been interrupted", ie);
    }
  }
  public void verify(VerificationData data) {
    AssertionError error = null;
    timer.start();
    while(timer.isCounting()){
      try {
        delegate.verify(data);
        if(returnOnSuccess) {
          return ;
        }
        else {
          error = null;
        }
      }
      catch (MockitoAssertionError e) {
        AssertionError var_135 = handleVerifyException(e);
        error = var_135;
      }
      catch (AssertionError e) {
        error = handleVerifyException(e);
      }
    }
    if(error != null) {
      throw error;
    }
  }
}