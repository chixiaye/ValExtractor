package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.deps.DependencyInfo;
import com.google.javascript.jscomp.deps.JsFileParser;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CompilerInput implements SourceAst, DependencyInfo  {
  final private static long serialVersionUID = 1L;
  private JSModule module;
  final private InputId id;
  final private SourceAst ast;
  final private Set<String> provides = Sets.newHashSet();
  final private Set<String> requires = Sets.newHashSet();
  private boolean generatedDependencyInfoFromSource = false;
  private transient AbstractCompiler compiler;
  public CompilerInput(SourceAst ast) {
    this(ast, ast.getSourceFile().getName(), false);
  }
  public CompilerInput(SourceAst ast, InputId inputId, boolean isExtern) {
    super();
    this.ast = ast;
    this.id = inputId;
    if(ast != null && ast.getSourceFile() != null) {
      ast.getSourceFile().setIsExtern(isExtern);
    }
  }
  public CompilerInput(SourceAst ast, String inputId, boolean isExtern) {
    this(ast, new InputId(inputId), isExtern);
  }
  public CompilerInput(SourceAst ast, boolean isExtern) {
    this(ast, ast.getInputId(), isExtern);
  }
  public CompilerInput(SourceFile file) {
    this(file, false);
  }
  public CompilerInput(SourceFile file, boolean isExtern) {
    this(new JsAst(file), isExtern);
  }
  @Override() public Collection<String> getProvides() {
    checkErrorManager();
    try {
      regenerateDependencyInfoIfNecessary();
      return Collections.<String>unmodifiableSet(provides);
    }
    catch (IOException e) {
      compiler.getErrorManager().report(CheckLevel.ERROR, JSError.make(AbstractCompiler.READ_ERROR, getName()));
      return ImmutableList.<String>of();
    }
  }
  @Override() public Collection<String> getRequires() {
    checkErrorManager();
    try {
      regenerateDependencyInfoIfNecessary();
      return Collections.<String>unmodifiableSet(requires);
    }
    catch (IOException e) {
      compiler.getErrorManager().report(CheckLevel.ERROR, JSError.make(AbstractCompiler.READ_ERROR, getName()));
      return ImmutableList.<String>of();
    }
  }
  @Override() public InputId getInputId() {
    return id;
  }
  public JSModule getModule() {
    return module;
  }
  @Override() public Node getAstRoot(AbstractCompiler compiler) {
    Node root = ast.getAstRoot(compiler);
    if(root != null) {
      Preconditions.checkState(root.isScript());
      Preconditions.checkNotNull(root.getInputId());
    }
    return root;
  }
  public Region getRegion(int lineNumber) {
    return getSourceFile().getRegion(lineNumber);
  }
  public SourceAst getAst() {
    return ast;
  }
  public SourceAst getSourceAst() {
    return ast;
  }
  @Override() public SourceFile getSourceFile() {
    return ast.getSourceFile();
  }
  public String getCode() throws IOException {
    return getSourceFile().getCode();
  }
  public String getLine(int lineNumber) {
    return getSourceFile().getLine(lineNumber);
  }
  @Override() public String getName() {
    return id.getIdName();
  }
  @Override() public String getPathRelativeToClosureBase() {
    throw new UnsupportedOperationException();
  }
  @Override() public String toString() {
    return getName();
  }
  public boolean isExtern() {
    if(ast == null || ast.getSourceFile() == null) {
      return false;
    }
    return ast.getSourceFile().isExtern();
  }
  public int getLineOffset(int lineno) {
    return ast.getSourceFile().getLineOffset(lineno);
  }
  public int getNumLines() {
    return ast.getSourceFile().getNumLines();
  }
  void addProvide(String provide) {
    getProvides();
    provides.add(provide);
  }
  void addRequire(String require) {
    getRequires();
    requires.add(require);
  }
  private void checkErrorManager() {
    Preconditions.checkNotNull(compiler, "Expected setCompiler to be called first: " + this);
    Preconditions.checkNotNull(compiler.getErrorManager(), "Expected compiler to call an error manager: " + this);
  }
  @Override() public void clearAst() {
    ast.clearAst();
  }
  void overrideModule(JSModule module) {
    this.module = module;
  }
  private void regenerateDependencyInfoIfNecessary() throws IOException {
    if(!(ast instanceof JsAst)) {
      Preconditions.checkNotNull(compiler, "Expected setCompiler to be called first");
      DepsFinder finder = new DepsFinder();
      Node root = getAstRoot(compiler);
      if(root == null) {
        return ;
      }
      finder.visitTree(getAstRoot(compiler));
      provides.addAll(finder.provides);
      requires.addAll(finder.requires);
    }
    else {
      if(!generatedDependencyInfoFromSource) {
        DependencyInfo info = (new JsFileParser(compiler.getErrorManager())).setIncludeGoogBase(true).parseFile(getName(), getName(), getCode());
        provides.addAll(info.getProvides());
        requires.addAll(info.getRequires());
        generatedDependencyInfoFromSource = true;
      }
    }
  }
  public void removeRequire(String require) {
    getRequires();
    requires.remove(require);
  }
  public void setCompiler(AbstractCompiler compiler) {
    this.compiler = compiler;
  }
  void setIsExtern(boolean isExtern) {
    if(ast == null || ast.getSourceFile() == null) {
      return ;
    }
    ast.getSourceFile().setIsExtern(isExtern);
  }
  public void setModule(JSModule module) {
    JSModule var_125 = this.module;
    Preconditions.checkArgument(module == null || this.module == null || var_125 == module);
    this.module = module;
  }
  @Override() public void setSourceFile(SourceFile file) {
    ast.setSourceFile(file);
  }
  
  private static class DepsFinder  {
    final private List<String> provides = Lists.newArrayList();
    final private List<String> requires = Lists.newArrayList();
    final private CodingConvention codingConvention = new ClosureCodingConvention();
    void visitSubtree(Node n, Node parent) {
      if(n.isCall()) {
        String require = codingConvention.extractClassNameIfRequire(n, parent);
        if(require != null) {
          requires.add(require);
        }
        String provide = codingConvention.extractClassNameIfProvide(n, parent);
        if(provide != null) {
          provides.add(provide);
        }
        return ;
      }
      else 
        if(parent != null && !parent.isExprResult() && !parent.isScript()) {
          return ;
        }
      for(com.google.javascript.rhino.Node child = n.getFirstChild(); child != null; child = child.getNext()) {
        visitSubtree(child, n);
      }
    }
    void visitTree(Node n) {
      visitSubtree(n, null);
    }
  }
}