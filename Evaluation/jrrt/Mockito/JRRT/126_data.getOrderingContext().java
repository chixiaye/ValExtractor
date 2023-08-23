package org.mockito.internal.verification;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.internal.verification.api.VerificationDataInOrder;
import org.mockito.internal.verification.api.VerificationInOrderMode;
import org.mockito.internal.verification.checkers.*;
import org.mockito.invocation.Invocation;
import org.mockito.verification.VerificationMode;
import java.util.List;

public class Calls implements VerificationMode, VerificationInOrderMode  {
  final int wantedCount;
  public Calls(int wantedNumberOfInvocations) {
    super();
    if(wantedNumberOfInvocations <= 0) {
      throw new MockitoException("Negative and zero values are not allowed here");
    }
    this.wantedCount = wantedNumberOfInvocations;
  }
  @Override() public String toString() {
    return "Wanted invocations count (non-greedy): " + wantedCount;
  }
  public void verify(VerificationData data) {
    throw new MockitoException("calls is only intended to work with InOrder");
  }
  public void verifyInOrder(VerificationDataInOrder data) {
    List<Invocation> allInvocations = data.getAllInvocations();
    InvocationMatcher wanted = data.getWanted();
    MissingInvocationInOrderChecker missingInvocation = new MissingInvocationInOrderChecker();
    org.mockito.internal.verification.api.InOrderContext var_126 = data.getOrderingContext();
    missingInvocation.check(allInvocations, wanted, this, var_126);
    NonGreedyNumberOfInvocationsInOrderChecker numberOfCalls = new NonGreedyNumberOfInvocationsInOrderChecker();
    numberOfCalls.check(allInvocations, wanted, wantedCount, data.getOrderingContext());
  }
}