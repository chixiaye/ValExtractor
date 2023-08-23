package com.google.javascript.jscomp.ant;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.DiagnosticGroup;
import com.google.javascript.jscomp.DiagnosticGroups;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.MessageFormatter;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.WarningLevel;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.Path;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

final public class CompileTask extends Task  {
  private CompilerOptions.LanguageMode languageIn;
  private WarningLevel warningLevel;
  private boolean debugOptions;
  private String encoding = "UTF-8";
  private String outputEncoding = "UTF-8";
  private CompilationLevel compilationLevel;
  private boolean customExternsOnly;
  private boolean manageDependencies;
  private boolean prettyPrint;
  private boolean printInputDelimiter;
  private boolean generateExports;
  private boolean replaceProperties;
  private boolean forceRecompile;
  private String replacePropertiesPrefix;
  private File outputFile;
  final private List<Parameter> defineParams;
  final private List<FileList> externFileLists;
  final private List<FileList> sourceFileLists;
  final private List<Path> sourcePaths;
  final private List<Warning> warnings;
  public CompileTask() {
    super();
    this.languageIn = CompilerOptions.LanguageMode.ECMASCRIPT3;
    this.warningLevel = WarningLevel.DEFAULT;
    this.debugOptions = false;
    this.compilationLevel = CompilationLevel.SIMPLE_OPTIMIZATIONS;
    this.customExternsOnly = false;
    this.manageDependencies = false;
    this.prettyPrint = false;
    this.printInputDelimiter = false;
    this.generateExports = false;
    this.replaceProperties = false;
    this.forceRecompile = false;
    this.replacePropertiesPrefix = "closure.define.";
    this.defineParams = Lists.newLinkedList();
    this.externFileLists = Lists.newLinkedList();
    java.util.LinkedList<FileList> var_2117 = Lists.newLinkedList();
    this.sourceFileLists = var_2117;
    this.sourcePaths = Lists.newLinkedList();
    this.warnings = Lists.newLinkedList();
  }
  private Compiler createCompiler(CompilerOptions options) {
    Compiler compiler = new Compiler();
    MessageFormatter formatter = options.errorFormat.toFormatter(compiler, false);
    AntErrorManager errorManager = new AntErrorManager(formatter, this);
    compiler.setErrorManager(errorManager);
    return compiler;
  }
  private CompilerOptions createCompilerOptions() {
    CompilerOptions options = new CompilerOptions();
    this.compilationLevel.setOptionsForCompilationLevel(options);
    if(this.debugOptions) {
      this.compilationLevel.setDebugOptionsForCompilationLevel(options);
    }
    options.prettyPrint = this.prettyPrint;
    options.printInputDelimiter = this.printInputDelimiter;
    options.generateExports = this.generateExports;
    options.setLanguageIn(this.languageIn);
    this.warningLevel.setOptionsForWarningLevel(options);
    options.setManageClosureDependencies(manageDependencies);
    if(replaceProperties) {
      convertPropertiesMap(options);
    }
    convertDefineParameters(options);
    for (Warning warning : warnings) {
      CheckLevel level = warning.getLevel();
      String groupName = warning.getGroup();
      DiagnosticGroup group = new DiagnosticGroups().forName(groupName);
      if(group == null) {
        throw new BuildException("Unrecognized \'warning\' option value (" + groupName + ")");
      }
      options.setWarningLevel(group, level);
    }
    return options;
  }
  private List<SourceFile> findExternFiles() {
    List<SourceFile> files = Lists.newLinkedList();
    if(!this.customExternsOnly) {
      files.addAll(getDefaultExterns());
    }
    for (FileList list : this.externFileLists) {
      files.addAll(findJavaScriptFiles(list));
    }
    return files;
  }
  private List<SourceFile> findJavaScriptFiles(FileList fileList) {
    List<SourceFile> files = Lists.newLinkedList();
    File baseDir = fileList.getDir(getProject());
    for (String included : fileList.getFiles(getProject())) {
      files.add(SourceFile.fromFile(new File(baseDir, included), Charset.forName(encoding)));
    }
    return files;
  }
  private List<SourceFile> findJavaScriptFiles(Path path) {
    List<SourceFile> files = Lists.newArrayList();
    for (String included : path.list()) {
      files.add(SourceFile.fromFile(new File(included), Charset.forName(encoding)));
    }
    return files;
  }
  private List<SourceFile> findSourceFiles() {
    List<SourceFile> files = Lists.newLinkedList();
    for (FileList list : this.sourceFileLists) {
      files.addAll(findJavaScriptFiles(list));
    }
    for (Path list : this.sourcePaths) {
      files.addAll(findJavaScriptFiles(list));
    }
    return files;
  }
  private List<SourceFile> getDefaultExterns() {
    try {
      return CommandLineRunner.getDefaultExterns();
    }
    catch (IOException e) {
      throw new BuildException(e);
    }
  }
  public Parameter createDefine() {
    Parameter param = new Parameter();
    defineParams.add(param);
    return param;
  }
  private boolean isStale() {
    long lastRun = outputFile.lastModified();
    long sourcesLastModified = Math.max(getLastModifiedTime(this.sourceFileLists), getLastModifiedTime(this.sourcePaths));
    long externsLastModified = getLastModifiedTime(this.externFileLists);
    return lastRun <= sourcesLastModified || lastRun <= externsLastModified;
  }
  private boolean setDefine(CompilerOptions options, String key, Object value) {
    boolean success = false;
    if(value instanceof String) {
      final boolean isTrue = "true".equals(value);
      final boolean isFalse = "false".equals(value);
      if(isTrue || isFalse) {
        options.setDefineToBooleanLiteral(key, isTrue);
      }
      else {
        try {
          double dblTemp = Double.parseDouble((String)value);
          options.setDefineToDoubleLiteral(key, dblTemp);
        }
        catch (NumberFormatException nfe) {
          options.setDefineToStringLiteral(key, (String)value);
        }
      }
      success = true;
    }
    else 
      if(value instanceof Boolean) {
        options.setDefineToBooleanLiteral(key, (Boolean)value);
        success = true;
      }
      else 
        if(value instanceof Integer) {
          options.setDefineToNumberLiteral(key, (Integer)value);
          success = true;
        }
        else 
          if(value instanceof Double) {
            options.setDefineToDoubleLiteral(key, (Double)value);
            success = true;
          }
    return success;
  }
  private long getLastModifiedTime(File file) {
    long fileLastModified = file.lastModified();
    if(fileLastModified == 0) {
      fileLastModified = new Date().getTime();
    }
    return fileLastModified;
  }
  private long getLastModifiedTime(List<?> fileLists) {
    long lastModified = 0;
    for (Object entry : fileLists) {
      if(entry instanceof FileList) {
        FileList list = (FileList)entry;
        for (String fileName : list.getFiles(this.getProject())) {
          File path = list.getDir(this.getProject());
          File file = new File(path, fileName);
          lastModified = Math.max(getLastModifiedTime(file), lastModified);
        }
      }
      else 
        if(entry instanceof Path) {
          Path path = (Path)entry;
          for (String src : path.list()) {
            File file = new File(src);
            lastModified = Math.max(getLastModifiedTime(file), lastModified);
          }
        }
    }
    return lastModified;
  }
  public void addExterns(FileList list) {
    this.externFileLists.add(list);
  }
  public void addPath(Path list) {
    this.sourcePaths.add(list);
  }
  public void addSources(FileList list) {
    this.sourceFileLists.add(list);
  }
  public void addWarning(Warning warning) {
    this.warnings.add(warning);
  }
  private void convertDefineParameters(CompilerOptions options) {
    for (Parameter p : defineParams) {
      String key = p.getName();
      Object value = p.getValue();
      if(!setDefine(options, key, value)) {
        log("Unexpected @define value for name=" + key + "; value=" + value);
      }
    }
  }
  private void convertPropertiesMap(CompilerOptions options) {
    @SuppressWarnings(value = {"unchecked", }) Map<String, Object> props = getProject().getProperties();
    for (Map.Entry<String, Object> entry : props.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if(key.startsWith(replacePropertiesPrefix)) {
        key = key.substring(replacePropertiesPrefix.length());
        if(!setDefine(options, key, value)) {
          log("Unexpected property value for key=" + key + "; value=" + value);
        }
      }
    }
  }
  @Override() public void execute() {
    if(this.outputFile == null) {
      throw new BuildException("outputFile attribute must be set");
    }
    Compiler.setLoggingLevel(Level.OFF);
    CompilerOptions options = createCompilerOptions();
    Compiler compiler = createCompiler(options);
    List<SourceFile> externs = findExternFiles();
    List<SourceFile> sources = findSourceFiles();
    if(isStale() || forceRecompile) {
      log("Compiling " + sources.size() + " file(s) with " + externs.size() + " extern(s)");
      Result result = compiler.compile(externs, sources, options);
      if(result.success) {
        writeResult(compiler.toSource());
      }
      else {
        throw new BuildException("Compilation failed.");
      }
    }
    else {
      log("None of the files changed. Compilation skipped.");
    }
  }
  public void setCompilationLevel(String value) {
    if("simple".equalsIgnoreCase(value)) {
      this.compilationLevel = CompilationLevel.SIMPLE_OPTIMIZATIONS;
    }
    else 
      if("advanced".equalsIgnoreCase(value)) {
        this.compilationLevel = CompilationLevel.ADVANCED_OPTIMIZATIONS;
      }
      else 
        if("whitespace".equalsIgnoreCase(value)) {
          this.compilationLevel = CompilationLevel.WHITESPACE_ONLY;
        }
        else {
          throw new BuildException("Unrecognized \'compilation\' option value (" + value + ")");
        }
  }
  public void setCustomExternsOnly(boolean value) {
    this.customExternsOnly = value;
  }
  public void setDebug(boolean value) {
    this.debugOptions = value;
  }
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }
  public void setForceRecompile(boolean forceRecompile) {
    this.forceRecompile = forceRecompile;
  }
  public void setGenerateExports(boolean generateExports) {
    this.generateExports = generateExports;
  }
  public void setLanguageIn(String value) {
    if(value.equals("ECMASCRIPT5_STRICT") || value.equals("ES5_STRICT")) {
      this.languageIn = CompilerOptions.LanguageMode.ECMASCRIPT5_STRICT;
    }
    else 
      if(value.equals("ECMASCRIPT5") || value.equals("ES5")) {
        this.languageIn = CompilerOptions.LanguageMode.ECMASCRIPT5;
      }
      else 
        if(value.equals("ECMASCRIPT3") || value.equals("ES3")) {
          this.languageIn = CompilerOptions.LanguageMode.ECMASCRIPT3;
        }
        else {
          throw new BuildException("Unrecognized \'languageIn\' option value (" + value + ")");
        }
  }
  public void setManageDependencies(boolean value) {
    this.manageDependencies = value;
  }
  public void setOutput(File value) {
    this.outputFile = value;
  }
  public void setOutputEncoding(String outputEncoding) {
    this.outputEncoding = outputEncoding;
  }
  public void setPrettyPrint(boolean pretty) {
    this.prettyPrint = pretty;
  }
  public void setPrintInputDelimiter(boolean print) {
    this.printInputDelimiter = print;
  }
  public void setReplaceProperties(boolean value) {
    this.replaceProperties = value;
  }
  public void setReplacePropertiesPrefix(String value) {
    this.replacePropertiesPrefix = value;
  }
  public void setWarning(String value) {
    if("default".equalsIgnoreCase(value)) {
      this.warningLevel = WarningLevel.DEFAULT;
    }
    else 
      if("quiet".equalsIgnoreCase(value)) {
        this.warningLevel = WarningLevel.QUIET;
      }
      else 
        if("verbose".equalsIgnoreCase(value)) {
          this.warningLevel = WarningLevel.VERBOSE;
        }
        else {
          throw new BuildException("Unrecognized \'warning\' option value (" + value + ")");
        }
  }
  private void writeResult(String source) {
    if(this.outputFile.getParentFile().mkdirs()) {
      log("Created missing parent directory " + this.outputFile.getParentFile(), Project.MSG_DEBUG);
    }
    try {
      OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(this.outputFile), outputEncoding);
      out.append(source);
      out.flush();
      out.close();
    }
    catch (IOException e) {
      throw new BuildException(e);
    }
    log("Compiled JavaScript written to " + this.outputFile.getAbsolutePath(), Project.MSG_DEBUG);
  }
}