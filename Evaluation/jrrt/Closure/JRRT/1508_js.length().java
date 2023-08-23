package com.google.javascript.jscomp;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.javascript.rhino.jstype.StaticSourceFile;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;

public class SourceFile implements StaticSourceFile, Serializable  {
  final private static long serialVersionUID = 1L;
  final private static int SOURCE_EXCERPT_REGION_LENGTH = 5;
  final private String fileName;
  private boolean isExternFile = false;
  private String originalPath = null;
  private int[] lineOffsets = null;
  private String code = null;
  public SourceFile(String fileName) {
    super();
    if(fileName == null || fileName.isEmpty()) {
      throw new IllegalArgumentException("a source must have a name");
    }
    this.fileName = fileName;
  }
  public static Builder builder() {
    return new Builder();
  }
  public Reader getCodeReader() throws IOException {
    return new StringReader(getCode());
  }
  public Region getRegion(int lineNumber) {
    String js = "";
    try {
      js = getCode();
    }
    catch (IOException e) {
      return null;
    }
    int pos = 0;
    int startLine = Math.max(1, lineNumber - (SOURCE_EXCERPT_REGION_LENGTH + 1) / 2 + 1);
    for(int n = 1; n < startLine; n++) {
      int nextpos = js.indexOf('\n', pos);
      if(nextpos == -1) {
        break ;
      }
      pos = nextpos + 1;
    }
    int end = pos;
    int endLine = startLine;
    for(int n = 0; n < SOURCE_EXCERPT_REGION_LENGTH; n++, endLine++) {
      end = js.indexOf('\n', end);
      if(end == -1) {
        break ;
      }
      end++;
    }
    if(lineNumber >= endLine) {
      return null;
    }
    if(end == -1) {
      int last = js.length() - 1;
      if(js.charAt(last) == '\n') {
        return new SimpleRegion(startLine, endLine, js.substring(pos, last));
      }
      else {
        return new SimpleRegion(startLine, endLine, js.substring(pos));
      }
    }
    else {
      return new SimpleRegion(startLine, endLine, js.substring(pos, end));
    }
  }
  public static SourceFile fromCode(String fileName, String code) {
    return builder().buildFromCode(fileName, code);
  }
  public static SourceFile fromCode(String fileName, String originalPath, String code) {
    return builder().withOriginalPath(originalPath).buildFromCode(fileName, code);
  }
  public static SourceFile fromFile(File file) {
    return builder().buildFromFile(file);
  }
  public static SourceFile fromFile(File file, Charset c) {
    return builder().withCharset(c).buildFromFile(file);
  }
  public static SourceFile fromFile(String fileName) {
    return builder().buildFromFile(fileName);
  }
  public static SourceFile fromFile(String fileName, Charset c) {
    return builder().withCharset(c).buildFromFile(fileName);
  }
  public static SourceFile fromGenerator(String fileName, Generator generator) {
    return builder().buildFromGenerator(fileName, generator);
  }
  public static SourceFile fromInputStream(String fileName, InputStream s) throws IOException {
    return builder().buildFromInputStream(fileName, s);
  }
  public static SourceFile fromInputStream(String fileName, String originalPath, InputStream s) throws IOException {
    return builder().withOriginalPath(originalPath).buildFromInputStream(fileName, s);
  }
  public static SourceFile fromReader(String fileName, Reader r) throws IOException {
    return builder().buildFromReader(fileName, r);
  }
  public String getCode() throws IOException {
    return code;
  }
  @VisibleForTesting() String getCodeNoCache() {
    return code;
  }
  public String getLine(int lineNumber) {
    findLineOffsets();
    if(lineNumber > lineOffsets.length) {
      return null;
    }
    if(lineNumber < 1) {
      lineNumber = 1;
    }
    int pos = lineOffsets[lineNumber - 1];
    String js = "";
    try {
      js = getCode();
    }
    catch (IOException e) {
      return null;
    }
    if(js.indexOf('\n', pos) == -1) {
      int var_1508 = js.length();
      if(pos >= var_1508) {
        return null;
      }
      else {
        return js.substring(pos, js.length());
      }
    }
    else {
      return js.substring(pos, js.indexOf('\n', pos));
    }
  }
  @Override() public String getName() {
    return fileName;
  }
  public String getOriginalPath() {
    return originalPath != null ? originalPath : fileName;
  }
  @Override() public String toString() {
    return fileName;
  }
  boolean hasSourceInMemory() {
    return code != null;
  }
  @Override() public boolean isExtern() {
    return isExternFile;
  }
  @Override() public int getColumnOfOffset(int offset) {
    int line = getLineOfOffset(offset);
    return offset - lineOffsets[line - 1];
  }
  @Override() public int getLineOfOffset(int offset) {
    findLineOffsets();
    int search = Arrays.binarySearch(lineOffsets, offset);
    if(search >= 0) {
      return search + 1;
    }
    else {
      int insertionPoint = -1 * (search + 1);
      return Math.min(insertionPoint - 1, lineOffsets.length - 1) + 1;
    }
  }
  @Override() public int getLineOffset(int lineno) {
    findLineOffsets();
    if(lineno < 1 || lineno > lineOffsets.length) {
      throw new IllegalArgumentException("Expected line number between 1 and " + lineOffsets.length + "\nActual: " + lineno);
    }
    return lineOffsets[lineno - 1];
  }
  int getNumLines() {
    findLineOffsets();
    return lineOffsets.length;
  }
  public void clearCachedSource() {
  }
  private void findLineOffsets() {
    if(lineOffsets != null) {
      return ;
    }
    try {
      String[] sourceLines = getCode().split("\n");
      lineOffsets = new int[sourceLines.length];
      for(int ii = 1; ii < sourceLines.length; ++ii) {
        lineOffsets[ii] = lineOffsets[ii - 1] + sourceLines[ii - 1].length() + 1;
      }
    }
    catch (IOException e) {
      lineOffsets = new int[1];
      lineOffsets[0] = 0;
    }
  }
  private void setCode(String sourceCode) {
    code = sourceCode;
  }
  void setIsExtern(boolean newVal) {
    isExternFile = newVal;
  }
  public void setOriginalPath(String originalPath) {
    this.originalPath = originalPath;
  }
  
