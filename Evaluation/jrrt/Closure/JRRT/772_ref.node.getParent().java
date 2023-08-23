package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.GlobalNamespace.Name;
import com.google.javascript.jscomp.GlobalNamespace.Ref;
import com.google.javascript.jscomp.NodeTraversal.Callback;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ProcessDefines implements CompilerPass  {
  final private static Set<String> KNOWN_DEFINES = Sets.newHashSet("COMPILED");
  final private AbstractCompiler compiler;
  final private Map<String, Node> dominantReplacements;
  private GlobalNamespace namespace = null;
  final static DiagnosticType UNKNOWN_DEFINE_WARNING = DiagnosticType.warning("JSC_UNKNOWN_DEFINE_WARNING", "unknown @define variable {0}");
  final static DiagnosticType INVALID_DEFINE_TYPE_ERROR = DiagnosticType.error("JSC_INVALID_DEFINE_TYPE_ERROR", "@define tag only permits literal types");
  final static DiagnosticType INVALID_DEFINE_INIT_ERROR = DiagnosticType.error("JSC_INVALID_DEFINE_INIT_ERROR", "illegal initialization of @define variable {0}");
  final static DiagnosticType NON_GLOBAL_DEFINE_INIT_ERROR = DiagnosticType.error("JSC_NON_GLOBAL_DEFINE_INIT_ERROR", "@define variable {0} assignment must be global");
  final static DiagnosticType DEFINE_NOT_ASSIGNABLE_ERROR = DiagnosticType.error("JSC_DEFINE_NOT_ASSIGNABLE_ERROR", "@define variable {0} cannot be reassigned due to code at {1}.");
  final private static MessageFormat REASON_DEFINE_NOT_ASSIGNABLE = new MessageFormat("line {0} of {1}");
  ProcessDefines(AbstractCompiler compiler, Map<String, Node> replacements) {
    super();
    this.compiler = compiler;
    dominantReplacements = replacements;
  }
  private Map<String, DefineInfo> collectDefines(Node root, GlobalNamespace namespace) {
    List<Name> allDefines = Lists.newArrayList();
    for (Name name : namespace.getNameIndex().values()) {
      Ref decl = name.getDeclaration();
      if(name.docInfo != null && name.docInfo.isDefine()) {
        if(isValidDefineType(name.docInfo.getType())) {
          allDefines.add(name);
        }
        else {
          JSError error = JSError.make(decl.getSourceName(), decl.node, INVALID_DEFINE_TYPE_ERROR);
          compiler.report(error);
        }
      }
      else {
        for (Ref ref : name.getRefs()) {
          if(ref == decl) {
            continue ;
          }
          Node n = ref.node;
          Node parent = ref.node.getParent();
          JSDocInfo info = n.getJSDocInfo();
          if(info == null && parent.isVar() && parent.hasOneChild()) {
            info = parent.getJSDocInfo();
          }
          if(info != null && info.isDefine()) {
            allDefines.add(name);
            break ;
          }
        }
      }
    }
    CollectDefines pass = new CollectDefines(compiler, allDefines);
    NodeTraversal.traverse(compiler, root, pass);
    return pass.getAllDefines();
  }
  ProcessDefines injectNamespace(GlobalNamespace namespace) {
    this.namespace = namespace;
    return this;
  }
  private static String format(MessageFormat format, Object ... params) {
    return format.format(params);
  }
  private boolean isValidDefineType(JSTypeExpression expression) {
    JSType type = expression.evaluate(null, compiler.getTypeRegistry());
    return !type.isUnknownType() && type.isSubtype(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_STRING_BOOLEAN));
  }
  private void overrideDefines(Map<String, DefineInfo> allDefines) {
    boolean changed = false;
    for (Map.Entry<String, DefineInfo> def : allDefines.entrySet()) {
      String defineName = def.getKey();
      DefineInfo info = def.getValue();
      Node inputValue = dominantReplacements.get(defineName);
      Node finalValue = inputValue != null ? inputValue : info.getLastValue();
      if(finalValue != info.initialValue) {
        info.initialValueParent.replaceChild(info.initialValue, finalValue.cloneTree());
        compiler.addToDebugLog("Overriding @define variable " + defineName);
        changed = changed || finalValue.getType() != info.initialValue.getType() || !finalValue.isEquivalentTo(info.initialValue);
      }
    }
    if(changed) {
      compiler.reportCodeChange();
    }
    Set<String> unusedReplacements = dominantReplacements.keySet();
    unusedReplacements.removeAll(allDefines.keySet());
    unusedReplacements.removeAll(KNOWN_DEFINES);
    for (String unknownDefine : unusedReplacements) {
      compiler.report(JSError.make(UNKNOWN_DEFINE_WARNING, unknownDefine));
    }
  }
  @Override() public void process(Node externs, Node root) {
    if(namespace == null) {
      namespace = new GlobalNamespace(compiler, root);
    }
    overrideDefines(collectDefines(root, namespace));
  }
  
  final private static class CollectDefines implements Callback  {
    final private AbstractCompiler compiler;
    final private Map<String, DefineInfo> assignableDefines;
    final private Map<String, DefineInfo> allDefines;
    final private Map<Node, RefInfo> allRefInfo;
    private Node lvalueToRemoveLater = null;
    final private Deque<Integer> assignAllowed;
    CollectDefines(AbstractCompiler compiler, List<Name> listOfDefines) {
      super();
      this.compiler = compiler;
      this.allDefines = Maps.newHashMap();
      assignableDefines = Maps.newHashMap();
      assignAllowed = new ArrayDeque<Integer>();
      assignAllowed.push(1);
      allRefInfo = Maps.newHashMap();
      for (Name name : listOfDefines) {
        Ref decl = name.getDeclaration();
        if(decl != null) {
          allRefInfo.put(decl.node, new RefInfo(decl, name));
        }
        for (Ref ref : name.getRefs()) {
          if(ref == decl) {
            continue ;
          }
          if(ref.getTwin() == null || !ref.getTwin().isSet()) {
            allRefInfo.put(ref.node, new RefInfo(ref, name));
          }
        }
      }
    }
    Map<String, DefineInfo> getAllDefines() {
      return allDefines;
    }
    private static Node getValueParent(Ref ref) {
      Node var_772 = ref.node.getParent();
      return var_772 != null && ref.node.getParent().isVar() ? ref.node : ref.node.getParent();
    }
    private boolean isAssignAllowed() {
      return assignAllowed.element() == 1;
    }
    private boolean processDefineAssignment(NodeTraversal t, String name, Node value, Node valueParent) {
      if(value == null || !NodeUtil.isValidDefineValue(value, allDefines.keySet())) {
        compiler.report(t.makeError(value, INVALID_DEFINE_INIT_ERROR, name));
      }
      else 
        if(!isAssignAllowed()) {
          compiler.report(t.makeError(valueParent, NON_GLOBAL_DEFINE_INIT_ERROR, name));
        }
        else {
          DefineInfo info = allDefines.get(name);
          if(info == null) {
            info = new DefineInfo(value, valueParent);
            allDefines.put(name, info);
            assignableDefines.put(name, info);
          }
          else 
            if(info.recordAssignment(value)) {
              return true;
            }
            else {
              compiler.report(t.makeError(valueParent, DEFINE_NOT_ASSIGNABLE_ERROR, name, info.getReasonWhyNotAssignable()));
            }
        }
      return false;
    }
    @Override() public boolean shouldTraverse(NodeTraversal nodeTraversal, Node n, Node parent) {
      updateAssignAllowedStack(n, true);
      return true;
    }
    private void setDefineInfoNotAssignable(DefineInfo info, NodeTraversal t) {
      info.setNotAssignable(format(REASON_DEFINE_NOT_ASSIGNABLE, t.getLineNumber(), t.getSourceName()));
    }
    private void updateAssignAllowedStack(Node n, boolean entering) {
      switch (n.getType()){
        case Token.CASE:
        case Token.FOR:
        case Token.FUNCTION:
        case Token.HOOK:
        case Token.IF:
        case Token.SWITCH:
        case Token.WHILE:
        if(entering) {
          assignAllowed.push(0);
        }
        else {
          assignAllowed.remove();
        }
        break ;
      }
    }
    @Override() public void visit(NodeTraversal t, Node n, Node parent) {
      RefInfo refInfo = allRefInfo.get(n);
      if(refInfo != null) {
        Ref ref = refInfo.ref;
        Name name = refInfo.name;
        String fullName = name.getFullName();
        switch (ref.type){
          case SET_FROM_GLOBAL:
          case SET_FROM_LOCAL:
          Node valParent = getValueParent(ref);
          Node val = valParent.getLastChild();
          if(valParent.isAssign() && name.isSimpleName() && name.getDeclaration() == ref) {
            compiler.report(t.makeError(val, INVALID_DEFINE_INIT_ERROR, fullName));
          }
          else 
            if(processDefineAssignment(t, fullName, val, valParent)) {
              refInfo.name.removeRef(ref);
              lvalueToRemoveLater = valParent;
            }
          break ;
          default:
          if(t.inGlobalScope()) {
            DefineInfo info = assignableDefines.get(fullName);
            if(info != null) {
              setDefineInfoNotAssignable(info, t);
              assignableDefines.remove(fullName);
            }
          }
          break ;
        }
      }
      if(!t.inGlobalScope() && n.getJSDocInfo() != null && n.getJSDocInfo().isDefine()) {
        compiler.report(t.makeError(n, NON_GLOBAL_DEFINE_INIT_ERROR, ""));
      }
      if(lvalueToRemoveLater == n) {
        lvalueToRemoveLater = null;
        if(n.isAssign()) {
          Node last = n.getLastChild();
          n.removeChild(last);
          parent.replaceChild(n, last);
        }
        else {
          Preconditions.checkState(n.isName());
          n.removeChild(n.getFirstChild());
        }
        compiler.reportCodeChange();
      }
      if(n.isCall()) {
        if(t.inGlobalScope()) {
          for (DefineInfo info : assignableDefines.values()) {
            setDefineInfoNotAssignable(info, t);
          }
          assignableDefines.clear();
        }
      }
      updateAssignAllowedStack(n, false);
    }
    
    private static class RefInfo  {
      final Ref ref;
      final Name name;
      RefInfo(Ref ref, Name name) {
        super();
        this.ref = ref;
        this.name = name;
      }
    }
  }
  
  final private static class DefineInfo  {
    final public Node initialValueParent;
    final public Node initialValue;
    private Node lastValue;
    private boolean isAssignable;
    private String reasonNotAssignable;
    public DefineInfo(Node initialValue, Node initialValueParent) {
      super();
      this.initialValueParent = initialValueParent;
      this.initialValue = initialValue;
      lastValue = initialValue;
      isAssignable = true;
    }
    public Node getLastValue() {
      return lastValue;
    }
    public String getReasonWhyNotAssignable() {
      return reasonNotAssignable;
    }
    public boolean recordAssignment(Node value) {
      lastValue = value;
      return isAssignable;
    }
    public void setNotAssignable(String reason) {
      isAssignable = false;
      reasonNotAssignable = reason;
    }
  }
}