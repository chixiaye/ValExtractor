package com.google.debugging.sourcemap;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.debugging.sourcemap.SourceMapConsumerV3.EntryVisitor;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;

public class SourceMapGeneratorV3 implements SourceMapGenerator  {
  final private static int UNMAPPED = -1;
  private List<Mapping> mappings = Lists.newArrayList();
  private LinkedHashMap<String, Integer> sourceFileMap = Maps.newLinkedHashMap();
  private LinkedHashMap<String, Integer> originalNameMap = Maps.newLinkedHashMap();
  private String lastSourceFile = null;
  private int lastSourceFileIndex = -1;
  private Mapping lastMapping;
  private FilePosition offsetPosition = new FilePosition(0, 0);
  private FilePosition prefixPosition = new FilePosition(0, 0);
  private CharSequence offsetValue(int line, int column) throws IOException {
    StringBuilder out = new StringBuilder();
    out.append("{\n");
    appendFirstField(out, "line", String.valueOf(line));
    appendField(out, "column", String.valueOf(column));
    out.append("\n}");
    return out;
  }
  private static String escapeString(String value) {
    return Util.escapeString(value);
  }
  private int getNameId(String symbolName) {
    int originalNameIndex;
    Integer index = originalNameMap.get(symbolName);
    if(index != null) {
      originalNameIndex = index;
    }
    else {
      originalNameIndex = originalNameMap.size();
      originalNameMap.put(symbolName, originalNameIndex);
    }
    return originalNameIndex;
  }
  private int getSourceId(String sourceName) {
    if(sourceName != lastSourceFile) {
      lastSourceFile = sourceName;
      Integer index = sourceFileMap.get(sourceName);
      if(index != null) {
        lastSourceFileIndex = index;
      }
      else {
        lastSourceFileIndex = sourceFileMap.size();
        sourceFileMap.put(sourceName, lastSourceFileIndex);
      }
    }
    return lastSourceFileIndex;
  }
  private int prepMappings() throws IOException {
    (new MappingTraversal()).traverse(new UsedMappingCheck());
    int id = 0;
    int maxLine = 0;
    int sourceId = 0;
    int nameId = 0;
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
    FilePosition adjustedStart = startPosition;
    FilePosition adjustedEnd = endPosition;
    if(offsetPosition.getLine() != 0 || offsetPosition.getColumn() != 0) {
      int offsetLine = offsetPosition.getLine();
      int startOffsetPosition = offsetPosition.getColumn();
      int endOffsetPosition = offsetPosition.getColumn();
      if(startPosition.getLine() > 0) {
        startOffsetPosition = 0;
      }
      if(endPosition.getLine() > 0) {
        endOffsetPosition = 0;
      }
      adjustedStart = new FilePosition(startPosition.getLine() + offsetLine, startPosition.getColumn() + startOffsetPosition);
      adjustedEnd = new FilePosition(endPosition.getLine() + offsetLine, endPosition.getColumn() + endOffsetPosition);
    }
    Mapping mapping = new Mapping();
    mapping.sourceFile = sourceName;
    mapping.originalPosition = sourceStartPosition;
    mapping.originalName = symbolName;
    mapping.startPosition = adjustedStart;
    mapping.endPosition = adjustedEnd;
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
  private void addNameMap(Appendable out, Map<String, Integer> map) throws IOException {
    int i = 0;
    for (Entry<String, Integer> entry : map.entrySet()) {
      String key = entry.getKey();
      if(i != 0) {
        out.append(",");
      }
      out.append(escapeString(key));
      i++;
    }
  }
  private void addSourceNameMap(Appendable out) throws IOException {
    addNameMap(out, sourceFileMap);
  }
  private void addSymbolNameMap(Appendable out) throws IOException {
    addNameMap(out, originalNameMap);
  }
  private static void appendField(Appendable out, String name, CharSequence value) throws IOException {
    out.append(",\n");
    out.append("\"");
    out.append(name);
    out.append("\"");
    out.append(":");
    out.append(value);
  }
  @SuppressWarnings(value = {"unused", }) private static void appendFieldEnd(Appendable out) throws IOException {
  }
  private static void appendFieldStart(Appendable out, String name) throws IOException {
    appendField(out, name, "");
  }
  private static void appendFirstField(Appendable out, String name, CharSequence value) throws IOException {
    out.append("\"");
    out.append(name);
    out.append("\"");
    out.append(":");
    out.append(value);
  }
  @Override() public void appendIndexMapTo(Appendable out, String name, List<SourceMapSection> sections) throws IOException {
    out.append("{\n");
    appendFirstField(out, "version", "3");
    appendField(out, "file", escapeString(name));
    appendFieldStart(out, "sections");
    out.append("[\n");
    boolean first = true;
    int line = 0;
    int column = 0;
    for (SourceMapSection section : sections) {
      if(first) {
        first = false;
      }
      else {
        out.append(",\n");
      }
      out.append("{\n");
      appendFirstField(out, "offset", offsetValue(section.getLine(), section.getColumn()));
      if(section.getSectionType() == SourceMapSection.SectionType.URL) {
        appendField(out, "url", escapeString(section.getSectionValue()));
      }
      else 
        if(section.getSectionType() == SourceMapSection.SectionType.MAP) {
          appendField(out, "map", section.getSectionValue());
        }
        else {
          throw new IOException("Unexpected section type");
        }
      out.append("\n}");
    }
    out.append("\n]");
    appendFieldEnd(out);
    out.append("\n}\n");
  }
  @Override() public void appendTo(Appendable out, String name) throws IOException {
    int maxLine = prepMappings();
    out.append("{\n");
    appendFirstField(out, "version", "3");
    appendField(out, "file", escapeString(name));
    appendField(out, "lineCount", String.valueOf(maxLine + 1));
    appendFieldStart(out, "mappings");
    (new LineMapper(out)).appendLineMappings();
    appendFieldEnd(out);
    appendFieldStart(out, "sources");
    out.append("[");
    addSourceNameMap(out);
    out.append("]");
    appendFieldEnd(out);
    appendFieldStart(out, "names");
    out.append("[");
    addSymbolNameMap(out);
    out.append("]");
    appendFieldEnd(out);
    out.append("\n}\n");
  }
  public void mergeMapSection(int line, int column, String mapSectionContents) throws SourceMapParseException {
    setStartingPosition(line, column);
    SourceMapConsumerV3 section = new SourceMapConsumerV3();
    section.parse(mapSectionContents);
    section.visitMappings(new ConsumerEntryVisitor());
  }
  @Override() public void reset() {
    mappings.clear();
    lastMapping = null;
    sourceFileMap.clear();
    originalNameMap.clear();
    lastSourceFile = null;
    lastSourceFileIndex = -1;
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
  
  class ConsumerEntryVisitor implements EntryVisitor  {
    @Override() public void visit(String sourceName, String symbolName, FilePosition sourceStartPosition, FilePosition startPosition, FilePosition endPosition) {
      addMapping(sourceName, symbolName, sourceStartPosition, startPosition, endPosition);
    }
  }
  
  private class LineMapper implements MappingVisitor  {
    final private Appendable out;
    private int previousLine = -1;
    private int previousColumn = 0;
    private int previousSourceFileId;
    private int previousSourceLine;
    private int previousSourceColumn;
    private int previousNameId;
    LineMapper(Appendable out) {
      super();
      this.out = out;
    }
    void appendLineMappings() throws IOException {
      openLine(true);
      (new MappingTraversal()).traverse(this);
      closeLine(true);
    }
    private void closeLine(boolean finalEntry) throws IOException {
      out.append(';');
      if(finalEntry) {
        out.append('\"');
      }
    }
    private void openLine(boolean firstEntry) throws IOException {
      if(firstEntry) {
        out.append('\"');
      }
    }
    @Override() public void visit(Mapping m, int line, int col, int nextLine, int nextCol) throws IOException {
      int id = (m != null) ? m.id : UNMAPPED;
      if(previousLine != line) {
        previousColumn = 0;
      }
      if(line != nextLine || col != nextCol) {
        if(previousLine == line) {
          out.append(',');
        }
        writeEntry(m, col);
        previousLine = line;
        previousColumn = col;
      }
      for(int i = line; i <= nextLine; i++) {
        if(i == nextLine) {
          break ;
        }
        closeLine(false);
        openLine(false);
      }
    }
    void writeEntry(Mapping m, int column) throws IOException {
      Base64VLQ.encode(out, column - previousColumn);
      previousColumn = column;
      if(m != null) {
        int sourceId = getSourceId(m.sourceFile);
        Base64VLQ.encode(out, sourceId - previousSourceFileId);
        previousSourceFileId = sourceId;
        int srcline = m.originalPosition.getLine();
        int srcColumn = m.originalPosition.getColumn();
        Base64VLQ.encode(out, srcline - previousSourceLine);
        previousSourceLine = srcline;
        Base64VLQ.encode(out, srcColumn - previousSourceColumn);
        previousSourceColumn = srcColumn;
        if(m.originalName != null) {
          int nameId = getNameId(m.originalName);
          Base64VLQ.encode(out, (nameId - previousNameId));
          previousNameId = nameId;
        }
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
      FilePosition var_92 = m.startPosition;
      int nextLine = getAdjustedLine(var_92);
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
  
  private class UsedMappingCheck implements MappingVisitor  {
    @Override() public void visit(Mapping m, int line, int col, int nextLine, int nextCol) throws IOException {
      if(m != null) {
        m.used = true;
      }
    }
  }
}