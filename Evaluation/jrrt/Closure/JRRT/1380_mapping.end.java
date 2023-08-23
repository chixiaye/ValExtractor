package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.debugging.sourcemap.FilePosition;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

class CodePrinter  {
  final static int DEFAULT_LINE_LENGTH_THRESHOLD = 500;
  private static String toSource(Node root, Format outputFormat, CompilerOptions options, SourceMap sourceMap, boolean tagAsStrict) {
    Preconditions.checkState(options.sourceMapDetailLevel != null);
    boolean createSourceMap = (sourceMap != null);
    MappedCodePrinter mcp = outputFormat == Format.COMPACT ? new CompactCodePrinter(options.lineBreak, options.preferLineBreakAtEndOfFile, options.lineLengthThreshold, createSourceMap, options.sourceMapDetailLevel) : new PrettyCodePrinter(options.lineLengthThreshold, createSourceMap, options.sourceMapDetailLevel);
    CodeGenerator cg = outputFormat == Format.TYPED ? new TypedCodeGenerator(mcp, options) : new CodeGenerator(mcp, options);
    if(tagAsStrict) {
      cg.tagAsStrict();
    }
    cg.add(root);
    mcp.endFile();
    String code = mcp.getCode();
    if(createSourceMap) {
      mcp.generateSourceMap(sourceMap);
    }
    return code;
  }
  
  static class Builder  {
    final private Node root;
    private CompilerOptions options = new CompilerOptions();
    private boolean outputTypes = false;
    private SourceMap sourceMap = null;
    private boolean tagAsStrict;
    Builder(Node node) {
      super();
      root = node;
    }
    Builder setCompilerOptions(CompilerOptions options) {
      try {
        this.options = (CompilerOptions)options.clone();
      }
      catch (CloneNotSupportedException e) {
        throw Throwables.propagate(e);
      }
      return this;
    }
    Builder setLineBreak(boolean lineBreak) {
      options.lineBreak = lineBreak;
      return this;
    }
    Builder setOutputTypes(boolean outputTypes) {
      this.outputTypes = outputTypes;
      return this;
    }
    Builder setPrettyPrint(boolean prettyPrint) {
      options.prettyPrint = prettyPrint;
      return this;
    }
    Builder setSourceMap(SourceMap sourceMap) {
      this.sourceMap = sourceMap;
      return this;
    }
    Builder setTagAsStrict(boolean tagAsStrict) {
      this.tagAsStrict = tagAsStrict;
      return this;
    }
    String build() {
      if(root == null) {
        throw new IllegalStateException("Cannot build without root node being specified");
      }
      Format outputFormat = outputTypes ? Format.TYPED : options.prettyPrint ? Format.PRETTY : Format.COMPACT;
      return toSource(root, outputFormat, options, sourceMap, tagAsStrict);
    }
  }
  
