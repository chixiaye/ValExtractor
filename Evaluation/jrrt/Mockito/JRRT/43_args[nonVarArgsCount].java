package org.mockito.internal.invocation;
import org.hamcrest.Matcher;
import org.mockito.internal.matchers.ArrayEquals;
import org.mockito.internal.matchers.Equals;
import org.mockito.internal.util.collections.ArrayUtils;
import java.util.ArrayList;
import java.util.List;

public class ArgumentsProcessor  {
  public static List<Matcher> argumentsToMatchers(Object[] arguments) {
    List<Matcher> matchers = new ArrayList<Matcher>(arguments.length);
    for (Object arg : arguments) {
      if(arg != null && arg.getClass().isArray()) {
        matchers.add(new ArrayEquals(arg));
      }
      else {
        matchers.add(new Equals(arg));
      }
    }
    return matchers;
  }
  public static Object[] expandVarArgs(final boolean isVarArgs, final Object[] args) {
    if(!isVarArgs || new ArrayUtils().isEmpty(args) || args[args.length - 1] != null && !args[args.length - 1].getClass().isArray()) {
      return args == null ? new Object[0] : args;
    }
    final int nonVarArgsCount = args.length - 1;
    Object[] varArgs;
    Object var_43 = args[nonVarArgsCount];
    if(var_43 == null) {
      varArgs = new Object[]{ null } ;
    }
    else {
      varArgs = ArrayEquals.createObjectArray(args[nonVarArgsCount]);
    }
    final int varArgsCount = varArgs.length;
    Object[] newArgs = new Object[nonVarArgsCount + varArgsCount];
    System.arraycopy(args, 0, newArgs, 0, nonVarArgsCount);
    System.arraycopy(varArgs, 0, newArgs, nonVarArgsCount, varArgsCount);
    return newArgs;
  }
}