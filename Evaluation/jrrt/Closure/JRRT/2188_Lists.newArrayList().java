package com.google.javascript.jscomp.jsonml;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerInput;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerOptions.Reach;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.JSModule;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.VariableRenamingPolicy;
import java.util.ArrayList;

public class SecureCompiler  {
  final private static String COMPILATION_UNCOMPLETED_MSG = "No compilation has been completed yet.";
  final private static String COMPILATION_UNSUCCESSFUL_MSG = "The last compilation was not successful.";
  final private static String COMPILATION_ALREADY_COMPLETED_MSG = "This instance has already compiled one source.";
  private Compiler compiler;
  private CompilerOptions options;
  private JsonMLAst sourceAst;
  private Report report;
  public SecureCompiler() {
    super();
    compiler = new Compiler();
    options = getSecureCompilerOptions();
  }
  private CompilerOptions getSecureCompilerOptions() {
    CompilerOptions options = new CompilerOptions();
    options.variableRenaming = VariableRenamingPolicy.OFF;
    options.setInlineVariables(Reach.LOCAL_ONLY);
    options.inlineLocalFunctions = true;
    options.checkGlobalThisLevel = CheckLevel.OFF;
    options.coalesceVariableNames = true;
    options.deadAssignmentElimination = true;
    options.collapseVariableDeclarations = true;
    options.convertToDottedProperties = true;
    options.labelRenaming = true;
    options.removeDeadCode = true;
    options.optimizeArgumentsArray = true;
    options.removeUnusedVars = false;
    options.removeUnusedLocalVars = true;
    return options;
  }
  public JsonML getJsonML() {
    Preconditions.checkState(report != null, COMPILATION_UNCOMPLETED_MSG);
    Preconditions.checkState(report.success, COMPILATION_UNSUCCESSFUL_MSG);
    return sourceAst.convertToJsonML();
  }
  Report generateReport(Result result) {
    if(result == null) {
      return null;
    }
    ArrayList<JsonMLError> var_2188 = Lists.newArrayList();
    ArrayList<JsonMLError> errors = var_2188;
    for (JSError error : result.errors) {
      errors.add(JsonMLError.make(error, sourceAst));
    }
    ArrayList<JsonMLError> warnings = Lists.newArrayList();
    for (JSError warning : result.warnings) {
      warnings.add(JsonMLError.make(warning, sourceAst));
    }
    return new Report(errors.toArray(new JsonMLError[0]), warnings.toArray(new JsonMLError[0]));
  }
  public Report getReport() {
    Preconditions.checkState(report != null, COMPILATION_UNCOMPLETED_MSG);
    return report;
  }
  public String getString() {
    Preconditions.checkState(report != null, COMPILATION_UNCOMPLETED_MSG);
    Preconditions.checkState(report.success, COMPILATION_UNSUCCESSFUL_MSG);
    return compiler.toSource();
  }
  public void compile(JsonML source) {
    if(report != null) {
      throw new IllegalStateException(COMPILATION_ALREADY_COMPLETED_MSG);
    }
    sourceAst = new JsonMLAst(source);
    CompilerInput input = new CompilerInput(sourceAst, "[[jsonmlsource]]", false);
    JSModule module = new JSModule("[[jsonmlmodule]]");
    module.add(input);
    Result result = compiler.compileModules(ImmutableList.<SourceFile>of(), ImmutableList.of(module), options);
    report = generateReport(result);
  }
  public void enableFoldConstant() {
    options.foldConstants = true;
  }
  
  public class Report  {
    final private boolean success;
    final private JsonMLError[] errors;
    final private JsonMLError[] warnings;
    private Report(JsonMLError[] errors, JsonMLError[] warnings) {
      super();
      this.success = errors.length == 0;
      this.errors = errors;
      this.warnings = warnings;
    }
    public JsonMLError[] getErrors() {
      return errors;
    }
    public JsonMLError[] getWarnings() {
      return warnings;
    }
    public boolean isSuccessful() {
      return success;
    }
  }
}