  static class CompactCodePrinter extends MappedCodePrinter  {
    final private boolean lineBreak;
    final private boolean preferLineBreakAtEndOfFile;
    private int lineStartPosition = 0;
    private int preferredBreakPosition = 0;
    private int prevCutPosition = 0;
    private int prevLineStartPosition = 0;
    private CompactCodePrinter(boolean lineBreak, boolean preferLineBreakAtEndOfFile, int lineLengthThreshold, boolean createSrcMap, SourceMap.DetailLevel sourceMapDetailLevel) {
      super(lineLengthThreshold, createSrcMap, sourceMapDetailLevel);
      this.lineBreak = lineBreak;
      this.preferLineBreakAtEndOfFile = preferLineBreakAtEndOfFile;
    }
    @Override() void append(String str) {
      code.append(str);
      lineLength += str.length();
    }
    @Override() void endFile() {
      super.endFile();
      if(!preferLineBreakAtEndOfFile) {
        return ;
      }
      if(lineLength > lineLengthThreshold / 2) {
        append(";");
        startNewLine();
      }
      else 
        if(prevCutPosition > 0) {
          code.setCharAt(prevCutPosition, ' ');
          lineStartPosition = prevLineStartPosition;
          lineLength = code.length() - lineStartPosition;
          reportLineCut(lineIndex, prevCutPosition + 1, false);
          lineIndex--;
          prevCutPosition = 0;
          prevLineStartPosition = 0;
          append(";");
          startNewLine();
        }
        else {
        }
    }
    @Override() void maybeCutLine() {
      if(lineLength > lineLengthThreshold) {
        if(preferredBreakPosition > lineStartPosition && preferredBreakPosition < lineStartPosition + lineLength) {
          int position = preferredBreakPosition;
          code.insert(position, '\n');
          prevCutPosition = position;
          reportLineCut(lineIndex, position - lineStartPosition, true);
          lineIndex++;
          lineLength -= (position - lineStartPosition);
          lineStartPosition = position + 1;
        }
        else {
          startNewLine();
        }
      }
    }
    @Override() void maybeLineBreak() {
      if(lineBreak) {
        if(sawFunction) {
          startNewLine();
          sawFunction = false;
        }
      }
      int len = code.length();
      if(preferredBreakPosition == len - 1) {
        char ch = code.charAt(len - 1);
        if(ch == ';') {
          preferredBreakPosition = len;
        }
      }
      maybeCutLine();
    }
    @Override() void notePreferredLineBreak() {
      preferredBreakPosition = code.length();
    }
    @Override() void startNewLine() {
      if(lineLength > 0) {
        prevCutPosition = code.length();
        prevLineStartPosition = lineStartPosition;
        code.append('\n');
        lineLength = 0;
        lineIndex++;
        lineStartPosition = code.length();
      }
    }
  }
  enum Format {
    COMPACT(),

    PRETTY(),

    TYPED(),

  ;
  private Format() {
  }
  }
  
  abstract private static class MappedCodePrinter extends CodeConsumer  {
    final private Deque<Mapping> mappings;
    final private List<Mapping> allMappings;
    final private boolean createSrcMap;
    final private SourceMap.DetailLevel sourceMapDetailLevel;
    final protected StringBuilder code = new StringBuilder(1024);
    final protected int lineLengthThreshold;
    protected int lineLength = 0;
    protected int lineIndex = 0;
    MappedCodePrinter(int lineLengthThreshold, boolean createSrcMap, SourceMap.DetailLevel sourceMapDetailLevel) {
      super();
      Preconditions.checkState(sourceMapDetailLevel != null);
      this.lineLengthThreshold = lineLengthThreshold <= 0 ? Integer.MAX_VALUE : lineLengthThreshold;
      this.createSrcMap = createSrcMap;
      this.sourceMapDetailLevel = sourceMapDetailLevel;
      this.mappings = createSrcMap ? new ArrayDeque<Mapping>() : null;
      this.allMappings = createSrcMap ? new ArrayList<Mapping>() : null;
    }
    private FilePosition convertPosition(FilePosition position, int lineIndex, int characterPosition, boolean insertion) {
      int originalLine = position.getLine();
      int originalChar = position.getColumn();
      if(insertion) {
        if(originalLine == lineIndex && originalChar >= characterPosition) {
          return new FilePosition(originalLine + 1, originalChar - characterPosition);
        }
        else {
          return position;
        }
      }
      else {
        if(originalLine == lineIndex) {
          return new FilePosition(originalLine - 1, originalChar + characterPosition);
        }
        else 
          if(originalLine > lineIndex) {
            throw new IllegalStateException("Cannot undo line cut on a previous line.");
          }
          else {
            return position;
          }
      }
    }
    public String getCode() {
      return code.toString();
    }
    @Override() char getLastChar() {
      return (code.length() > 0) ? code.charAt(code.length() - 1) : '\u0000';
    }
    final protected int getCurrentCharIndex() {
      return lineLength;
    }
    final protected int getCurrentLineIndex() {
      return lineIndex;
    }
    @Override() void endSourceMapping(Node node) {
      if(createSrcMap && !mappings.isEmpty() && mappings.peek().node == node) {
        Mapping mapping = mappings.pop();
        int line = getCurrentLineIndex();
        int index = getCurrentCharIndex();
        Preconditions.checkState(line >= 0);
        mapping.end = new FilePosition(line, index);
      }
    }
    void generateSourceMap(SourceMap map) {
      if(createSrcMap) {
        for (Mapping mapping : allMappings) {
          map.addMapping(mapping.node, mapping.start, mapping.end);
        }
      }
    }
    void reportLineCut(int lineIndex, int charIndex, boolean insertion) {
      if(createSrcMap) {
        for (Mapping mapping : allMappings) {
          mapping.start = convertPosition(mapping.start, lineIndex, charIndex, insertion);
          FilePosition var_1380 = mapping.end;
          if(var_1380 != null) {
            mapping.end = convertPosition(mapping.end, lineIndex, charIndex, insertion);
          }
        }
      }
    }
    @Override() void startSourceMapping(Node node) {
      Preconditions.checkState(sourceMapDetailLevel != null);
      Preconditions.checkState(node != null);
      if(createSrcMap && node.getSourceFileName() != null && node.getLineno() > 0 && sourceMapDetailLevel.apply(node)) {
        int line = getCurrentLineIndex();
        int index = getCurrentCharIndex();
        Preconditions.checkState(line >= 0);
        Mapping mapping = new Mapping();
        mapping.node = node;
        mapping.start = new FilePosition(line, index);
        mappings.push(mapping);
        allMappings.add(mapping);
      }
    }
    
