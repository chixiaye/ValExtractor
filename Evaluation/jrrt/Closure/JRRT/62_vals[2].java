package com.google.debugging.sourcemap;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.debugging.sourcemap.Base64VLQ.CharIterator;
import com.google.debugging.sourcemap.proto.Mapping.OriginalMapping;
import com.google.debugging.sourcemap.proto.Mapping.OriginalMapping.Builder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SourceMapConsumerV3 implements SourceMapConsumer, SourceMappingReversable  {
  final static int UNMAPPED = -1;
  private String[] sources;
  private String[] names;
  private int lineCount;
  private ArrayList<ArrayList<Entry>> lines = null;
  private Map<String, Map<Integer, Collection<OriginalMapping>>> reverseSourceMapping;
  public SourceMapConsumerV3() {
    super();
  }
  @Override() public Collection<OriginalMapping> getReverseMapping(String originalFile, int line, int column) {
    if(reverseSourceMapping == null) {
      createReverseMapping();
    }
    Map<Integer, Collection<OriginalMapping>> sourceLineToCollectionMap = reverseSourceMapping.get(originalFile);
    if(sourceLineToCollectionMap == null) {
      return Collections.emptyList();
    }
    else {
      Collection<OriginalMapping> mappings = sourceLineToCollectionMap.get(line);
      if(mappings == null) {
        return Collections.emptyList();
      }
      else {
        return mappings;
      }
    }
  }
  @Override() public Collection<String> getOriginalSources() {
    return Arrays.asList(sources);
  }
  @Override() public OriginalMapping getMappingForLine(int lineNumber, int column) {
    lineNumber--;
    column--;
    if(lineNumber < 0 || lineNumber >= lines.size()) {
      return null;
    }
    Preconditions.checkState(lineNumber >= 0);
    Preconditions.checkState(column >= 0);
    if(lines.get(lineNumber) == null) {
      return getPreviousMapping(lineNumber);
    }
    ArrayList<Entry> entries = lines.get(lineNumber);
    Preconditions.checkState(entries.size() > 0);
    if(entries.get(0).getGeneratedColumn() > column) {
      return getPreviousMapping(lineNumber);
    }
    int index = search(entries, column, 0, entries.size() - 1);
    Preconditions.checkState(index >= 0, "unexpected:%s", index);
    return getOriginalMappingForEntry(entries.get(index));
  }
  private OriginalMapping getOriginalMappingForEntry(Entry entry) {
    if(entry.getSourceFileId() == UNMAPPED) {
      return null;
    }
    else {
      Builder x = OriginalMapping.newBuilder().setOriginalFile(sources[entry.getSourceFileId()]).setLineNumber(entry.getSourceLine() + 1).setColumnPosition(entry.getSourceColumn() + 1);
      if(entry.getNameId() != UNMAPPED) {
        x.setIdentifier(names[entry.getNameId()]);
      }
      return x.build();
    }
  }
  private OriginalMapping getPreviousMapping(int lineNumber) {
    do {
      if(lineNumber == 0) {
        return null;
      }
      lineNumber--;
    }while(lines.get(lineNumber) == null);
    ArrayList<Entry> entries = lines.get(lineNumber);
    return getOriginalMappingForEntry(entries.get(entries.size() - 1));
  }
  private String[] getJavaStringArray(JSONArray array) throws JSONException {
    int len = array.length();
    String[] result = new String[len];
    for(int i = 0; i < len; i++) {
      result[i] = array.getString(i);
    }
    return result;
  }
  private int compareEntry(ArrayList<Entry> entries, int entry, int target) {
    return entries.get(entry).getGeneratedColumn() - target;
  }
  private int search(ArrayList<Entry> entries, int target, int start, int end) {
    while(true){
      int mid = ((end - start) / 2) + start;
      int compare = compareEntry(entries, mid, target);
      if(compare == 0) {
        return mid;
      }
      else 
        if(compare < 0) {
          start = mid + 1;
          if(start > end) {
            return end;
          }
        }
        else {
          end = mid - 1;
          if(end < start) {
            return end;
          }
        }
    }
  }
  private void createReverseMapping() {
    reverseSourceMapping = new HashMap<String, Map<Integer, Collection<OriginalMapping>>>();
    for(int targetLine = 0; targetLine < lines.size(); targetLine++) {
      ArrayList<Entry> entries = lines.get(targetLine);
      if(entries != null) {
        for (Entry entry : entries) {
          if(entry.getSourceFileId() != UNMAPPED && entry.getSourceLine() != UNMAPPED) {
            String originalFile = sources[entry.getSourceFileId()];
            if(!reverseSourceMapping.containsKey(originalFile)) {
              reverseSourceMapping.put(originalFile, new HashMap<Integer, Collection<OriginalMapping>>());
            }
            Map<Integer, Collection<OriginalMapping>> lineToCollectionMap = reverseSourceMapping.get(originalFile);
            int sourceLine = entry.getSourceLine();
            if(!lineToCollectionMap.containsKey(sourceLine)) {
              lineToCollectionMap.put(sourceLine, new ArrayList<OriginalMapping>(1));
            }
            Collection<OriginalMapping> mappings = lineToCollectionMap.get(sourceLine);
            Builder builder = OriginalMapping.newBuilder().setLineNumber(targetLine).setColumnPosition(entry.getGeneratedColumn());
            mappings.add(builder.build());
          }
        }
      }
    }
  }
  @Override() public void parse(String contents) throws SourceMapParseException {
    parse(contents, null);
  }
  public void parse(String contents, SourceMapSupplier sectionSupplier) throws SourceMapParseException {
    try {
      JSONObject sourceMapRoot = new JSONObject(contents);
      parse(sourceMapRoot, sectionSupplier);
    }
    catch (JSONException ex) {
      throw new SourceMapParseException("JSON parse exception: " + ex);
    }
  }
  public void parse(JSONObject sourceMapRoot) throws SourceMapParseException {
    parse(sourceMapRoot, null);
  }
  public void parse(JSONObject sourceMapRoot, SourceMapSupplier sectionSupplier) throws SourceMapParseException {
    try {
      int version = sourceMapRoot.getInt("version");
      if(version != 3) {
        throw new SourceMapParseException("Unknown version: " + version);
      }
      String file = sourceMapRoot.getString("file");
      if(file.isEmpty()) {
        throw new SourceMapParseException("File entry is missing or empty");
      }
      if(sourceMapRoot.has("sections")) {
        parseMetaMap(sourceMapRoot, sectionSupplier);
        return ;
      }
      lineCount = sourceMapRoot.getInt("lineCount");
      String lineMap = sourceMapRoot.getString("mappings");
      sources = getJavaStringArray(sourceMapRoot.getJSONArray("sources"));
      names = getJavaStringArray(sourceMapRoot.getJSONArray("names"));
      lines = Lists.newArrayListWithCapacity(lineCount);
      new MappingBuilder(lineMap).build();
    }
    catch (JSONException ex) {
      throw new SourceMapParseException("JSON parse exception: " + ex);
    }
  }
  private void parseMetaMap(JSONObject sourceMapRoot, SourceMapSupplier sectionSupplier) throws SourceMapParseException {
    if(sectionSupplier == null) {
      sectionSupplier = new DefaultSourceMapSupplier();
    }
    try {
      int version = sourceMapRoot.getInt("version");
      if(version != 3) {
        throw new SourceMapParseException("Unknown version: " + version);
      }
      String file = sourceMapRoot.getString("file");
      if(file.isEmpty()) {
        throw new SourceMapParseException("File entry is missing or empty");
      }
      if(sourceMapRoot.has("lineCount") || sourceMapRoot.has("mappings") || sourceMapRoot.has("sources") || sourceMapRoot.has("names")) {
        throw new SourceMapParseException("Invalid map format");
      }
      SourceMapGeneratorV3 generator = new SourceMapGeneratorV3();
      JSONArray sections = sourceMapRoot.getJSONArray("sections");
      for(int i = 0, count = sections.length(); i < count; i++) {
        JSONObject section = sections.getJSONObject(i);
        if(section.has("map") && section.has("url")) {
          throw new SourceMapParseException("Invalid map format: section may not have both \'map\' and \'url\'");
        }
        JSONObject offset = section.getJSONObject("offset");
        int line = offset.getInt("line");
        int column = offset.getInt("column");
        String mapSectionContents;
        if(section.has("url")) {
          String url = section.getString("url");
          mapSectionContents = sectionSupplier.getSourceMap(url);
          if(mapSectionContents == null) {
            throw new SourceMapParseException("Unable to retrieve: " + url);
          }
        }
        else 
          if(section.has("map")) {
            mapSectionContents = section.getString("map");
          }
          else {
            throw new SourceMapParseException("Invalid map format: section must have either \'map\' or \'url\'");
          }
        generator.mergeMapSection(line, column, mapSectionContents);
      }
      StringBuilder sb = new StringBuilder();
      try {
        generator.appendTo(sb, file);
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
      parse(sb.toString());
    }
    catch (IOException ex) {
      throw new SourceMapParseException("IO exception: " + ex);
    }
    catch (JSONException ex) {
      throw new SourceMapParseException("JSON parse exception: " + ex);
    }
  }
  public void visitMappings(EntryVisitor visitor) {
    boolean pending = false;
    String sourceName = null;
    String symbolName = null;
    FilePosition sourceStartPosition = null;
    FilePosition startPosition = null;
    final int lineCount = lines.size();
    for(int i = 0; i < lineCount; i++) {
      ArrayList<Entry> line = lines.get(i);
      if(line != null) {
        final int entryCount = line.size();
        for(int j = 0; j < entryCount; j++) {
          Entry entry = line.get(j);
          if(pending) {
            FilePosition endPosition = new FilePosition(i, entry.getGeneratedColumn());
            visitor.visit(sourceName, symbolName, sourceStartPosition, startPosition, endPosition);
            pending = false;
          }
          if(entry.getSourceFileId() != UNMAPPED) {
            pending = true;
            sourceName = sources[entry.getSourceFileId()];
            symbolName = (entry.getNameId() != UNMAPPED) ? names[entry.getNameId()] : null;
            sourceStartPosition = new FilePosition(entry.getSourceLine(), entry.getSourceColumn());
            startPosition = new FilePosition(i, entry.getGeneratedColumn());
          }
        }
      }
    }
  }
  
  static class DefaultSourceMapSupplier implements SourceMapSupplier  {
    @Override() public String getSourceMap(String url) {
      return null;
    }
  }
  
  private interface Entry  {
    int getGeneratedColumn();
    int getNameId();
    int getSourceColumn();
    int getSourceFileId();
    int getSourceLine();
  }
  
  public static interface EntryVisitor  {
    void visit(String sourceName, String symbolName, FilePosition sourceStartPosition, FilePosition startPosition, FilePosition endPosition);
  }
  
  private class MappingBuilder  {
    final private static int MAX_ENTRY_VALUES = 5;
    final private StringCharIterator content;
    private int line = 0;
    private int previousCol = 0;
    private int previousSrcId = 0;
    private int previousSrcLine = 0;
    private int previousSrcColumn = 0;
    private int previousNameId = 0;
    MappingBuilder(String lineMap) {
      super();
      this.content = new StringCharIterator(lineMap);
    }
    private Entry decodeEntry(int[] vals, int entryValues) {
      Entry entry;
      switch (entryValues){
        case 1:
        entry = new UnmappedEntry(vals[0] + previousCol);
        previousCol = entry.getGeneratedColumn();
        return entry;
        case 4:
        int var_62 = vals[2];
        entry = new UnnamedEntry(vals[0] + previousCol, vals[1] + previousSrcId, var_62 + previousSrcLine, vals[3] + previousSrcColumn);
        previousCol = entry.getGeneratedColumn();
        previousSrcId = entry.getSourceFileId();
        previousSrcLine = entry.getSourceLine();
        previousSrcColumn = entry.getSourceColumn();
        return entry;
        case 5:
        entry = new NamedEntry(vals[0] + previousCol, vals[1] + previousSrcId, vals[2] + previousSrcLine, vals[3] + previousSrcColumn, vals[4] + previousNameId);
        previousCol = entry.getGeneratedColumn();
        previousSrcId = entry.getSourceFileId();
        previousSrcLine = entry.getSourceLine();
        previousSrcColumn = entry.getSourceColumn();
        previousNameId = entry.getNameId();
        return entry;
        default:
        throw new IllegalStateException("Unexpected number of values for entry:" + entryValues);
      }
    }
    private boolean entryComplete() {
      if(!content.hasNext()) {
        return true;
      }
      char c = content.peek();
      return (c == ';' || c == ',');
    }
    private boolean tryConsumeToken(char token) {
      if(content.hasNext() && content.peek() == token) {
        content.next();
        return true;
      }
      return false;
    }
    private int nextValue() {
      return Base64VLQ.decode(content);
    }
    void build() {
      int[] temp = new int[MAX_ENTRY_VALUES];
      ArrayList<Entry> entries = new ArrayList<Entry>();
      while(content.hasNext()){
        if(tryConsumeToken(';')) {
          ArrayList<Entry> result;
          if(entries.size() > 0) {
            result = entries;
            entries = new ArrayList<Entry>();
          }
          else {
            result = null;
          }
          lines.add(result);
          entries.clear();
          line++;
          previousCol = 0;
        }
        else {
          int entryValues = 0;
          while(!entryComplete()){
            temp[entryValues] = nextValue();
            entryValues++;
          }
          Entry entry = decodeEntry(temp, entryValues);
          validateEntry(entry);
          entries.add(entry);
          tryConsumeToken(',');
        }
      }
    }
    private void validateEntry(Entry entry) {
      Preconditions.checkState(line < lineCount);
      Preconditions.checkState(entry.getSourceFileId() == UNMAPPED || entry.getSourceFileId() < sources.length);
      Preconditions.checkState(entry.getNameId() == UNMAPPED || entry.getNameId() < names.length);
    }
  }
  
  private static class NamedEntry extends UnnamedEntry  {
    final private int name;
    NamedEntry(int column, int srcFile, int srcLine, int srcColumn, int name) {
      super(column, srcFile, srcLine, srcColumn);
      this.name = name;
    }
    @Override() public int getNameId() {
      return name;
    }
  }
  
  private static class StringCharIterator implements CharIterator  {
    final String content;
    final int length;
    int current = 0;
    StringCharIterator(String content) {
      super();
      this.content = content;
      this.length = content.length();
    }
    @Override() public boolean hasNext() {
      return current < length;
    }
    @Override() public char next() {
      return content.charAt(current++);
    }
    char peek() {
      return content.charAt(current);
    }
  }
  
  private static class UnmappedEntry implements Entry  {
    final private int column;
    UnmappedEntry(int column) {
      super();
      this.column = column;
    }
    @Override() public int getGeneratedColumn() {
      return column;
    }
    @Override() public int getNameId() {
      return UNMAPPED;
    }
    @Override() public int getSourceColumn() {
      return UNMAPPED;
    }
    @Override() public int getSourceFileId() {
      return UNMAPPED;
    }
    @Override() public int getSourceLine() {
      return UNMAPPED;
    }
  }
  
  private static class UnnamedEntry extends UnmappedEntry  {
    final private int srcFile;
    final private int srcLine;
    final private int srcColumn;
    UnnamedEntry(int column, int srcFile, int srcLine, int srcColumn) {
      super(column);
      this.srcFile = srcFile;
      this.srcLine = srcLine;
      this.srcColumn = srcColumn;
    }
    @Override() public int getNameId() {
      return UNMAPPED;
    }
    @Override() public int getSourceColumn() {
      return srcColumn;
    }
    @Override() public int getSourceFileId() {
      return srcFile;
    }
    @Override() public int getSourceLine() {
      return srcLine;
    }
  }
}