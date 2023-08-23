package org.mockito.internal.debugging;
import org.mockito.MockitoDebugger;
import org.mockito.internal.invocation.UnusedStubsFinder;
import org.mockito.internal.invocation.finder.AllInvocationsFinder;
import org.mockito.invocation.Invocation;
import java.util.List;
import static java.util.Arrays.asList;

public class MockitoDebuggerImpl implements MockitoDebugger  {
  final private AllInvocationsFinder allInvocationsFinder = new AllInvocationsFinder();
  final private UnusedStubsFinder unusedStubsFinder = new UnusedStubsFinder();
  private String line(String text) {
    return text + "\n";
  }
  private String print(String out) {
    System.out.println(out);
    return out;
  }
  public String printInvocations(Object ... mocks) {
    String out = "";
    List<Invocation> invocations = allInvocationsFinder.find(asList(mocks));
    out += line("********************************");
    out += line("*** Mockito interactions log ***");
    out += line("********************************");
    for (Invocation i : invocations) {
      out += line(i.toString());
      out += line(" invoked: " + i.getLocation());
      if(i.stubInfo() != null) {
        out += line(" stubbed: " + i.stubInfo().stubbedAt().toString());
      }
    }
    invocations = unusedStubsFinder.find(asList(mocks));
    if(invocations.isEmpty()) {
      String var_28 = print(out);
      return var_28;
    }
    out += line("********************************");
    out += line("***       Unused stubs       ***");
    out += line("********************************");
    for (Invocation i : invocations) {
      out += line(i.toString());
      out += line(" stubbed: " + i.getLocation());
    }
    return print(out);
  }
}