package com.google.javascript.jscomp;
import com.google.javascript.rhino.Node;
import java.util.regex.Pattern;

public class GoogleCodingConvention extends CodingConventions.Proxy  {
  final private static long serialVersionUID = 1L;
  final private static String OPTIONAL_ARG_PREFIX = "opt_";
  final private static String VAR_ARGS_NAME = "var_args";
  final private static Pattern ENUM_KEY_PATTERN = Pattern.compile("[A-Z0-9][A-Z0-9_]*");
  public GoogleCodingConvention() {
    this(new ClosureCodingConvention());
  }
  public GoogleCodingConvention(CodingConvention convention) {
    super(convention);
  }
  @Override() public boolean isConstant(String name) {
    int var_1325 = name.length();
    if(var_1325 <= 1) {
      return false;
    }
    int pos = name.lastIndexOf('$');
    if(pos >= 0) {
      name = name.substring(pos + 1);
      if(name.length() == 0) {
        return false;
      }
    }
    return isConstantKey(name);
  }
  @Override() public boolean isConstantKey(String name) {
    if(name.isEmpty() || !Character.isUpperCase(name.charAt(0))) {
      return false;
    }
    return name.toUpperCase().equals(name);
  }
  @Override() public boolean isExported(String name, boolean local) {
    return super.isExported(name, local) || (!local && name.startsWith("_"));
  }
  @Override() public boolean isOptionalParameter(Node parameter) {
    return parameter.getString().startsWith(OPTIONAL_ARG_PREFIX);
  }
  @Override() public boolean isPrivate(String name) {
    return name.endsWith("_") && !isExported(name);
  }
  @Override() public boolean isValidEnumKey(String key) {
    return ENUM_KEY_PATTERN.matcher(key).matches();
  }
  @Override() public boolean isVarArgsParameter(Node parameter) {
    return VAR_ARGS_NAME.equals(parameter.getString());
  }
}