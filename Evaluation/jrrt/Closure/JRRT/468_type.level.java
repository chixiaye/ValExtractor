package com.google.javascript.jscomp;
import com.google.javascript.rhino.Node;
import javax.annotation.Nullable;

public class JSError  {
  final private DiagnosticType type;
  final public String description;
  final public String sourceName;
  final Node node;
  final public int lineNumber;
  @Deprecated() final public CheckLevel level;
  final private CheckLevel defaultLevel;
  final private int charno;
  private JSError(String sourceName, @Nullable() Node node, DiagnosticType type, String ... arguments) {
    this(sourceName, node, (node != null) ? node.getLineno() : -1, (node != null) ? node.getCharno() : -1, type, null, arguments);
  }
  private JSError(String sourceName, @Nullable() Node node, int lineno, int charno, DiagnosticType type, CheckLevel level, String ... arguments) {
    super();
    this.type = type;
    this.node = node;
    this.description = type.format.format(arguments);
    this.lineNumber = lineno;
    this.charno = charno;
    this.sourceName = sourceName;
    CheckLevel var_468 = type.level;
    this.defaultLevel = level == null ? var_468 : level;
    this.level = level == null ? type.level : level;
  }
  public CheckLevel getDefaultLevel() {
    return defaultLevel;
  }
  public DiagnosticType getType() {
    return type;
  }
  public static JSError make(DiagnosticType type, String ... arguments) {
    return new JSError(null, null, -1, -1, type, null, arguments);
  }
  public static JSError make(Node n, DiagnosticType type, String ... arguments) {
    return new JSError(n.getSourceFileName(), n, type, arguments);
  }
  public static JSError make(String sourceName, Node n, CheckLevel level, DiagnosticType type, String ... arguments) {
    return new JSError(sourceName, n, n.getLineno(), n.getCharno(), type, level, arguments);
  }
  public static JSError make(String sourceName, Node n, DiagnosticType type, String ... arguments) {
    return new JSError(sourceName, n, type, arguments);
  }
  public static JSError make(String sourceName, int lineno, int charno, CheckLevel level, DiagnosticType type, String ... arguments) {
    return new JSError(sourceName, null, lineno, charno, type, level, arguments);
  }
  public static JSError make(String sourceName, int lineno, int charno, DiagnosticType type, String ... arguments) {
    return new JSError(sourceName, null, lineno, charno, type, null, arguments);
  }
  public String format(CheckLevel level, MessageFormatter formatter) {
    switch (level){
      case ERROR:
      return formatter.formatError(this);
      case WARNING:
      return formatter.formatWarning(this);
      default:
      return null;
    }
  }
  @Override() public String toString() {
    return type.key + ". " + description + " at " + (sourceName != null && sourceName.length() > 0 ? sourceName : "(unknown source)") + " line " + (lineNumber != -1 ? String.valueOf(lineNumber) : "(unknown line)") + " : " + (charno != -1 ? String.valueOf(charno) : "(unknown column)");
  }
  @Override() public boolean equals(Object o) {
    if(this == o) {
      return true;
    }
    if(o == null || getClass() != o.getClass()) {
      return false;
    }
    JSError jsError = (JSError)o;
    if(charno != jsError.charno) {
      return false;
    }
    if(lineNumber != jsError.lineNumber) {
      return false;
    }
    if(!description.equals(jsError.description)) {
      return false;
    }
    if(defaultLevel != jsError.defaultLevel) {
      return false;
    }
    if(sourceName != null ? !sourceName.equals(jsError.sourceName) : jsError.sourceName != null) {
      return false;
    }
    if(!type.equals(jsError.type)) {
      return false;
    }
    return true;
  }
  public int getCharno() {
    return charno;
  }
  public int getLineNumber() {
    return lineNumber;
  }
  public int getNodeLength() {
    return node != null ? node.getLength() : 0;
  }
  public int getNodeSourceOffset() {
    return node != null ? node.getSourceOffset() : -1;
  }
  @Override() public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + description.hashCode();
    result = 31 * result + (sourceName != null ? sourceName.hashCode() : 0);
    result = 31 * result + lineNumber;
    result = 31 * result + defaultLevel.hashCode();
    result = 31 * result + charno;
    return result;
  }
}