    private static class Mapping  {
      Node node;
      FilePosition start;
      FilePosition end;
    }
  }
  
  static class PrettyCodePrinter extends MappedCodePrinter  {
    final static String INDENT = "  ";
    private int indent = 0;
    private PrettyCodePrinter(int lineLengthThreshold, boolean createSourceMap, SourceMap.DetailLevel sourceMapDetailLevel) {
      super(lineLengthThreshold, createSourceMap, sourceMapDetailLevel);
    }
    private Node getTryForCatch(Node n) {
      return n.getParent().getParent();
    }
    @Override() boolean breakAfterBlockFor(Node n, boolean isStatementContext) {
      Preconditions.checkState(n.isBlock());
      Node parent = n.getParent();
      if(parent != null) {
        int type = parent.getType();
        switch (type){
          case Token.DO:
          return false;
          case Token.FUNCTION:
          return false;
          case Token.TRY:
          return n != parent.getFirstChild();
          case Token.CATCH:
          return !NodeUtil.hasFinally(getTryForCatch(parent));
          case Token.IF:
          return n == parent.getLastChild();
        }
      }
      return true;
    }
    @Override() boolean shouldPreserveExtraBlocks() {
      return true;
    }
    @Override() void append(String str) {
      if(lineLength == 0) {
        for(int i = 0; i < indent; i++) {
          code.append(INDENT);
          lineLength += INDENT.length();
        }
      }
      code.append(str);
      lineLength += str.length();
    }
    @Override() void appendBlockEnd() {
      endLine();
      indent--;
      append("}");
    }
    @Override() void appendBlockStart() {
      append(" {");
      indent++;
    }
    @Override() void appendOp(String op, boolean binOp) {
      if(binOp) {
        if(getLastChar() != ' ' && op.charAt(0) != ',') {
          append(" ");
        }
        append(op);
        append(" ");
      }
      else {
        append(op);
      }
    }
    @Override() void beginCaseBody() {
      super.beginCaseBody();
      indent++;
      endLine();
    }
    @Override() void endCaseBody() {
      super.endCaseBody();
      indent--;
      endStatement();
    }
    @Override() void endFile() {
      maybeEndStatement();
    }
    @Override() void endFunction(boolean statementContext) {
      super.endFunction(statementContext);
      if(statementContext) {
        startNewLine();
      }
    }
    @Override() void endLine() {
      startNewLine();
    }
    @Override() void listSeparator() {
      add(", ");
      maybeLineBreak();
    }
    @Override() void maybeCutLine() {
      if(lineLength > lineLengthThreshold) {
        startNewLine();
      }
    }
    @Override() void maybeLineBreak() {
      maybeCutLine();
    }
    @Override() void startNewLine() {
      if(lineLength > 0) {
        code.append('\n');
        lineIndex++;
        lineLength = 0;
      }
    }
  }
}