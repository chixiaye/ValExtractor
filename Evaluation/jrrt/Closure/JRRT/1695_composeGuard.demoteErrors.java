package com.google.javascript.jscomp;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.javascript.jscomp.CheckLevel;
import java.io.Serializable;
import java.util.*;
import java.util.Map;
import java.util.TreeSet;

public class ComposeWarningsGuard extends WarningsGuard  {
  final private static long serialVersionUID = 1L;
  final private Map<WarningsGuard, Integer> orderOfAddition = Maps.newHashMap();
  private int numberOfAdds = 0;
  final private Comparator<WarningsGuard> guardComparator = new GuardComparator(orderOfAddition);
  private boolean demoteErrors = false;
  final private TreeSet<WarningsGuard> guards = new TreeSet<WarningsGuard>(guardComparator);
  public ComposeWarningsGuard(List<WarningsGuard> guards) {
    super();
    addGuards(guards);
  }
  public ComposeWarningsGuard(WarningsGuard ... guards) {
    this(Lists.newArrayList(guards));
  }
  @Override() public CheckLevel level(JSError error) {
    for (WarningsGuard guard : guards) {
      CheckLevel newLevel = guard.level(error);
      if(newLevel != null) {
        if(demoteErrors && newLevel == CheckLevel.ERROR) {
          return CheckLevel.WARNING;
        }
        return newLevel;
      }
    }
    return null;
  }
  ComposeWarningsGuard makeEmergencyFailSafeGuard() {
    ComposeWarningsGuard safeGuard = new ComposeWarningsGuard();
    safeGuard.demoteErrors = true;
    for (WarningsGuard guard : guards.descendingSet()) {
      safeGuard.addGuard(guard);
    }
    return safeGuard;
  }
  List<WarningsGuard> getGuards() {
    return Collections.unmodifiableList(Lists.newArrayList(guards));
  }
  @Override() public String toString() {
    return Joiner.on(", ").join(guards);
  }
  @Override() public boolean disables(DiagnosticGroup group) {
    nextSingleton:
      for (DiagnosticType type : group.getTypes()) {
        DiagnosticGroup singleton = DiagnosticGroup.forType(type);
        for (WarningsGuard guard : guards) {
          if(guard.disables(singleton)) {
            continue nextSingleton;
          }
          else 
            if(guard.enables(singleton)) {
              return false;
            }
        }
        return false;
      }
    return true;
  }
  @Override() public boolean enables(DiagnosticGroup group) {
    for (WarningsGuard guard : guards) {
      if(guard.enables(group)) {
        return true;
      }
      else 
        if(guard.disables(group)) {
          return false;
        }
    }
    return false;
  }
  void addGuard(WarningsGuard guard) {
    if(guard instanceof ComposeWarningsGuard) {
      ComposeWarningsGuard composeGuard = (ComposeWarningsGuard)guard;
      boolean var_1695 = composeGuard.demoteErrors;
      if(var_1695) {
        this.demoteErrors = composeGuard.demoteErrors;
      }
      addGuards(Lists.newArrayList(composeGuard.guards.descendingSet()));
    }
    else {
      numberOfAdds++;
      orderOfAddition.put(guard, numberOfAdds);
      guards.remove(guard);
      guards.add(guard);
    }
  }
  private void addGuards(Iterable<WarningsGuard> guards) {
    for (WarningsGuard guard : guards) {
      addGuard(guard);
    }
  }
  
  private static class GuardComparator implements Comparator<WarningsGuard>, Serializable  {
    final private static long serialVersionUID = 1L;
    final private Map<WarningsGuard, Integer> orderOfAddition;
    private GuardComparator(Map<WarningsGuard, Integer> orderOfAddition) {
      super();
      this.orderOfAddition = orderOfAddition;
    }
    @Override() public int compare(WarningsGuard a, WarningsGuard b) {
      int priorityDiff = a.getPriority() - b.getPriority();
      if(priorityDiff != 0) {
        return priorityDiff;
      }
      return orderOfAddition.get(b).intValue() - orderOfAddition.get(a).intValue();
    }
  }
}