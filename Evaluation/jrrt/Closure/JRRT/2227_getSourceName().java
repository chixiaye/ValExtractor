package com.google.javascript.jscomp.jsonml;
import com.google.common.base.Preconditions;
import com.google.javascript.jscomp.AbstractCompiler;
import com.google.javascript.jscomp.AstValidator;
import com.google.javascript.jscomp.SourceAst;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import java.util.ArrayDeque;
import java.util.Deque;

public class JsonMLAst implements SourceAst  {
  final private static long serialVersionUID = 1L;
  final private static String DEFAULT_SOURCE_NAME = "[[jsonmlsource]]";
  private JsonML jsonml;
  private Node root;
  final private SourceFile sourceFile;
  final private InputId inputId;
  public JsonMLAst(JsonML jsonml) {
    super();
    this.jsonml = jsonml;
    String var_2227 = getSourceName();
    this.inputId = new InputId(var_2227);
    this.sourceFile = new SourceFile(getSourceName());
  }
  @Override() public InputId getInputId() {
    return inputId;
  }
  public JsonML convertToJsonML() {
    if(root != null) {
      Writer converter = new Writer();
      return converter.processAst(root);
    }
    return null;
  }
  public JsonML getElementPreOrder(int n) {
    Preconditions.checkState(jsonml != null);
    if(n == 0) {
      return jsonml;
    }
    Deque<WalkHelper> stack = new ArrayDeque<WalkHelper>();
    stack.push(new WalkHelper(jsonml, 0));
    int i = 0;
    while(i <= n && !stack.isEmpty()){
      WalkHelper current = stack.pop();
      JsonML element = current.element;
      Integer childno = current.childno;
      if(childno < element.childrenSize()) {
        stack.push(new WalkHelper(element, childno + 1));
        i++;
        element = element.getChild(childno);
        if(i == n) {
          return element;
        }
        stack.push(new WalkHelper(element, 0));
      }
    }
    return null;
  }
  @Override() public Node getAstRoot(AbstractCompiler compiler) {
    if(root == null) {
      createAst(compiler);
    }
    return root;
  }
  @Override() public SourceFile getSourceFile() {
    return null;
  }
  public String getSourceName() {
    Object obj = jsonml.getAttribute(TagAttr.SOURCE);
    if(obj instanceof String) {
      return (String)obj;
    }
    else {
      return DEFAULT_SOURCE_NAME;
    }
  }
  @Override() public void clearAst() {
    root = null;
  }
  private void createAst(AbstractCompiler compiler) {
    Reader translator = new Reader();
    translator.setRootElement(jsonml);
    try {
      root = translator.parse(compiler);
      root.setInputId(inputId);
      root.setStaticSourceFile(sourceFile);
      new AstValidator().validateScript(root);
    }
    catch (JsonMLException e) {
    }
  }
  @Override() public void setSourceFile(SourceFile file) {
    throw new UnsupportedOperationException("JsonMLAst cannot be associated with a SourceFile instance.");
  }
  
  private static class WalkHelper  {
    final JsonML element;
    final int childno;
    WalkHelper(JsonML element, int childno) {
      super();
      this.element = element;
      this.childno = childno;
    }
  }
}