package com.google.javascript.jscomp.deps;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;

final public class PathUtil  {
  final private static CharMatcher SLASH_MATCHER = CharMatcher.is('/');
  final private static CharMatcher NON_SLASH_MATCHER = CharMatcher.isNot('/');
  private PathUtil() {
    super();
  }
  public static String collapseDots(String path) {
    path = removeExtraneousSlashes(path);
    if(!path.contains(".")) {
      return path;
    }
    String[] srcFragments = path.split("/");
    List<String> dstFragments = Lists.newArrayList();
    for (String fragment : srcFragments) {
      if(fragment.equals("..")) {
        if(!dstFragments.isEmpty()) {
          dstFragments.remove(dstFragments.size() - 1);
        }
      }
      else 
        if(!fragment.equals(".")) {
          dstFragments.add(fragment);
        }
    }
    if(dstFragments.size() == 1 && dstFragments.get(0).isEmpty()) {
      return "/";
    }
    return Joiner.on("/").join(dstFragments);
  }
  public static String makeAbsolute(String path) {
    return makeAbsolute(path, System.getProperty("user.dir"));
  }
  public static String makeAbsolute(String path, String rootPath) {
    if(!isAbsolute(path)) {
      path = rootPath + "/" + path;
    }
    return collapseDots(path);
  }
  public static String makeRelative(String basePath, String targetPath) {
    if(isAbsolute(basePath) != isAbsolute(targetPath)) {
      throw new IllegalArgumentException("Paths must both be relative or both absolute.\n" + "  basePath: " + basePath + "\n" + "  targetPath: " + targetPath);
    }
    basePath = collapseDots(basePath);
    targetPath = collapseDots(targetPath);
    String[] baseFragments = basePath.split("/");
    String[] targetFragments = targetPath.split("/");
    int i = -1;
    do {
      i += 1;
      if(i == baseFragments.length && i == targetFragments.length) {
        return ".";
      }
      else 
        if(i == baseFragments.length) {
          return Joiner.on("/").join(Lists.newArrayList(Arrays.asList(targetFragments).listIterator(i)));
        }
        else 
          if(i == targetFragments.length) {
            int var_2148 = baseFragments.length;
            return Strings.repeat("../", var_2148 - i - 1) + "..";
          }
    }while(baseFragments[i].equals(targetFragments[i]));
    return Strings.repeat("../", baseFragments.length - i) + Joiner.on("/").join(Lists.newArrayList(Arrays.asList(targetFragments).listIterator(i)));
  }
  static String removeExtraneousSlashes(String s) {
    int lastNonSlash = NON_SLASH_MATCHER.lastIndexIn(s);
    if(lastNonSlash != -1) {
      s = s.substring(0, lastNonSlash + 1);
    }
    return SLASH_MATCHER.collapseFrom(s, '/');
  }
  static boolean isAbsolute(String path) {
    return path.startsWith("/");
  }
}