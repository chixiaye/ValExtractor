package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.regex.Pattern;

public class ProcessCommonJSModules implements CompilerPass  {
  final private static String MODULE_SLASH = "/";
  final public static String DEFAULT_FILENAME_PREFIX = "." + MODULE_SLASH;
  final private static String MODULE_NAME_SEPARATOR = "\\$";
  final private static String MODULE_NAME_PREFIX = "module$";
  final private AbstractCompiler compiler;
  final private String filenamePrefix;
  final private boolean reportDependencies;
  private JSModule module;
  ProcessCommonJSModules(AbstractCompiler compiler, String filenamePrefix) {
    this(compiler, filenamePrefix, true);
  }
  ProcessCommonJSModules(AbstractCompiler compiler, String filenamePrefix, boolean reportDependencies) {
    super();
    this.compiler = compiler;
    this.filenamePrefix = filenamePrefix.endsWith(MODULE_SLASH) ? filenamePrefix : filenamePrefix + MODULE_SLASH;
    this.reportDependencies = reportDependencies;
  }
  JSModule getModule() {
    return module;
  }
  String guessCJSModuleName(String filename) {
    return toModuleName(normalizeSourceName(filename));
  }
  private String normalizeSourceName(String filename) {
    filename = filename.replace("\\", "/");
    if(filename.indexOf(filenamePrefix) == 0) {
      filename = filename.substring(filenamePrefix.length());
    }
    return filename;
  }
  public static String toModuleName(String filename) {
    return MODULE_NAME_PREFIX + filename.replaceAll("^\\." + Pattern.quote(MODULE_SLASH), "").replaceAll(Pattern.quote(MODULE_SLASH), MODULE_NAME_SEPARATOR).replaceAll("\\.js$", "").replaceAll("-", "_");
  }
  public static String toModuleName(String requiredFilename, String currentFilename) {
    requiredFilename = requiredFilename.replaceAll("\\.js$", "");
    currentFilename = currentFilename.replaceAll("\\.js$", "");
    if(requiredFilename.startsWith("." + MODULE_SLASH) || requiredFilename.startsWith(".." + MODULE_SLASH)) {
      try {
        requiredFilename = (new URI(currentFilename)).resolve(new URI(requiredFilename)).toString();
      }
      catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }
    return toModuleName(requiredFilename);
  }
  @Override() public void process(Node externs, Node root) {
    NodeTraversal.traverse(compiler, root, new ProcessCommonJsModulesCallback());
  }
  
  private class ProcessCommonJsModulesCallback extends AbstractPostOrderCallback  {
    private int scriptNodeCount = 0;
    private Set<String> modulesWithExports = Sets.newHashSet();
    private Node getCurrentScriptNode(Node n) {
      while(true){
        if(n.isScript()) {
          return n;
        }
        n = n.getParent();
      }
    }
    private void emitOptionalModuleExportsOverride(Node script, String moduleName) {
      if(!modulesWithExports.contains(moduleName)) {
        return ;
      }
      Node moduleExportsProp = IR.getprop(IR.name(moduleName), IR.string("module$exports"));
      script.addChildToBack(IR.ifNode(moduleExportsProp, IR.block(IR.exprResult(IR.assign(IR.name(moduleName), moduleExportsProp.cloneTree())))).copyInformationFromForTree(script));
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(n.isCall() && n.getChildCount() == 2 && "require".equals(n.getFirstChild().getQualifiedName()) && n.getChildAtIndex(1).isString()) {
        visitRequireCall(t, n, parent);
      }
      if(n.isScript()) {
        scriptNodeCount++;
        visitScript(t, n);
      }
      if(n.isGetProp() && "module.exports".equals(n.getQualifiedName())) {
        visitModuleExports(n);
      }
    }
    private void visitModuleExports(Node prop) {
      String moduleName = guessCJSModuleName(prop.getSourceFileName());
      Node module = prop.getChildAtIndex(0);
      int var_1930 = Node.ORIGINALNAME_PROP;
      module.putProp(var_1930, "module");
      module.setString(moduleName);
      Node exports = prop.getChildAtIndex(1);
      exports.putProp(Node.ORIGINALNAME_PROP, "exports");
      exports.setString("module$exports");
      modulesWithExports.add(moduleName);
    }
    private void visitRequireCall(NodeTraversal t, Node require, Node parent) {
      String moduleName = toModuleName(require.getChildAtIndex(1).getString(), normalizeSourceName(t.getSourceName()));
      Node moduleRef = IR.name(moduleName).srcref(require);
      parent.replaceChild(require, moduleRef);
      Node script = getCurrentScriptNode(parent);
      if(reportDependencies) {
        t.getInput().addRequire(moduleName);
      }
      script.addChildToFront(IR.exprResult(IR.call(IR.getprop(IR.name("goog"), IR.string("require")), IR.string(moduleName))).copyInformationFromForTree(require));
      compiler.reportCodeChange();
    }
    private void visitScript(NodeTraversal t, Node script) {
      Preconditions.checkArgument(scriptNodeCount == 1, "ProcessCommonJSModules supports only one invocation per " + "CompilerInput / script node");
      String moduleName = guessCJSModuleName(script.getSourceFileName());
      script.addChildToFront(IR.var(IR.name(moduleName), IR.objectlit()).copyInformationFromForTree(script));
      if(reportDependencies) {
        CompilerInput ci = t.getInput();
        ci.addProvide(moduleName);
        JSModule m = new JSModule(moduleName);
        m.addAndOverrideModule(ci);
        module = m;
      }
      script.addChildToFront(IR.exprResult(IR.call(IR.getprop(IR.name("goog"), IR.string("provide")), IR.string(moduleName))).copyInformationFromForTree(script));
      emitOptionalModuleExportsOverride(script, moduleName);
      NodeTraversal.traverse(compiler, script, new SuffixVarsCallback(moduleName));
      compiler.reportCodeChange();
    }
  }
  
  private class SuffixVarsCallback extends AbstractPostOrderCallback  {
    final private static String EXPORTS = "exports";
    final private String suffix;
    SuffixVarsCallback(String suffix) {
      super();
      this.suffix = suffix;
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      if(n.isName()) {
        String name = n.getString();
        if(suffix.equals(name)) {
          return ;
        }
        if(EXPORTS.equals(name)) {
          n.setString(suffix);
          n.putProp(Node.ORIGINALNAME_PROP, EXPORTS);
        }
        else {
          Scope.Var var = t.getScope().getVar(name);
          if(var != null && var.isGlobal()) {
            n.setString(name + "$$" + suffix);
            n.putProp(Node.ORIGINALNAME_PROP, name);
          }
        }
      }
    }
  }
}