  public static class Builder  {
    private Charset charset = Charsets.UTF_8;
    private String originalPath = null;
    public Builder() {
      super();
    }
    public Builder withCharset(Charset charset) {
      this.charset = charset;
      return this;
    }
    public Builder withOriginalPath(String originalPath) {
      this.originalPath = originalPath;
      return this;
    }
    public SourceFile buildFromCode(String fileName, String code) {
      return new Preloaded(fileName, originalPath, code);
    }
    public SourceFile buildFromFile(File file) {
      return new OnDisk(file, originalPath, charset);
    }
    public SourceFile buildFromFile(String fileName) {
      return buildFromFile(new File(fileName));
    }
    public SourceFile buildFromGenerator(String fileName, Generator generator) {
      return new Generated(fileName, originalPath, generator);
    }
    public SourceFile buildFromInputStream(String fileName, InputStream s) throws IOException {
      return buildFromCode(fileName, CharStreams.toString(new InputStreamReader(s, charset)));
    }
    public SourceFile buildFromReader(String fileName, Reader r) throws IOException {
      return buildFromCode(fileName, CharStreams.toString(r));
    }
  }
  
  static class Generated extends SourceFile  {
    final private static long serialVersionUID = 1L;
    final private Generator generator;
    Generated(String fileName, String originalPath, Generator generator) {
      super(fileName);
      super.setOriginalPath(originalPath);
      this.generator = generator;
    }
    @Override() public synchronized String getCode() throws IOException {
      String cachedCode = super.getCode();
      if(cachedCode == null) {
        cachedCode = generator.getCode();
        super.setCode(cachedCode);
      }
      return cachedCode;
    }
    @Override() public void clearCachedSource() {
      super.setCode(null);
    }
  }
  
  public interface Generator  {
    public String getCode();
  }
  
  static class OnDisk extends SourceFile  {
    final private static long serialVersionUID = 1L;
    final private File file;
    private String inputCharset = Charsets.UTF_8.name();
    OnDisk(File file, String originalPath, Charset c) {
      super(file.getPath());
      this.file = file;
      super.setOriginalPath(originalPath);
      if(c != null) {
        this.setCharset(c);
      }
    }
    public Charset getCharset() {
      return Charset.forName(inputCharset);
    }
    @Override() public Reader getCodeReader() throws IOException {
      if(hasSourceInMemory()) {
        return super.getCodeReader();
      }
      else {
        return new FileReader(file);
      }
    }
    @Override() public synchronized String getCode() throws IOException {
      String cachedCode = super.getCode();
      if(cachedCode == null) {
        cachedCode = Files.toString(file, this.getCharset());
        super.setCode(cachedCode);
      }
      return cachedCode;
    }
    @Override() public void clearCachedSource() {
      super.setCode(null);
    }
    public void setCharset(Charset c) {
      inputCharset = c.name();
    }
  }
  
  static class Preloaded extends SourceFile  {
    final private static long serialVersionUID = 1L;
    Preloaded(String fileName, String originalPath, String code) {
      super(fileName);
      super.setOriginalPath(originalPath);
      super.setCode(code);
    }
  }
}