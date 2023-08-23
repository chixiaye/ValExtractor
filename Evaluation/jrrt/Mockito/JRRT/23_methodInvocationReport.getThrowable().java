package org.mockito.internal.debugging;
import java.io.PrintStream;
import org.mockito.invocation.DescribedInvocation;
import org.mockito.listeners.InvocationListener;
import org.mockito.listeners.MethodInvocationReport;

public class VerboseMockInvocationLogger implements InvocationListener  {
  final PrintStream printStream;
  private int mockInvocationsCounter = 0;
  public VerboseMockInvocationLogger() {
    this(System.out);
  }
  public VerboseMockInvocationLogger(PrintStream printStream) {
    super();
    this.printStream = printStream;
  }
  private void printFooter() {
    printStream.println("");
  }
  private void printHeader() {
    mockInvocationsCounter++;
    printStream.println("############ Logging method invocation #" + mockInvocationsCounter + " on mock/spy ########");
  }
  private void printInvocation(DescribedInvocation invocation) {
    printStream.println(invocation.toString());
    printlnIndented("invoked: " + invocation.getLocation().toString());
  }
  private void printReturnedValueOrThrowable(MethodInvocationReport methodInvocationReport) {
    if(methodInvocationReport.threwException()) {
      Throwable var_23 = methodInvocationReport.getThrowable();
      String message = var_23.getMessage() == null ? "" : " with message " + methodInvocationReport.getThrowable().getMessage();
      printlnIndented("has thrown: " + methodInvocationReport.getThrowable().getClass() + message);
    }
    else {
      String type = (methodInvocationReport.getReturnedValue() == null) ? "" : " (" + methodInvocationReport.getReturnedValue().getClass().getName() + ")";
      printlnIndented("has returned: \"" + methodInvocationReport.getReturnedValue() + "\"" + type);
    }
  }
  private void printStubInfo(MethodInvocationReport methodInvocationReport) {
    if(methodInvocationReport.getLocationOfStubbing() != null) {
      printlnIndented("stubbed: " + methodInvocationReport.getLocationOfStubbing());
    }
  }
  private void printlnIndented(String message) {
    printStream.println("   " + message);
  }
  public void reportInvocation(MethodInvocationReport methodInvocationReport) {
    printHeader();
    printStubInfo(methodInvocationReport);
    printInvocation(methodInvocationReport.getInvocation());
    printReturnedValueOrThrowable(methodInvocationReport);
    printFooter();
  }
}