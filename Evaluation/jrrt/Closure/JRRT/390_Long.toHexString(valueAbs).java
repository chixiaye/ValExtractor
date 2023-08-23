package com.google.javascript.jscomp;
import com.google.javascript.rhino.Node;

abstract class CodeConsumer  {
  boolean statementNeedsEnded = false;
  boolean statementStarted = false;
  boolean sawFunction = false;
  boolean breakAfterBlockFor(Node n, boolean statementContext) {
    return statementContext;
  }
  boolean continueProcessing() {
    return true;
  }
  static boolean isNegativeZero(double x) {
    return x == 0.0D && Math.copySign(1, x) == -1.0D;
  }
  static boolean isWordChar(char ch) {
    return (ch == '_' || ch == '$' || Character.isLetterOrDigit(ch));
  }
  boolean shouldPreserveExtraBlocks() {
    return false;
  }
  abstract char getLastChar();
  void add(String newcode) {
    maybeEndStatement();
    if(newcode.length() == 0) {
      return ;
    }
    char c = newcode.charAt(0);
    if((isWordChar(c) || c == '\\') && isWordChar(getLastChar())) {
      append(" ");
    }
    else 
      if(c == '/' && getLastChar() == '/') {
        append(" ");
      }
    append(newcode);
  }
  void addConstant(String newcode) {
    add(newcode);
  }
  void addIdentifier(String identifier) {
    add(identifier);
  }
  void addNumber(double x) {
    char prev = getLastChar();
    boolean negativeZero = isNegativeZero(x);
    if((x < 0 || negativeZero) && prev == '-') {
      add(" ");
    }
    if(negativeZero) {
      addConstant("-0");
    }
    else 
      if((long)x == x) {
        long value = (long)x;
        long mantissa = value;
        int exp = 0;
        if(Math.abs(x) >= 100) {
          while(mantissa / 10 * Math.pow(10, exp + 1) == value){
            mantissa /= 10;
            exp++;
          }
        }
        if(exp > 2) {
          addConstant(Long.toString(mantissa) + "E" + Integer.toString(exp));
        }
        else {
          long valueAbs = Math.abs(value);
          String var_390 = Long.toHexString(valueAbs);
          if(var_390.length() + 2 < Long.toString(valueAbs).length()) {
            addConstant((value < 0 ? "-" : "") + "0x" + Long.toHexString(valueAbs));
          }
          else {
            addConstant(Long.toString(value));
          }
        }
      }
      else {
        addConstant(String.valueOf(x).replace(".0E", "E"));
      }
  }
  void addOp(String op, boolean binOp) {
    maybeEndStatement();
    char first = op.charAt(0);
    char prev = getLastChar();
    if((first == '+' || first == '-') && prev == first) {
      append(" ");
    }
    else 
      if(Character.isLetter(first) && isWordChar(prev)) {
        append(" ");
      }
      else 
        if(prev == '-' && first == '>') {
          append(" ");
        }
    appendOp(op, binOp);
    if(binOp) {
      maybeCutLine();
    }
  }
  abstract void append(String str);
  void appendBlockEnd() {
    append("}");
  }
  void appendBlockStart() {
    append("{");
  }
  void appendOp(String op, boolean binOp) {
    append(op);
  }
  void beginBlock() {
    if(statementNeedsEnded) {
      append(";");
      maybeLineBreak();
    }
    appendBlockStart();
    endLine();
    statementNeedsEnded = false;
  }
  void beginCaseBody() {
    append(":");
  }
  void endBlock() {
    endBlock(false);
  }
  void endBlock(boolean shouldEndLine) {
    appendBlockEnd();
    if(shouldEndLine) {
      endLine();
    }
    statementNeedsEnded = false;
  }
  void endCaseBody() {
  }
  void endFile() {
  }
  void endFunction() {
    endFunction(false);
  }
  void endFunction(boolean statementContext) {
    sawFunction = true;
    if(statementContext) {
      endLine();
    }
  }
  void endLine() {
  }
  void endSourceMapping(Node node) {
  }
  void endStatement() {
    endStatement(false);
  }
  void endStatement(boolean needSemiColon) {
    if(needSemiColon) {
      append(";");
      maybeLineBreak();
      statementNeedsEnded = false;
    }
    else 
      if(statementStarted) {
        statementNeedsEnded = true;
      }
  }
  void listSeparator() {
    add(",");
    maybeLineBreak();
  }
  void maybeCutLine() {
  }
  void maybeEndStatement() {
    if(statementNeedsEnded) {
      append(";");
      maybeLineBreak();
      endLine();
      statementNeedsEnded = false;
    }
    statementStarted = true;
  }
  void maybeLineBreak() {
    maybeCutLine();
  }
  void notePreferredLineBreak() {
  }
  void startNewLine() {
  }
  void startSourceMapping(Node node) {
  }
}