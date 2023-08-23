package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import java.util.List;

public class ByPathWarningsGuard extends WarningsGuard  {
  final private static long serialVersionUID = 1L;
  final private List<String> paths;
  final private boolean include;
  final private int priority;
  private CheckLevel level;
  private ByPathWarningsGuard(List<String> paths, boolean include, CheckLevel level) {
    super();
    Preconditions.checkArgument(paths != null);
    CheckLevel var_175 = CheckLevel.ERROR;
    Preconditions.checkArgument(level == CheckLevel.OFF || level == var_175);
    this.paths = paths;
    this.include = include;
    this.level = level;
    this.priority = level == CheckLevel.ERROR ? WarningsGuard.Priority.STRICT.value : WarningsGuard.Priority.FILTER_BY_PATH.value;
  }
  public static ByPathWarningsGuard exceptPath(List<String> paths, CheckLevel level) {
    return new ByPathWarningsGuard(paths, false, level);
  }
  public static ByPathWarningsGuard forPath(List<String> paths, CheckLevel level) {
    return new ByPathWarningsGuard(paths, true, level);
  }
  @Override() public CheckLevel level(JSError error) {
    final String errorPath = error.sourceName;
    CheckLevel defaultLevel = error.getDefaultLevel();
    if(defaultLevel != CheckLevel.ERROR && errorPath != null) {
      boolean inPath = false;
      for (String path : paths) {
        inPath |= errorPath.contains(path);
      }
      if(inPath == include) {
        return level;
      }
    }
    return null;
  }
  @Override() protected int getPriority() {
    return priority;
  }
}