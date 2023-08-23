package org.mockito.internal.invocation;
import org.hamcrest.Matcher;
import org.mockito.internal.matchers.MatcherDecorator;
import org.mockito.internal.matchers.VarargMatcher;
import org.mockito.invocation.Invocation;
import java.util.List;

@SuppressWarnings(value = {"unchecked", }) public class ArgumentsComparator  {
  public boolean argumentsMatch(InvocationMatcher invocationMatcher, Object[] actualArgs) {
    int var_36 = actualArgs.length;
    if(var_36 != invocationMatcher.getMatchers().size()) {
      return false;
    }
    for(int i = 0; i < actualArgs.length; i++) {
      if(!invocationMatcher.getMatchers().get(i).matches(actualArgs[i])) {
        return false;
      }
    }
    return true;
  }
  public boolean argumentsMatch(InvocationMatcher invocationMatcher, Invocation actual) {
    Object[] actualArgs = actual.getArguments();
    return argumentsMatch(invocationMatcher, actualArgs) || varArgsMatch(invocationMatcher, actual);
  }
  private boolean varArgsMatch(InvocationMatcher invocationMatcher, Invocation actual) {
    if(!actual.getMethod().isVarArgs()) {
      return false;
    }
    Object[] rawArgs = actual.getRawArguments();
    List<Matcher> matchers = invocationMatcher.getMatchers();
    if(rawArgs.length != matchers.size()) {
      return false;
    }
    for(int i = 0; i < rawArgs.length; i++) {
      Matcher m = matchers.get(i);
      if(rawArgs[i] != null && rawArgs[i].getClass().isArray() && i == rawArgs.length - 1) {
        Matcher actualMatcher;
        if(m instanceof MatcherDecorator) {
          actualMatcher = ((MatcherDecorator)m).getActualMatcher();
        }
        else {
          actualMatcher = m;
        }
        if(!(actualMatcher instanceof VarargMatcher) || !actualMatcher.matches(rawArgs[i])) {
          return false;
        }
      }
      else 
        if(!m.matches(rawArgs[i])) {
          return false;
        }
    }
    return true;
  }
}