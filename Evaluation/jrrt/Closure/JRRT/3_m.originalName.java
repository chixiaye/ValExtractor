package com.google.debugging.sourcemap;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nullable;

public class SourceMapGeneratorV1 implements SourceMapGenerator  {
  final private static int UNMAPPED = -1;
  private List<Mapping> mappings = Lists.newArrayList();
  private Mapping lastMapping;
  private FilePosition offsetPosition = new FilePosition(0, 0);
  private FilePosition prefixPosition = new FilePosition(0, 0);
  private static String escapeString(String value) {
    return Util.escapeString(value);
  }
  private int prepMappings() throws IOException {
    (new MappingTraversal()).traverse(new UsedMappingCheck());
    int id = 0;
    int maxLine = 0;
    for (Mapping m : mappings) {
      if(m.used) {
        m.id = id++;
        int endPositionLine = m.endPosition.getLine();
        maxLine = Math.max(maxLine, endPositionLine);
      }
    }
    return maxLine + prefixPosition.getLine();
  }
  @Override() public void addMapping(String sourceName, @Nullable() String symbolName, FilePosition sourceStartPosition, FilePosition startPosition, FilePosition endPosition) {
    if(sourceName == null || sourceStartPosition.getLine() < 0) {
      return ;
    }
    Mapping mapping = new Mapping();
    mapping.sourceFile = sourceName;
    mapping.originalPosition = sourceStartPosition;
    mapping.originalName = symbolName;
    if(offsetPosition.getLine() == 0 && offsetPosition.getColumn() == 0) {
      mapping.startPosition = startPosition;
      mapping.endPosition = endPosition;
    }
    else {
      int offsetLine = offsetPosition.getLine();
      int startOffsetPosition = offsetPosition.getColumn();
      int endOffsetPosition = offsetPosition.getColumn();
      if(startPosition.getLine() > 0) {
        startOffsetPosition = 0;
      }
      if(endPosition.getLine() > 0) {
        endOffsetPosition = 0;
      }
      mapping.startPosition = new FilePosition(startPosition.getLine() + offsetLine, startPosition.getColumn() + startOffsetPosition);
      mapping.endPosition = new FilePosition(endPosition.getLine() + offsetLine, endPosition.getColumn() + endOffsetPosition);
    }
    if(lastMapping != null) {
      int lastLine = lastMapping.startPosition.getLine();
      int lastColumn = lastMapping.startPosition.getColumn();
      int nextLine = mapping.startPosition.getLine();
      int nextColumn = mapping.startPosition.getColumn();
      Preconditions.checkState(nextLine > lastLine || (nextLine == lastLine && nextColumn >= lastColumn), "Incorrect source mappings order, previous : (%s,%s)\n" + "new : (%s,%s)\nnode : %s", lastLine, lastColumn, nextLine, nextColumn);
    }
    lastMapping = mapping;
    mappings.add(mapping);
  }
  @Override() public void appendIndexMapTo(Appendable out, String name, List<SourceMapSection> appSections) {
    throw new UnsupportedOperationException();
  }
  @Override() public void appendTo(Appendable out, String name) throws IOException {
    int maxLine = prepMappings();
    out.append("/** Begin line maps. **/{ \"file\" : ");
    out.append(escapeString(name));
    out.append(", \"count\": ");
    out.append(String.valueOf(maxLine + 1));
    out.append(" }\n");
    (new LineMapper(out)).appendLineMappings();
    out.append("/** Begin file information. **/\n");
    for(int i = 0; i <= maxLine; ++i) {
      out.append("[]\n");
    }
    out.append("/** Begin mapping definitions. **/\n");
    (new MappingWriter()).appendMappings(out);
  }
  @Override() public void reset() {
    mappings = Lists.newArrayList();
    lastMapping = null;
    offsetPosition = new FilePosition(0, 0);
    prefixPosition = new FilePosition(0, 0);
  }
  @Override() public void setStartingPosition(int offsetLine, int offsetIndex) {
    Preconditions.checkState(offsetLine >= 0);
    Preconditions.checkState(offsetIndex >= 0);
    offsetPosition = new FilePosition(offsetLine, offsetIndex);
  }
  @Override() public void setWrapperPrefix(String prefix) {
    int prefixLine = 0;
    int prefixIndex = 0;
    for(int i = 0; i < prefix.length(); ++i) {
      if(prefix.charAt(i) == '\n') {
        prefixLine++;
        prefixIndex = 0;
      }
      else {
        prefixIndex++;
      }
    }
    prefixPosition = new FilePosition(prefixLine, prefixIndex);
  }
  @Override() public void validate(boolean validate) {
  }
  
