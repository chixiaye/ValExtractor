package com.google.debugging.sourcemap;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;

public class SourceMapGeneratorV2 implements SourceMapGenerator  {
  private boolean validate = false;
  final private static int UNMAPPED = -1;
  private List<Mapping> mappings = Lists.newArrayList();
  private LinkedHashMap<String, Integer> sourceFileMap = Maps.newLinkedHashMap();
  private LinkedHashMap<String, Integer> originalNameMap = Maps.newLinkedHashMap();
  private String lastSourceFile = null;
  private int lastSourceFileIndex = -1;
  private Mapping lastMapping;
  private FilePosition offsetPosition = new FilePosition(0, 0);
  private FilePosition prefixPosition = new FilePosition(0, 0);
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
    for (Mapping m : mappings) {
      if(m.used) {
        m.id = id++;
        int endPositionLine = m.endPosition.getLine();
        maxLine = Math.max(maxLine, endPositionLine);
      }
    }
    return maxLine + prefixPosition.getLine();
  }
  private void addMap(Appendable out, Map<String, Integer> map) throws IOException {
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
    mapping.sourceFile = getSourceId(sourceName);
    mapping.originalPosition = sourceStartPosition;
    mapping.originalName = symbolName;
    mapping.startPosition = adjustedStart;
    mapping.endPosition = adjustedEnd;
    if(lastMapping != null) {
      FilePosition var_34 = lastMapping.startPosition;
      int lastLine = var_34.getLine();
      int lastColumn = lastMapping.startPosition.getColumn();
      int nextLine = mapping.startPosition.getLine();
      int nextColumn = mapping.startPosition.getColumn();
      Preconditions.checkState(nextLine > lastLine || (nextLine == lastLine && nextColumn >= lastColumn), "Incorrect source mappings order, previous : (%s,%s)\n" + "new : (%s,%s)\nnode : %s", lastLine, lastColumn, nextLine, nextColumn);
    }
    lastMapping = mapping;
    mappings.add(mapping);
  }
  private void addOriginalNameMap(Appendable out) throws IOException {
    addMap(out, originalNameMap);
  }
  private void addSourceNameMap(Appendable out) throws IOException {
    addMap(out, sourceFileMap);
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
  @Override() public void appendIndexMapTo(Appendable out, String name, List<SourceMapSection> appSections) {
    throw new UnsupportedOperationException();
  }
  @Override() public void appendTo(Appendable out, String name) throws IOException {
    int maxLine = prepMappings();
    out.append("{\n");
    appendFirstField(out, "version", "2");
    appendField(out, "file", escapeString(name));
    appendField(out, "lineCount", String.valueOf(maxLine + 1));
    appendFieldStart(out, "lineMaps");
    out.append("[");
    (new LineMapper(out)).appendLineMappings();
    out.append("]");
    appendFieldEnd(out);
    appendFieldStart(out, "mappings");
    out.append("[");
    (new MappingWriter()).appendMappings(out);
    out.append("]");
    appendFieldEnd(out);
    appendFieldStart(out, "sources");
    out.append("[");
    addSourceNameMap(out);
    out.append("]");
    appendFieldEnd(out);
    appendFieldStart(out, "names");
    out.append("[");
    addOriginalNameMap(out);
    out.append("]");
    appendFieldEnd(out);
    out.append("\n}\n");
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
  @Override() @VisibleForTesting() public void validate(boolean validate) {
    this.validate = validate;
  }
  
  @VisibleForTesting() public static class LineMapEncoder  {
    static String valueToBase64(int value, int minimumSize) {
      int size = 0;
      char[] chars = new char[4];
      do {
        int charValue = value & 63;
        value = value >>> 6;
        chars[size++] = Base64.toBase64(charValue);
      }while(value > 0);
      StringBuilder sb = new StringBuilder(size);
      while(minimumSize > size){
        sb.append(Base64.toBase64(0));
        minimumSize--;
      }
      while(size > 0){
        sb.append(chars[--size]);
      }
      return sb.toString();
    }
    public static int getRelativeMappingId(int id, int idLength, int lastId) {
      int base = 1 << (idLength * 6);
      int relativeId = id - lastId;
      return (relativeId < 0) ? relativeId + base : relativeId;
    }
    public static int getRelativeMappingIdLength(int rawId, int lastId) {
      Preconditions.checkState(rawId >= 0 || rawId == UNMAPPED);
      int relativeId = rawId - lastId;
      int id = (relativeId < 0 ? Math.abs(relativeId) - 1 : relativeId) << 1;
      int digits = 1;
      int base = 64;
      while(id >= base){
        digits++;
        base *= 64;
      }
      return digits;
    }
    public static void encodeEntry(Appendable out, int id, int lastId, int reps) throws IOException {
      Preconditions.checkState(reps > 0);
      int relativeIdLength = getRelativeMappingIdLength(id, lastId);
      int relativeId = getRelativeMappingId(id, relativeIdLength, lastId);
      String relativeIdString = valueToBase64(relativeId, relativeIdLength);
      if(reps > 16 || relativeIdLength > 4) {
        String repsString = valueToBase64(reps - 1, 1);
        for(int i = 0; i < repsString.length(); i++) {
          out.append('!');
        }
        String sizeId = valueToBase64(relativeIdString.length() - 1, 1);
        out.append(sizeId);
        out.append(repsString);
      }
      else {
        int prefix = ((reps - 1) << 2) + (relativeIdString.length() - 1);
        Preconditions.checkState(prefix < 64 && prefix >= 0, "prefix (%s) reps(%s) map id size(%s)", prefix, reps, relativeIdString.length());
        out.append(valueToBase64(prefix, 1));
      }
      out.append(relativeIdString);
    }
  }
  
  private class LineMapper implements MappingVisitor  {
    final private Appendable out;
    private int lastId = UNMAPPED;
    LineMapper(Appendable out) {
      super();
      this.out = out;
    }
    void appendLineMappings() throws IOException {
      openLine();
      (new MappingTraversal()).traverse(this);
      closeLine(true);
    }
    private void closeEntry(int id, int reps) throws IOException {
      if(reps == 0) {
        return ;
      }
      StringBuilder sb = new StringBuilder();
      LineMapEncoder.encodeEntry(sb, id, lastId, reps);
      if(validate) {
        SourceMapLineDecoder.LineEntry entry = SourceMapLineDecoder.decodeLineEntry(sb.toString(), lastId);
        Preconditions.checkState(entry.id == id && entry.reps == reps, "expected (%s,%s) but got (%s,%s)", id, reps, entry.id, entry.reps);
      }
      out.append(sb);
      lastId = id;
    }
    private void closeLine(boolean finalEntry) throws IOException {
      if(finalEntry) {
        out.append("\"");
      }
      else {
        out.append("\",\n");
      }
    }
    private void openLine() throws IOException {
      out.append("\"");
      this.lastId = 0;
    }
    @Override() public void visit(Mapping m, int line, int col, int nextLine, int nextCol) throws IOException {
      int id = (m != null) ? m.id : UNMAPPED;
      for(int i = line; i <= nextLine; i++) {
        if(i == nextLine) {
          closeEntry(id, nextCol - col);
          break ;
        }
        closeLine(false);
        openLine();
        col = 0;
      }
    }
  }
  
  static class Mapping  {
    int id = UNMAPPED;
    int sourceFile;
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
    private int lastLine = 0;
    private String lastLineString = String.valueOf(0);
    private void appendMappingTo(Mapping m, Appendable out) throws IOException {
      out.append("[");
      out.append(String.valueOf(m.sourceFile));
      out.append(",");
      int line = m.originalPosition.getLine();
      if(line != lastLine) {
        lastLineString = String.valueOf(line);
      }
      String lineValue = lastLineString;
      out.append(lineValue);
      out.append(",");
      out.append(String.valueOf(m.originalPosition.getColumn()));
      if(m.originalName != null) {
        out.append(",");
        out.append(String.valueOf(getNameId(m.originalName)));
      }
      out.append("],\n");
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