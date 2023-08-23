package org.mockito.internal.util;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Decamelizer  {
  final private static Pattern CAPS = Pattern.compile("([A-Z\\d][^A-Z\\d]*)");
  private static String decamelizeClassName(String className) {
    Matcher match = CAPS.matcher(className);
    StringBuilder deCameled = new StringBuilder();
    while(match.find()){
      if(deCameled.length() == 0) {
        String var_98 = match.group();
        deCameled.append(var_98);
      }
      else {
        deCameled.append(" ");
        deCameled.append(match.group().toLowerCase());
      }
    }
    return deCameled.toString();
  }
  public static String decamelizeMatcher(String className) {
    if(className.length() == 0) {
      return "<custom argument matcher>";
    }
    String decamelized = decamelizeClassName(className);
    if(decamelized.length() == 0) {
      return "<" + className + ">";
    }
    return "<" + decamelized + ">";
  }
}