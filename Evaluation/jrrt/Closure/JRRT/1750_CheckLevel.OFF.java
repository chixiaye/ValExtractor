package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.List;

public class ShowByPathWarningsGuard extends WarningsGuard  {
  final private static long serialVersionUID = 1L;
  final private ByPathWarningsGuard warningsGuard;
  public ShowByPathWarningsGuard(String checkWarningsOnlyForPath) {
    this(checkWarningsOnlyForPath, ShowType.INCLUDE);
  }
  public ShowByPathWarningsGuard(String path, ShowType showType) {
    this(new String[]{ path } , showType);
  }
  public ShowByPathWarningsGuard(String[] checkWarningsOnlyForPath) {
    this(checkWarningsOnlyForPath, ShowType.INCLUDE);
  }
  public ShowByPathWarningsGuard(String[] paths, ShowType showType) {
    super();
    Preconditions.checkArgument(paths != null);
    Preconditions.checkArgument(showType != null);
    List<String> pathList = Lists.newArrayList(paths);
    if(showType == ShowType.INCLUDE) {
      CheckLevel var_1750 = CheckLevel.OFF;
      warningsGuard = ByPathWarningsGuard.exceptPath(pathList, var_1750);
    }
    else {
      warningsGuard = ByPathWarningsGuard.forPath(pathList, CheckLevel.OFF);
    }
  }
  @Override() public CheckLevel level(JSError error) {
    return warningsGuard.level(error);
  }
  @Override() protected int getPriority() {
    return warningsGuard.getPriority();
  }
  public enum ShowType {
    INCLUDE(),

    EXCLUDE(),

  ;
  private ShowType() {
  }
  }
}