  private class LineMapper implements MappingVisitor  {
    final private Appendable out;
    private boolean firstChar = true;
    final private static String UNMAPPED_STRING = "-1";
    private int lastId = UNMAPPED;
    private String lastIdString = UNMAPPED_STRING;
    LineMapper(Appendable out) {
      super();
      this.out = out;
    }
    private void addCharEntry(String id) throws IOException {
      if(out != null) {
        if(firstChar) {
          firstChar = false;
        }
        else {
          out.append(",");
        }
        out.append(id);
      }
    }
    void appendLineMappings() throws IOException {
      openLine();
      (new MappingTraversal()).traverse(this);
      closeLine();
    }
    private void closeLine() throws IOException {
      if(out != null) {
        out.append("]\n");
      }
    }
    private void openLine() throws IOException {
      if(out != null) {
        out.append("[");
        this.firstChar = true;
      }
    }
    @Override() public void visit(Mapping m, int line, int col, int nextLine, int nextCol) throws IOException {
      int id = (m != null) ? m.id : UNMAPPED;
      if(lastId != id) {
        lastIdString = (id == UNMAPPED) ? UNMAPPED_STRING : String.valueOf(id);
        lastId = id;
      }
      String idString = lastIdString;
      for(int i = line; i <= nextLine; i++) {
        if(i == nextLine) {
          for(int j = col; j < nextCol; j++) {
            addCharEntry(idString);
          }
          break ;
        }
        closeLine();
        openLine();
        col = 0;
      }
    }
  }
  
  static class Mapping  {
    int id = UNMAPPED;
    String sourceFile;
    FilePosition originalPosition;
    FilePosition startPosition;
    FilePosition endPosition;
    String originalName;
    boolean used = false;
  }
  
  private class MappingTraversal  {
    private int line;
    private int col;
    MappingTraversal() {
      super();
    }
    private boolean isOverlapped(Mapping m1, Mapping m2) {
      int l1 = m1.endPosition.getLine();
      int l2 = m2.startPosition.getLine();
      int c1 = m1.endPosition.getColumn();
      int c2 = m2.startPosition.getColumn();
      return (l1 == l2 && c1 >= c2) || l1 > l2;
    }
    private int getAdjustedCol(FilePosition p) {
      int rawLine = p.getLine();
      int rawCol = p.getColumn();
      return (rawLine != 0) ? rawCol : rawCol + prefixPosition.getColumn();
    }
    private int getAdjustedLine(FilePosition p) {
      return p.getLine() + prefixPosition.getLine();
    }
    private void maybeVisit(MappingVisitor v, Mapping m) throws IOException {
      int nextLine = getAdjustedLine(m.endPosition);
      int nextCol = getAdjustedCol(m.endPosition);
      if(line < nextLine || (line == nextLine && col < nextCol)) {
        visit(v, m, nextLine, nextCol);
      }
    }
    private void maybeVisitParent(MappingVisitor v, Mapping parent, Mapping m) throws IOException {
      int nextLine = getAdjustedLine(m.startPosition);
      int nextCol = getAdjustedCol(m.startPosition);
      Preconditions.checkState(line < nextLine || col <= nextCol);
      if(line < nextLine || (line == nextLine && col < nextCol)) {
        visit(v, parent, nextLine, nextCol);
      }
    }
    void traverse(MappingVisitor v) throws IOException {
      Deque<Mapping> stack = new ArrayDeque<Mapping>();
      for (Mapping m : mappings) {
        while(!stack.isEmpty() && !isOverlapped(stack.peek(), m)){
          Mapping previous = stack.pop();
          maybeVisit(v, previous);
        }
        Mapping parent = stack.peek();
        maybeVisitParent(v, parent, m);
        stack.push(m);
      }
      while(!stack.isEmpty()){
        Mapping m = stack.pop();
        maybeVisit(v, m);
      }
    }
    private void visit(MappingVisitor v, Mapping m, int nextLine, int nextCol) throws IOException {
      Preconditions.checkState(line <= nextLine);
      Preconditions.checkState(line < nextLine || col < nextCol);
      if(line == nextLine && col == nextCol) {
        Preconditions.checkState(false);
        return ;
      }
      v.visit(m, line, col, nextLine, nextCol);
      line = nextLine;
      col = nextCol;
    }
  }
  
  private interface MappingVisitor  {
    void visit(Mapping m, int line, int col, int endLine, int endCol) throws IOException;
  }
  
  private class MappingWriter  {
    private String lastSourceFile = null;
    private String lastSourceFileEscaped = null;
    private int lastLine = 0;
    private String lastLineString = String.valueOf(0);
    private void appendMappingTo(Mapping m, Appendable out) throws IOException {
      out.append("[");
      String sourceFile = m.sourceFile;
      String escapedSourceFile;
      if(lastSourceFile != sourceFile) {
        lastSourceFile = sourceFile;
        lastSourceFileEscaped = escapeString(sourceFile);
      }
      escapedSourceFile = lastSourceFileEscaped;
      out.append(escapedSourceFile);
      out.append(",");
      int line = m.originalPosition.getLine();
      if(line != lastLine) {
        lastLineString = String.valueOf(line);
      }
      String lineValue = lastLineString;
      out.append(lineValue);
      out.append(",");
      out.append(String.valueOf(m.originalPosition.getColumn()));
      String var_3 = m.originalName;
      if(var_3 != null) {
        out.append(",");
        out.append(escapeString(m.originalName));
      }
      out.append("]\n");
    }
    void appendMappings(Appendable out) throws IOException {
      for (Mapping m : mappings) {
        if(m.used) {
          appendMappingTo(m, out);
        }
      }
    }
  }
  
  private class UsedMappingCheck implements MappingVisitor  {
    @Override() public void visit(Mapping m, int line, int col, int nextLine, int nextCol) throws IOException {
      if(m != null) {
        m.used = true;
      }
    }
  }
}