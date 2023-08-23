package com.google.javascript.jscomp.deps;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.DiagnosticType;
import com.google.javascript.jscomp.ErrorManager;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.SourceFile;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class DepsGenerator  {
  private static Logger logger = Logger.getLogger(DepsGenerator.class.getName());
  final private Collection<SourceFile> srcs;
  final private Collection<SourceFile> deps;
  final private String closurePathAbs;
  final private InclusionStrategy mergeStrategy;
  final ErrorManager errorManager;
  final static DiagnosticType SAME_FILE_WARNING = DiagnosticType.warning("DEPS_SAME_FILE", "Namespace \"{0}\" is both required and provided in the same file.");
  final static DiagnosticType NEVER_PROVIDED_ERROR = DiagnosticType.error("DEPS_NEVER_PROVIDED", "Namespace \"{0}\" is required but never provided.");
  final static DiagnosticType DUPE_PROVIDES_WARNING = DiagnosticType.warning("DEPS_DUPE_PROVIDES", "Multiple calls to goog.provide(\"{0}\")");
  final static DiagnosticType MULTIPLE_PROVIDES_ERROR = DiagnosticType.error("DEPS_DUPE_PROVIDES", "Namespace \"{0}\" is already provided in other file {1}");
  final static DiagnosticType DUPE_REQUIRE_WARNING = DiagnosticType.warning("DEPS_DUPE_REQUIRES", "Namespace \"{0}\" is required multiple times");
  final static DiagnosticType NO_DEPS_WARNING = DiagnosticType.warning("DEPS_NO_DEPS", "No dependencies found in file");
  public DepsGenerator(Collection<SourceFile> deps, Collection<SourceFile> srcs, InclusionStrategy mergeStrategy, String closurePathAbs, ErrorManager errorManager) {
    super();
    this.deps = deps;
    this.srcs = srcs;
    this.mergeStrategy = mergeStrategy;
    this.closurePathAbs = closurePathAbs;
    this.errorManager = errorManager;
  }
  protected DepsFileParser createDepsFileParser() {
    DepsFileParser depsParser = new DepsFileParser(errorManager);
    depsParser.setShortcutMode(true);
    return depsParser;
  }
  static List<SourceFile> createSourceFilesFromPaths(String ... paths) {
    return createSourceFilesFromPaths(Arrays.asList(paths));
  }
  static List<SourceFile> createSourceFilesFromPaths(Collection<String> paths) {
    List<SourceFile> files = Lists.newArrayList();
    for (String path : paths) {
      files.add(SourceFile.fromFile(path));
    }
    return files;
  }
  private Map<String, DependencyInfo> parseDepsFiles() throws IOException {
    DepsFileParser depsParser = createDepsFileParser();
    Map<String, DependencyInfo> depsFiles = Maps.newHashMap();
    for (SourceFile file : deps) {
      if(!shouldSkipDepsFile(file)) {
        List<DependencyInfo> depInfos = depsParser.parseFileReader(file.getName(), file.getCodeReader());
        if(depInfos.isEmpty()) {
          reportNoDepsInDepsFile(file.getName());
        }
        else {
          for (DependencyInfo info : depInfos) {
            depsFiles.put(info.getPathRelativeToClosureBase(), info);
          }
        }
      }
    }
    for (SourceFile src : srcs) {
      String var_2159 = src.getName();
      if((new File(var_2159)).exists() && !shouldSkipDepsFile(src)) {
        List<DependencyInfo> srcInfos = depsParser.parseFileReader(src.getName(), src.getCodeReader());
        for (DependencyInfo info : srcInfos) {
          depsFiles.put(info.getPathRelativeToClosureBase(), info);
        }
      }
    }
    return depsFiles;
  }
  private Map<String, DependencyInfo> parseSources(Set<String> preparsedFiles) throws IOException {
    Map<String, DependencyInfo> parsedFiles = Maps.newHashMap();
    JsFileParser jsParser = new JsFileParser(errorManager);
    for (SourceFile file : srcs) {
      String closureRelativePath = PathUtil.makeRelative(closurePathAbs, PathUtil.makeAbsolute(file.getName()));
      logger.fine("Closure-relative path: " + closureRelativePath);
      if(InclusionStrategy.WHEN_IN_SRCS == mergeStrategy || !preparsedFiles.contains(closureRelativePath)) {
        DependencyInfo depInfo = jsParser.parseFile(file.getName(), closureRelativePath, file.getCode());
        file.clearCachedSource();
        parsedFiles.put(closureRelativePath, depInfo);
      }
    }
    return parsedFiles;
  }
  public String computeDependencyCalls() throws IOException {
    Map<String, DependencyInfo> depsFiles = parseDepsFiles();
    logger.fine("preparsedFiles: " + depsFiles);
    Map<String, DependencyInfo> jsFiles = parseSources(depsFiles.keySet());
    if(errorManager.getErrorCount() > 0) {
      return null;
    }
    cleanUpDuplicatedFiles(depsFiles, jsFiles);
    validateDependencies(depsFiles.values(), jsFiles.values());
    if(errorManager.getErrorCount() > 0) {
      return null;
    }
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    writeDepsContent(depsFiles, jsFiles, new PrintStream(output));
    return new String(output.toByteArray());
  }
  protected String formatPathToDepsFile(String path) {
    return path;
  }
  protected boolean shouldSkipDepsFile(SourceFile file) {
    return false;
  }
  private void addToProvideMap(Iterable<DependencyInfo> depInfos, Map<String, DependencyInfo> providesMap) {
    for (DependencyInfo depInfo : depInfos) {
      for (String provide : depInfo.getProvides()) {
        DependencyInfo prevValue = providesMap.put(provide, depInfo);
        if(prevValue != null) {
          reportDuplicateProvide(provide, prevValue, depInfo);
        }
      }
    }
  }
  protected void cleanUpDuplicatedFiles(Map<String, DependencyInfo> depsFiles, Map<String, DependencyInfo> jsFiles) {
    Set<String> depsPathsCopy = Sets.newHashSet(depsFiles.keySet());
    for (String path : depsPathsCopy) {
      if(mergeStrategy != InclusionStrategy.WHEN_IN_SRCS) {
        jsFiles.remove(path);
      }
    }
    for (String path : jsFiles.keySet()) {
      depsFiles.remove(path);
    }
  }
  private void reportDuplicateProvide(String namespace, DependencyInfo firstDep, DependencyInfo secondDep) {
    if(firstDep == secondDep) {
      errorManager.report(CheckLevel.WARNING, JSError.make(firstDep.getName(), -1, -1, DUPE_PROVIDES_WARNING, namespace));
    }
    else {
      errorManager.report(CheckLevel.ERROR, JSError.make(secondDep.getName(), -1, -1, MULTIPLE_PROVIDES_ERROR, namespace, firstDep.getName()));
    }
  }
  private void reportDuplicateRequire(String namespace, DependencyInfo depInfo) {
    errorManager.report(CheckLevel.WARNING, JSError.make(depInfo.getName(), -1, -1, DUPE_REQUIRE_WARNING, namespace));
  }
  private void reportNoDepsInDepsFile(String filePath) {
    errorManager.report(CheckLevel.WARNING, JSError.make(filePath, -1, -1, NO_DEPS_WARNING));
  }
  private void reportSameFile(String namespace, DependencyInfo depInfo) {
    errorManager.report(CheckLevel.WARNING, JSError.make(depInfo.getName(), -1, -1, SAME_FILE_WARNING, namespace));
  }
  private void reportUndefinedNamespace(String namespace, DependencyInfo depInfo) {
    errorManager.report(CheckLevel.ERROR, JSError.make(depInfo.getName(), -1, -1, NEVER_PROVIDED_ERROR, namespace));
  }
  private void validateDependencies(Iterable<DependencyInfo> preparsedFileDepedencies, Iterable<DependencyInfo> parsedFileDependencies) {
    Map<String, DependencyInfo> providesMap = Maps.newHashMap();
    addToProvideMap(preparsedFileDepedencies, providesMap);
    addToProvideMap(parsedFileDependencies, providesMap);
    for (DependencyInfo depInfo : parsedFileDependencies) {
      List<String> requires = Lists.newArrayList(depInfo.getRequires());
      for(int i = 0, l = requires.size(); i < l; ++i) {
        String namespace = requires.get(i);
        if(requires.subList(i + 1, l).contains(namespace)) {
          reportDuplicateRequire(namespace, depInfo);
        }
        DependencyInfo provider = providesMap.get(namespace);
        if(provider == null) {
          reportUndefinedNamespace(namespace, depInfo);
        }
        else 
          if(provider == depInfo) {
            reportSameFile(namespace, depInfo);
          }
      }
    }
  }
  private void writeDepInfos(PrintStream out, Collection<DependencyInfo> depInfos) throws IOException {
    for (DependencyInfo depInfo : depInfos) {
      Collection<String> provides = depInfo.getProvides();
      Collection<String> requires = depInfo.getRequires();
      out.print("goog.addDependency(\'" + depInfo.getPathRelativeToClosureBase() + "\', ");
      writeJsArray(out, provides);
      out.print(", ");
      writeJsArray(out, requires);
      out.println(");");
    }
  }
  private void writeDepsContent(Map<String, DependencyInfo> depsFiles, Map<String, DependencyInfo> jsFiles, PrintStream out) throws IOException {
    writeDepInfos(out, jsFiles.values());
    if(mergeStrategy == InclusionStrategy.ALWAYS) {
      Multimap<String, DependencyInfo> infosIndex = Multimaps.index(depsFiles.values(), new Function<DependencyInfo, String>() {
          @Override() public String apply(DependencyInfo from) {
            return from.getName();
          }
      });
      for (String depsPath : infosIndex.keySet()) {
        String path = formatPathToDepsFile(depsPath);
        out.println("\n// Included from: " + path);
        writeDepInfos(out, infosIndex.get(depsPath));
      }
    }
  }
  private static void writeJsArray(PrintStream out, Collection<String> values) {
    if(values.isEmpty()) {
      out.print("[]");
    }
    else {
      out.print("[\'");
      out.print(Joiner.on("\', \'").join(values));
      out.print("\']");
    }
  }
  public static enum InclusionStrategy {
    ALWAYS(),

    WHEN_IN_SRCS(),

    DO_NOT_DUPLICATE(),

  ;
  private InclusionStrategy() {
  }
  }
}