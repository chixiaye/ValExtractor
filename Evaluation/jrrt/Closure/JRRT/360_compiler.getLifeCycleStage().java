package com.google.javascript.jscomp;
import com.google.javascript.rhino.Node;

class SanityCheck implements CompilerPass  {
  final static DiagnosticType CANNOT_PARSE_GENERATED_CODE = DiagnosticType.error("JSC_CANNOT_PARSE_GENERATED_CODE", "Internal compiler error. Cannot parse generated code: {0}");
  final static DiagnosticType GENERATED_BAD_CODE = DiagnosticType.error("JSC_GENERATED_BAD_CODE", "Internal compiler error. Generated bad code." + "----------------------------------------\n" + "Expected:\n{0}\n" + "----------------------------------------\n" + "Actual:\n{1}");
  final private AbstractCompiler compiler;
  final private AstValidator astValidator = new AstValidator();
  SanityCheck(AbstractCompiler compiler) {
    super();
    this.compiler = compiler;
  }
  private Node sanityCheckCodeGeneration(Node root) {
    if(compiler.hasHaltingErrors()) {
      return null;
    }
    String source = compiler.toSource(root);
    Node root2 = compiler.parseSyntheticCode(source);
    if(compiler.hasHaltingErrors()) {
      compiler.report(JSError.make(CANNOT_PARSE_GENERATED_CODE, Strings.truncateAtMaxLength(source, 100, true)));
      throw new IllegalStateException("Sanity Check failed");
    }
    String source2 = compiler.toSource(root2);
    if(!source.equals(source2)) {
      compiler.report(JSError.make(GENERATED_BAD_CODE, source, source2));
      throw new IllegalStateException("Sanity Check failed");
    }
    return root2;
  }
  @Override() public void process(Node externs, Node root) {
    sanityCheckAst(externs, root);
    sanityCheckNormalization(externs, root);
    sanityCheckCodeGeneration(root);
    sanityCheckVars(externs, root);
  }
  private void sanityCheckAst(Node externs, Node root) {
    astValidator.validateCodeRoot(externs);
    astValidator.validateCodeRoot(root);
  }
  private void sanityCheckNormalization(Node externs, Node root) {
    CodeChangeHandler handler = new CodeChangeHandler.ForbiddenChange();
    compiler.addChangeHandler(handler);
    new PrepareAst(compiler, true).process(null, root);
    AbstractCompiler.LifeCycleStage var_360 = compiler.getLifeCycleStage();
    if(var_360.isNormalized()) {
      (new Normalize(compiler, true)).process(externs, root);
      if(compiler.getLifeCycleStage().isNormalizedUnobfuscated()) {
        boolean checkUserDeclarations = true;
        CompilerPass pass = new Normalize.VerifyConstants(compiler, checkUserDeclarations);
        pass.process(externs, root);
      }
    }
    compiler.removeChangeHandler(handler);
  }
  private void sanityCheckVars(Node externs, Node root) {
    if(compiler.getLifeCycleStage().isNormalized()) {
      (new VarCheck(compiler, true)).process(externs, root);
    }
  }
}