package org.mockito.internal.verification;
import java.util.List;
import org.mockito.exceptions.Reporter;
import org.mockito.internal.invocation.InvocationMarker;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.invocation.InvocationsFinder;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.invocation.Invocation;
import org.mockito.verification.VerificationMode;

public class Only implements VerificationMode  {
  final private InvocationsFinder finder = new InvocationsFinder();
  final private InvocationMarker marker = new InvocationMarker();
  final private Reporter reporter = new Reporter();
  @SuppressWarnings(value = {"unchecked", }) public void verify(VerificationData data) {
    InvocationMatcher wantedMatcher = data.getWanted();
    List<Invocation> invocations = data.getAllInvocations();
    List<Invocation> chunk = finder.findInvocations(invocations, wantedMatcher);
    int var_133 = invocations.size();
    if(var_133 != 1 && chunk.size() > 0) {
      Invocation unverified = finder.findFirstUnverified(invocations);
      reporter.noMoreInteractionsWanted(unverified, (List)invocations);
    }
    else 
      if(invocations.size() != 1 || chunk.size() == 0) {
        reporter.wantedButNotInvoked(wantedMatcher);
      }
    marker.markVerified(chunk.get(0), wantedMatcher);
  }
}