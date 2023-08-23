package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.javascript.jscomp.parsing.ParserRunner;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import java.io.IOException;
import java.util.logging.Logger;

public class JsAst implements SourceAst  {
  final private static Logger logger_ = Logger.getLogger(JsAst.class.getName());
  final private static long serialVersionUID = 1L;
  private transient InputId inputId;
  private transient SourceFile sourceFile;
  private String fileName;
  private Node root;
  public JsAst(SourceFile sourceFile) {
    super();
    String var_932 = sourceFile.getName();
    this.inputId = new InputId(var_932);
    this.sourceFile = sourceFile;
    this.fileName = sourceFile.getName();
  }
  @Override() public InputId getInputId() {
    return inputId;
  }
  @Override() public Node getAstRoot(AbstractCompiler compiler) {
    if(root == null) {
      parse(compiler);
      root.setInputId(inputId);
    }
    return root;
  }
  @Override() public SourceFile getSourceFile() {
    return sourceFile;
  }
  @Override() public void clearAst() {
    root = null;
    sourceFile.clearCachedSource();
  }
  private void parse(AbstractCompiler compiler) {
    try {
      logger_.fine("Parsing: " + sourceFile.getName());
      ParserRunner.ParseResult result = ParserRunner.parse(sourceFile, sourceFile.getCode(), compiler.getParserConfig(), compiler.getDefaultErrorReporter(), logger_);
      root = result.ast;
      compiler.setOldParseTree(sourceFile.getName(), result.oldAst);
    }
    catch (IOException e) {
      compiler.report(JSError.make(AbstractCompiler.READ_ERROR, sourceFile.getName()));
    }
    if(root == null || compiler.hasHaltingErrors()) {
      root = IR.script();
    }
    else {
      compiler.prepareAst(root);
    }
    root.setStaticSourceFile(sourceFile);
  }
  @Override() public void setSourceFile(SourceFile file) {
    Preconditions.checkState(fileName.equals(file.getName()));
    sourceFile = file;
  }
}