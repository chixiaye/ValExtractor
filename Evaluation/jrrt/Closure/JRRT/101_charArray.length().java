package com.google.debugging.sourcemap;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Shorts;
import com.google.debugging.sourcemap.proto.Mapping.OriginalMapping;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class SourceMapConsumerV1 implements SourceMapConsumer  {
  final private static String LINEMAP_HEADER = "/** Begin line maps. **/";
  final private static String FILEINFO_HEADER = "/** Begin file information. **/";
  final private static String DEFINITION_HEADER = "/** Begin mapping definitions. **/";
  private ImmutableList<ImmutableList<LineFragment>> characterMap;
  private ImmutableList<SourceFile> mappings;
  private FileName splitFileName(Interner<String> interner, String input) {
    int hashIndex = input.lastIndexOf('/');
    String dir = interner.intern(input.substring(0, hashIndex + 1));
    String fileName = interner.intern(input.substring(hashIndex + 1));
    return new FileName(dir, fileName);
  }
  @Override() public OriginalMapping getMappingForLine(int lineNumber, int columnIndex) {
    Preconditions.checkNotNull(characterMap, "parse() must be called first");
    if(lineNumber < 1 || lineNumber > characterMap.size() || columnIndex < 1) {
      return null;
    }
    List<LineFragment> lineFragments = characterMap.get(lineNumber - 1);
    if(lineFragments == null || lineFragments.isEmpty()) {
      return null;
    }
    int columnOffset = 0;
    LineFragment lastFragment = lineFragments.get(lineFragments.size() - 1);
    int mapId = lastFragment.valueAtColumn(lastFragment.length());
    for (LineFragment lineFragment : lineFragments) {
      int columnPosition = columnIndex - columnOffset;
      if(columnPosition <= lineFragment.length()) {
        mapId = lineFragment.valueAtColumn(columnPosition);
        break ;
      }
      columnOffset += lineFragment.length();
    }
    if(mapId < 0) {
      return null;
    }
    return getMappingFromId(mapId);
  }
  private OriginalMapping getMappingFromId(int mapID) {
    SourceFile match = binarySearch(mapID);
    if(match == null) {
      return null;
    }
    int pos = mapID - match.getStartMapId();
    return match.getOriginalMapping(pos);
  }
  private SourceFile binarySearch(int mapId) {
    int lower = 0;
    int upper = mappings.size() - 1;
    while(lower <= upper){
      int middle = lower + (upper - lower) / 2;
      SourceFile middleCompare = mappings.get(middle);
      if(mapId < middleCompare.getStartMapId()) {
        upper = middle - 1;
      }
      else 
        if(mapId < (middleCompare.getStartMapId() + middleCompare.getLength())) {
          return middleCompare;
        }
        else {
          lower = middle + 1;
        }
    }
    return null;
  }
  private int parseCharacterMap(ParseState parser, int lineCount, ImmutableList.Builder<ImmutableList<LineFragment>> characterMapBuilder) throws SourceMapParseException, JSONException {
    int maxID = -1;
    for(int i = 0; i < lineCount; ++i) {
      String currentLine = parser.readLine();
      if(currentLine.isEmpty()) {
        continue ;
      }
      ImmutableList.Builder<LineFragment> fragmentList = ImmutableList.builder();
      LineFragment myLineFragment = null;
      JSONArray charArray = new JSONArray(currentLine);
      int var_101 = charArray.length();
      int numOffsets = var_101;
      int lastID = -1;
      int startID = Integer.MIN_VALUE;
      List<Byte> currentOffsets = Lists.newArrayList();
      for(int j = 0; j < charArray.length(); ++j) {
        int mappingID = lastID;
        if(!charArray.isNull(j)) {
          mappingID = charArray.optInt(j);
          if(mappingID > maxID) {
            maxID = mappingID;
          }
        }
        if(startID == Integer.MIN_VALUE) {
          startID = mappingID;
        }
        else {
          if(mappingID - lastID > Byte.MAX_VALUE || mappingID - lastID < Byte.MIN_VALUE) {
            myLineFragment = new LineFragment(startID, Bytes.toArray(currentOffsets));
            currentOffsets.clear();
            fragmentList.add(myLineFragment);
            startID = mappingID;
          }
          else {
            currentOffsets.add((byte)(mappingID - lastID));
          }
        }
        lastID = mappingID;
      }
      if(startID != Integer.MIN_VALUE) {
        myLineFragment = new LineFragment(startID, Bytes.toArray(currentOffsets));
        fragmentList.add(myLineFragment);
      }
      characterMapBuilder.add(fragmentList.build());
    }
    return maxID;
  }
  @Override() public void parse(String contents) throws SourceMapParseException {
    ParseState parser = new ParseState(contents);
    try {
      parseInternal(parser);
    }
    catch (JSONException ex) {
      parser.fail("JSON parse exception: " + ex);
    }
  }
  private void parseFileMappings(ParseState parser, int maxID) throws SourceMapParseException, JSONException {
    Interner<String> interner = Interners.newStrongInterner();
    ImmutableList.Builder<SourceFile> mappingsBuilder = ImmutableList.builder();
    ArrayList<Byte> lineOffsets = Lists.newArrayList();
    ArrayList<Short> columns = Lists.newArrayList();
    ArrayList<String> identifiers = Lists.newArrayList();
    String currentFile = null;
    int lastLine = -1;
    int startLine = -1;
    int startMapId = -1;
    for(int mappingId = 0; mappingId <= maxID; ++mappingId) {
      String currentLine = parser.readLine();
      JSONArray mapArray = new JSONArray(currentLine);
      if(mapArray.length() < 3) {
        parser.fail("Invalid mapping array");
      }
      String myFile = mapArray.getString(0);
      int line = mapArray.getInt(1);
      if(!myFile.equals(currentFile) || (line - lastLine) > Byte.MAX_VALUE || (line - lastLine) < Byte.MIN_VALUE) {
        if(currentFile != null) {
          FileName dirFile = splitFileName(interner, currentFile);
          SourceFile.Builder builder = SourceFile.newBuilder().setDir(dirFile.dir).setFileName(dirFile.name).setStartLine(startLine).setStartMapId(startMapId).setLineOffsets(lineOffsets).setColumns(columns).setIdentifiers(identifiers);
          mappingsBuilder.add(builder.build());
        }
        currentFile = myFile;
        startLine = line;
        lastLine = line;
        startMapId = mappingId;
        columns.clear();
        lineOffsets.clear();
        identifiers.clear();
      }
      lineOffsets.add((byte)(line - lastLine));
      columns.add((short)mapArray.getInt(2));
      identifiers.add(interner.intern(mapArray.optString(3, "")));
      lastLine = line;
    }
    if(currentFile != null) {
      FileName dirFile = splitFileName(interner, currentFile);
      SourceFile.Builder builder = SourceFile.newBuilder().setDir(dirFile.dir).setFileName(dirFile.name).setStartLine(startLine).setStartMapId(startMapId).setLineOffsets(lineOffsets).setColumns(columns).setIdentifiers(identifiers);
      mappingsBuilder.add(builder.build());
    }
    mappings = mappingsBuilder.build();
  }
  private void parseInternal(ParseState parser) throws SourceMapParseException, JSONException {
    String headerCount = parser.readLine();
    Preconditions.checkArgument(headerCount.startsWith(LINEMAP_HEADER), "Expected %s", LINEMAP_HEADER);
    JSONObject countObject = new JSONObject(headerCount.substring(LINEMAP_HEADER.length()));
    if(!countObject.has("count")) {
      parser.fail("Missing \'count\'");
    }
    int lineCount = countObject.getInt("count");
    if(lineCount <= 0) {
      parser.fail("Count must be >= 1");
    }
    ImmutableList.Builder<ImmutableList<LineFragment>> characterMapBuilder = ImmutableList.builder();
    int maxId = parseCharacterMap(parser, lineCount, characterMapBuilder);
    characterMap = characterMapBuilder.build();
    parser.expectLine(FILEINFO_HEADER);
    for(int i = 0; i < lineCount; i++) {
      parser.readLine();
    }
    parser.expectLine(DEFINITION_HEADER);
    parseFileMappings(parser, maxId);
  }
  
  private class FileName  {
    final private String dir;
    final private String name;
    FileName(String directory, String name) {
      super();
      this.dir = directory;
      this.name = name;
    }
  }
  
  final private static class LineFragment  {
    final private int startIndex;
    final private byte[] offsets;
    LineFragment(int startIndex, byte[] offsets) {
      super();
      this.startIndex = startIndex;
      this.offsets = offsets;
    }
    int length() {
      return offsets.length + 1;
    }
    int valueAtColumn(int column) {
      Preconditions.checkArgument(column > 0);
      int pos = startIndex;
      for(int i = 0; i < column - 1; i++) {
        pos += offsets[i];
      }
      return pos;
    }
  }
  
  private static class ParseState  {
    final String contents;
    int currentPosition = 0;
    ParseState(String contents) {
      super();
      this.contents = contents;
    }
    String readLine() throws SourceMapParseException {
      String line = readLineOrNull();
      if(line == null) {
        fail("EOF");
      }
      return line;
    }
    String readLineOrNull() {
      if(currentPosition >= contents.length()) {
        return null;
      }
      int index = contents.indexOf('\n', currentPosition);
      if(index < 0) {
        index = contents.length();
      }
      String line = contents.substring(currentPosition, index);
      currentPosition = index + 1;
      return line;
    }
    void expectLine(String expect) throws SourceMapParseException {
      String line = readLine();
      if(!expect.equals(line)) {
        fail("Expected " + expect + " got " + line);
      }
    }
    void fail(String message) throws SourceMapParseException {
      throw new SourceMapParseException(message);
    }
  }
  
  final private static class SourceFile  {
    final String dir;
    final String fileName;
    final int startMapId;
    final int startLine;
    final byte[] lineOffsets;
    final short[] columns;
    final String[] identifiers;
    private SourceFile(String dir, String fileName, int startLine, int startMapId, byte[] lineOffsets, short[] columns, String[] identifiers) {
      super();
      this.fileName = Preconditions.checkNotNull(fileName);
      this.dir = Preconditions.checkNotNull(dir);
      this.startLine = startLine;
      this.startMapId = startMapId;
      this.lineOffsets = Preconditions.checkNotNull(lineOffsets);
      this.columns = Preconditions.checkNotNull(columns);
      this.identifiers = Preconditions.checkNotNull(identifiers);
      Preconditions.checkArgument(lineOffsets.length == columns.length && columns.length == identifiers.length);
    }
    private SourceFile(int startMapId) {
      super();
      this.startMapId = startMapId;
      this.fileName = null;
      this.dir = null;
      this.startLine = 0;
      this.lineOffsets = null;
      this.columns = null;
      this.identifiers = null;
    }
    static Builder newBuilder() {
      return new Builder();
    }
    OriginalMapping getOriginalMapping(int offset) {
      int lineNumber = this.startLine;
      for(int i = 0; i <= offset; i++) {
        lineNumber += lineOffsets[i];
      }
      OriginalMapping.Builder builder = OriginalMapping.newBuilder().setOriginalFile(dir + fileName).setLineNumber(lineNumber).setColumnPosition(columns[offset]).setIdentifier(identifiers[offset]);
      return builder.build();
    }
    int getLength() {
      return lineOffsets.length;
    }
    int getStartMapId() {
      return startMapId;
    }
    
    final static class Builder  {
      String dir;
      String fileName;
      int startMapId;
      int startLine;
      byte[] lineOffsets;
      short[] columns;
      String[] identifiers;
      Builder setColumns(List<Short> columns) {
        this.columns = Shorts.toArray(columns);
        return this;
      }
      Builder setDir(String dir) {
        this.dir = dir;
        return this;
      }
      Builder setFileName(String fileName) {
        this.fileName = fileName;
        return this;
      }
      Builder setIdentifiers(List<String> identifiers) {
        this.identifiers = identifiers.toArray(new String[0]);
        return this;
      }
      Builder setLineOffsets(List<Byte> lineOffsets) {
        this.lineOffsets = Bytes.toArray(lineOffsets);
        return this;
      }
      Builder setStartLine(int startLine) {
        this.startLine = startLine;
        return this;
      }
      Builder setStartMapId(int startMapId) {
        this.startMapId = startMapId;
        return this;
      }
      SourceFile build() {
        return new SourceFile(dir, fileName, startLine, startMapId, lineOffsets, columns, identifiers);
      }
    }
  }
}