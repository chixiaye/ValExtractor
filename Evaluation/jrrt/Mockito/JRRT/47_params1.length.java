package org.mockito.internal.invocation;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.hamcrest.Matcher;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.internal.matchers.MatcherDecorator;
import org.mockito.internal.matchers.VarargMatcher;
import org.mockito.internal.reporting.PrintSettings;
import org.mockito.invocation.DescribedInvocation;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.Location;

@SuppressWarnings(value = {"unchecked", }) public class InvocationMatcher implements DescribedInvocation, CapturesArgumensFromInvocation, Serializable  {
  final private static long serialVersionUID = -3047126096857467610L;
  final private Invocation invocation;
  final private List<Matcher> matchers;
  public InvocationMatcher(Invocation invocation) {
    this(invocation, Collections.<Matcher>emptyList());
  }
  public InvocationMatcher(Invocation invocation, List<Matcher> matchers) {
    super();
    this.invocation = invocation;
    if(matchers.isEmpty()) {
      this.matchers = ArgumentsProcessor.argumentsToMatchers(invocation.getArguments());
    }
    else {
      this.matchers = matchers;
    }
  }
  public Invocation getInvocation() {
    return this.invocation;
  }
  public static List<InvocationMatcher> createFrom(List<Invocation> invocations) {
    LinkedList<InvocationMatcher> out = new LinkedList<InvocationMatcher>();
    for (Invocation i : invocations) {
      out.add(new InvocationMatcher(i));
    }
    return out;
  }
  public List<Matcher> getMatchers() {
    return this.matchers;
  }
  public Location getLocation() {
    return invocation.getLocation();
  }
  public Method getMethod() {
    return invocation.getMethod();
  }
  public String toString() {
    return new PrintSettings().print(matchers, invocation);
  }
  public boolean hasSameMethod(Invocation candidate) {
    Method m1 = invocation.getMethod();
    Method m2 = candidate.getMethod();
    if(m1.getName() != null && m1.getName().equals(m2.getName())) {
      Class[] params1 = m1.getParameterTypes();
      Class[] params2 = m2.getParameterTypes();
      int var_47 = params1.length;
      if(var_47 == params2.length) {
        for(int i = 0; i < params1.length; i++) {
          if(params1[i] != params2[i]) 
            return false;
        }
        return true;
      }
    }
    return false;
  }
  public boolean hasSimilarMethod(Invocation candidate) {
    String wantedMethodName = getMethod().getName();
    String currentMethodName = candidate.getMethod().getName();
    final boolean methodNameEquals = wantedMethodName.equals(currentMethodName);
    final boolean isUnverified = !candidate.isVerified();
    final boolean mockIsTheSame = getInvocation().getMock() == candidate.getMock();
    final boolean methodEquals = hasSameMethod(candidate);
    if(!methodNameEquals || !isUnverified || !mockIsTheSame) {
      return false;
    }
    final boolean overloadedButSameArgs = !methodEquals && safelyArgumentsMatch(candidate.getArguments());
    return !overloadedButSameArgs;
  }
  private boolean isVarargMatcher(Matcher matcher) {
    Matcher actualMatcher = matcher;
    if(actualMatcher instanceof MatcherDecorator) {
      actualMatcher = ((MatcherDecorator)actualMatcher).getActualMatcher();
    }
    return actualMatcher instanceof VarargMatcher;
  }
  private boolean isVariableArgument(Invocation invocation, int position) {
    return invocation.getRawArguments().length - 1 == position && invocation.getRawArguments()[position] != null && invocation.getRawArguments()[position].getClass().isArray() && invocation.getMethod().isVarArgs();
  }
  public boolean matches(Invocation actual) {
    return invocation.getMock().equals(actual.getMock()) && hasSameMethod(actual) && new ArgumentsComparator().argumentsMatch(this, actual);
  }
  private boolean safelyArgumentsMatch(Object[] actualArgs) {
    try {
      return new ArgumentsComparator().argumentsMatch(this, actualArgs);
    }
    catch (Throwable t) {
      return false;
    }
  }
  public void captureArgumentsFrom(Invocation invocation) {
    if(invocation.getMethod().isVarArgs()) {
      int indexOfVararg = invocation.getRawArguments().length - 1;
      for(int position = 0; position < indexOfVararg; position++) {
        Matcher m = matchers.get(position);
        if(m instanceof CapturesArguments) {
          ((CapturesArguments)m).captureFrom(invocation.getArgumentAt(position, Object.class));
        }
      }
      for(int position = indexOfVararg; position < matchers.size(); position++) {
        Matcher m = matchers.get(position);
        if(m instanceof CapturesArguments) {
          ((CapturesArguments)m).captureFrom(invocation.getRawArguments()[position - indexOfVararg]);
        }
      }
    }
    else {
      for(int position = 0; position < matchers.size(); position++) {
        Matcher m = matchers.get(position);
        if(m instanceof CapturesArguments) {
          ((CapturesArguments)m).captureFrom(invocation.getArgumentAt(position, Object.class));
        }
      }
    }
  }
}