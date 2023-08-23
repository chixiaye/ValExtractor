package com.google.javascript.jscomp.jsonml;
import com.google.common.base.Preconditions;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.Iterator;
import java.util.Set;

public class Writer  {
  public JsonML processAst(Node root) {
    Preconditions.checkNotNull(root);
    Preconditions.checkArgument(root.getType() == Token.BLOCK || root.getType() == Token.SCRIPT);
    JsonML rootElement = new JsonML(TagType.BlockStmt);
    if(root.getType() == Token.SCRIPT) {
      processNode(root, rootElement);
      return rootElement.getChild(0);
    }
    else {
      Node child = root.getFirstChild();
      while(child != null){
        processNode(child, rootElement);
        child = child.getNext();
      }
      return rootElement.getChild(0);
    }
  }
  private void processArrayLiteral(Node node, JsonML currentParent) {
    JsonML element = new JsonML(TagType.ArrayExpr);
    currentParent.appendChild(element);
    Iterator<Node> it = node.children().iterator();
    while(it.hasNext()){
      processNode(it.next(), element);
    }
  }
  private void processAssignExpr(Node node, JsonML currentParent, String op) {
    processTwoArgExpr(node, currentParent, TagType.AssignExpr, op);
  }
  private void processBinaryExpr(Node node, JsonML currentParent, String op) {
    processTwoArgExpr(node, currentParent, TagType.BinaryExpr, op);
  }
  private void processBlock(Node node, JsonML currentParent) {
    JsonML element = new JsonML(TagType.BlockStmt);
    if(currentParent != null) {
      currentParent.appendChild(element);
    }
    processDirectives(node, element);
    for (Node child : node.children()) {
      processNode(child, element);
    }
  }
  private void processCall(Node node, JsonML currentParent) {
    Iterator<Node> it = node.children().iterator();
    Node child = it.next();
    JsonML element;
    switch (child.getType()){
      case Token.GETPROP:
      case Token.GETELEM:
      element = new JsonML(TagType.InvokeExpr);
      element.setAttribute(TagAttr.OP, child.getType() == Token.GETPROP ? "." : "[]");
      currentParent.appendChild(element);
      Node grandchild = child.getFirstChild();
      processNode(grandchild, element);
      processNode(grandchild.getNext(), element);
      break ;
      case Token.NAME:
      if(child.getString().equals("eval")) {
        element = new JsonML(TagType.EvalExpr);
      }
      else {
        element = new JsonML(TagType.IdExpr);
        element.setAttribute(TagAttr.NAME, child.getString());
        element = new JsonML(TagType.CallExpr, element);
      }
      currentParent.appendChild(element);
      break ;
      default:
      element = new JsonML(TagType.CallExpr);
      currentParent.appendChild(element);
      processNode(child, element);
      break ;
    }
    while(it.hasNext()){
      processNode(it.next(), element);
    }
  }
  private void processCase(Node node, JsonML currentParent, TagType type) {
    JsonML element = new JsonML(type);
    currentParent.appendChild(element);
    Node child = node.getFirstChild();
    if(type == TagType.Case) {
      processNode(child, element);
      Node var_2209 = child.getNext();
      child = var_2209;
    }
    Preconditions.checkNotNull(child);
    Preconditions.checkState(child.getType() == Token.BLOCK);
    child = child.getFirstChild();
    while(child != null){
      processNode(child, element);
      child = child.getNext();
    }
  }
  private void processCatch(Node node, JsonML currentParent) {
    JsonML element = new JsonML(TagType.CatchClause);
    currentParent.appendChild(element);
    Node child = node.getFirstChild();
    JsonML patt = new JsonML(TagType.IdPatt);
    patt.setAttribute(TagAttr.NAME, child.getString());
    element.appendChild(patt);
    child = child.getNext();
    processNode(child, element);
  }
  private void processChildren(Node node, JsonML currentParent) {
    for (Node child : node.children()) {
      processNode(child, currentParent);
    }
  }
  private void processDirectives(Node node, JsonML currentParent) {
    Set<String> directives = node.getDirectives();
    if(directives == null) {
      return ;
    }
    for (String directive : directives) {
      JsonML element = new JsonML(TagType.PrologueDecl);
      element.setAttribute(TagAttr.DIRECTIVE, directive);
      element.setAttribute(TagAttr.VALUE, directive);
      currentParent.appendChild(element);
    }
  }
  private void processEmpty(Node node, JsonML currentParent) {
    if(currentParent.getType() == TagType.ArrayExpr) {
      currentParent.appendChild(new JsonML(TagType.Empty));
    }
    else {
      currentParent.appendChild(new JsonML(TagType.EmptyStmt));
    }
  }
  private void processExprResult(Node node, JsonML currentParent) {
    processNode(node.getFirstChild(), currentParent);
  }
  private void processForLoop(Node node, JsonML currentParent) {
    if(NodeUtil.isForIn(node)) {
      processLoop(node, currentParent, TagType.ForInStmt);
    }
    else {
      processLoop(node, currentParent, TagType.ForStmt);
    }
  }
  private void processFunction(Node node, JsonML currentParent) {
    JsonML element;
    if(NodeUtil.isFunctionDeclaration(node)) {
      element = new JsonML(TagType.FunctionDecl);
    }
    else {
      element = new JsonML(TagType.FunctionExpr);
    }
    currentParent.appendChild(element);
    Node child = node.getFirstChild();
    String name = child.getString();
    if(!name.equals("")) {
      JsonML nameElement = new JsonML(TagType.IdPatt);
      nameElement.setAttribute(TagAttr.NAME, name);
      element.appendChild(nameElement);
    }
    else {
      element.appendChild(new JsonML(TagType.Empty));
    }
    child = child.getNext();
    JsonML params = new JsonML(TagType.ParamDecl);
    element.appendChild(params);
    Iterator<Node> it = child.children().iterator();
    while(it.hasNext()){
      JsonML param = new JsonML(TagType.IdPatt);
      Node nameNode = it.next();
      param.setAttribute(TagAttr.NAME, nameNode.getString());
      params.appendChild(param);
    }
    child = child.getNext();
    processDirectives(child, element);
    it = child.children().iterator();
    while(it.hasNext()){
      processNode(it.next(), element);
    }
  }
  private void processHook(Node node, JsonML currentParent) {
    JsonML element = new JsonML(TagType.ConditionalExpr);
    currentParent.appendChild(element);
    processChildren(node, element);
  }
  private void processIf(Node node, JsonML currentParent) {
    JsonML element = new JsonML(TagType.IfStmt);
    currentParent.appendChild(element);
    Iterator<Node> it = node.children().iterator();
    while(it.hasNext()){
      processNode(it.next(), element);
    }
    int childCount = node.getChildCount();
    Preconditions.checkState(childCount >= 2);
    if(childCount < 3) {
      element.appendChild(new JsonML(TagType.EmptyStmt));
    }
  }
  private void processIncrDecrExpr(Node node, JsonML currentParent, String op) {
    JsonML element = new JsonML(TagType.CountExpr);
    currentParent.appendChild(element);
    if(op.equals("++")) {
      element.setAttribute(TagAttr.OP, "++");
    }
    else {
      element.setAttribute(TagAttr.OP, "--");
    }
    if(node.getIntProp(Node.INCRDECR_PROP) == 1) {
      element.setAttribute(TagAttr.IS_PREFIX, false);
    }
    else {
      element.setAttribute(TagAttr.IS_PREFIX, true);
    }
    processNode(node.getFirstChild(), element);
  }
  private void processJmp(Node node, JsonML currentParent, TagType type) {
    JsonML element = new JsonML(type);
    currentParent.appendChild(element);
    Node child = node.getFirstChild();
    if(child != null) {
      element.setAttribute(TagAttr.LABEL, child.getString());
    }
  }
  private void processLabel(Node node, JsonML currentParent) {
    JsonML element = new JsonML(TagType.LabelledStmt);
    currentParent.appendChild(element);
    Node child = node.getFirstChild();
    element.setAttribute(TagAttr.LABEL, child.getString());
    child = child.getNext();
    processNode(child, element);
  }
  private void processLiteral(Node node, JsonML currentParent) {
    JsonML element = new JsonML(TagType.LiteralExpr);
    switch (node.getType()){
      case Token.NUMBER:
      element.setAttribute(TagAttr.TYPE, "number");
      element.setAttribute(TagAttr.VALUE, node.getDouble());
      break ;
      case Token.STRING:
      element.setAttribute(TagAttr.TYPE, "string");
      element.setAttribute(TagAttr.VALUE, node.getString());
      break ;
      case Token.NULL:
      element.setAttribute(TagAttr.TYPE, "null");
      element.setAttribute(TagAttr.VALUE, null);
      break ;
      case Token.TRUE:
      element.setAttribute(TagAttr.TYPE, "boolean");
      element.setAttribute(TagAttr.VALUE, true);
      break ;
      case Token.FALSE:
      element.setAttribute(TagAttr.TYPE, "boolean");
      element.setAttribute(TagAttr.VALUE, false);
      break ;
      default:
      throw new IllegalArgumentException("Illegal type of node.");
    }
    currentParent.appendChild(element);
  }
  private void processLogicalExpr(Node node, JsonML currentParent, String op) {
    if(op.equals("||")) {
      processTwoArgExpr(node, currentParent, TagType.LogicalOrExpr);
    }
    else 
      if(op.endsWith("&&")) {
        processTwoArgExpr(node, currentParent, TagType.LogicalAndExpr);
      }
      else {
        throw new IllegalArgumentException("Unsupported value of op argument.");
      }
  }
  private void processLoop(Node node, JsonML currentParent, TagType type) {
    JsonML element = new JsonML(type);
    currentParent.appendChild(element);
    processChildren(node, element);
  }
  private void processMemberExpr(Node node, JsonML currentParent, String op) {
    JsonML element = new JsonML(TagType.MemberExpr);
    element.setAttribute(TagAttr.OP, op);
    currentParent.appendChild(element);
    Node child = node.getFirstChild();
    processNode(child, element);
    processNode(child.getNext(), element);
  }
  private void processName(Node node, JsonML currentParent) {
    Preconditions.checkState(!node.hasChildren());
    JsonML element = new JsonML(TagType.IdExpr);
    element.setAttribute(TagAttr.NAME, node.getString());
    currentParent.appendChild(element);
  }
  private void processNew(Node node, JsonML currentParent, TagType type) {
    JsonML element = new JsonML(type);
    currentParent.appendChild(element);
    processChildren(node, element);
  }
  private void processNode(Node node, JsonML currentParent) {
    switch (node.getType()){
      case Token.RETURN:
      processReturn(node, currentParent);
      break ;
      case Token.BITOR:
      processBinaryExpr(node, currentParent, "|");
      break ;
      case Token.BITXOR:
      processBinaryExpr(node, currentParent, "^");
      break ;
      case Token.BITAND:
      processBinaryExpr(node, currentParent, "&");
      break ;
      case Token.EQ:
      processBinaryExpr(node, currentParent, "==");
      break ;
      case Token.NE:
      processBinaryExpr(node, currentParent, "!=");
      break ;
      case Token.LT:
      processBinaryExpr(node, currentParent, "<");
      break ;
      case Token.LE:
      processBinaryExpr(node, currentParent, "<=");
      break ;
      case Token.GT:
      processBinaryExpr(node, currentParent, ">");
      break ;
      case Token.GE:
      processBinaryExpr(node, currentParent, ">=");
      break ;
      case Token.LSH:
      processBinaryExpr(node, currentParent, "<<");
      break ;
      case Token.RSH:
      processBinaryExpr(node, currentParent, ">>");
      break ;
      case Token.URSH:
      processBinaryExpr(node, currentParent, ">>>");
      break ;
      case Token.ADD:
      processBinaryExpr(node, currentParent, "+");
      break ;
      case Token.SUB:
      processBinaryExpr(node, currentParent, "-");
      break ;
      case Token.MUL:
      processBinaryExpr(node, currentParent, "*");
      break ;
      case Token.DIV:
      processBinaryExpr(node, currentParent, "/");
      break ;
      case Token.MOD:
      processBinaryExpr(node, currentParent, "%");
      break ;
      case Token.NOT:
      processUnaryExpr(node, currentParent, "!");
      break ;
      case Token.BITNOT:
      processUnaryExpr(node, currentParent, "~");
      break ;
      case Token.POS:
      processUnaryExpr(node, currentParent, "+");
      break ;
      case Token.NEG:
      processUnaryExpr(node, currentParent, "-");
      break ;
      case Token.NEW:
      processNew(node, currentParent, TagType.NewExpr);
      break ;
      case Token.DELPROP:
      processOneArgExpr(node, currentParent, TagType.DeleteExpr);
      break ;
      case Token.TYPEOF:
      processOneArgExpr(node, currentParent, TagType.TypeofExpr);
      break ;
      case Token.GETPROP:
      processMemberExpr(node, currentParent, ".");
      break ;
      case Token.GETELEM:
      processMemberExpr(node, currentParent, "[]");
      break ;
      case Token.CALL:
      processCall(node, currentParent);
      break ;
      case Token.NAME:
      processName(node, currentParent);
      break ;
      case Token.NUMBER:
      case Token.STRING:
      case Token.NULL:
      case Token.FALSE:
      case Token.TRUE:
      processLiteral(node, currentParent);
      break ;
      case Token.THIS:
      processThis(node, currentParent);
      break ;
      case Token.SHEQ:
      processBinaryExpr(node, currentParent, "===");
      break ;
      case Token.SHNE:
      processBinaryExpr(node, currentParent, "!==");
      break ;
      case Token.REGEXP:
      processRegExp(node, currentParent);
      break ;
      case Token.THROW:
      processThrow(node, currentParent);
      break ;
      case Token.IN:
      processBinaryExpr(node, currentParent, "in");
      break ;
      case Token.INSTANCEOF:
      processBinaryExpr(node, currentParent, "instanceof");
      break ;
      case Token.ARRAYLIT:
      processArrayLiteral(node, currentParent);
      break ;
      case Token.OBJECTLIT:
      processObjectLiteral(node, currentParent);
      break ;
      case Token.TRY:
      processTry(node, currentParent);
      break ;
      case Token.COMMA:
      processBinaryExpr(node, currentParent, ",");
      break ;
      case Token.ASSIGN:
      processAssignExpr(node, currentParent, "=");
      break ;
      case Token.ASSIGN_BITOR:
      processAssignExpr(node, currentParent, "|=");
      break ;
      case Token.ASSIGN_BITXOR:
      processAssignExpr(node, currentParent, "^=");
      break ;
      case Token.ASSIGN_BITAND:
      processAssignExpr(node, currentParent, "&=");
      break ;
      case Token.ASSIGN_LSH:
      processAssignExpr(node, currentParent, "<<=");
      break ;
      case Token.ASSIGN_RSH:
      processAssignExpr(node, currentParent, ">>=");
      break ;
      case Token.ASSIGN_URSH:
      processAssignExpr(node, currentParent, ">>>=");
      break ;
      case Token.ASSIGN_ADD:
      processAssignExpr(node, currentParent, "+=");
      break ;
      case Token.ASSIGN_SUB:
      processAssignExpr(node, currentParent, "-=");
      break ;
      case Token.ASSIGN_MUL:
      processAssignExpr(node, currentParent, "*=");
      break ;
      case Token.ASSIGN_DIV:
      processAssignExpr(node, currentParent, "/=");
      break ;
      case Token.ASSIGN_MOD:
      processAssignExpr(node, currentParent, "%=");
      break ;
      case Token.HOOK:
      processHook(node, currentParent);
      break ;
      case Token.OR:
      processLogicalExpr(node, currentParent, "||");
      break ;
      case Token.AND:
      processLogicalExpr(node, currentParent, "&&");
      break ;
      case Token.INC:
      processIncrDecrExpr(node, currentParent, "++");
      break ;
      case Token.DEC:
      processIncrDecrExpr(node, currentParent, "--");
      break ;
      case Token.FUNCTION:
      processFunction(node, currentParent);
      break ;
      case Token.IF:
      processIf(node, currentParent);
      break ;
      case Token.SWITCH:
      processSwitch(node, currentParent);
      break ;
      case Token.CASE:
      processCase(node, currentParent, TagType.Case);
      break ;
      case Token.DEFAULT_CASE:
      processCase(node, currentParent, TagType.DefaultCase);
      break ;
      case Token.WHILE:
      processLoop(node, currentParent, TagType.WhileStmt);
      break ;
      case Token.DO:
      processLoop(node, currentParent, TagType.DoWhileStmt);
      break ;
      case Token.FOR:
      processForLoop(node, currentParent);
      break ;
      case Token.BREAK:
      processJmp(node, currentParent, TagType.BreakStmt);
      break ;
      case Token.CONTINUE:
      processJmp(node, currentParent, TagType.ContinueStmt);
      break ;
      case Token.VAR:
      processVar(node, currentParent);
      break ;
      case Token.WITH:
      processWith(node, currentParent);
      break ;
      case Token.CATCH:
      processCatch(node, currentParent);
      break ;
      case Token.VOID:
      processUnaryExpr(node, currentParent, "void");
      break ;
      case Token.EMPTY:
      processEmpty(node, currentParent);
      break ;
      case Token.BLOCK:
      processBlock(node, currentParent);
      break ;
      case Token.LABEL:
      processLabel(node, currentParent);
      break ;
      case Token.EXPR_RESULT:
      processExprResult(node, currentParent);
      break ;
      case Token.SCRIPT:
      processScript(node, currentParent);
      break ;
    }
  }
  private void processObjectLiteral(Node node, JsonML currentParent) {
    JsonML element = new JsonML(TagType.ObjectExpr);
    currentParent.appendChild(element);
    for (Node key : node.children()) {
      Node value = key.getFirstChild();
      JsonML item;
      Object name;
      switch (key.getType()){
        case Token.STRING_KEY:
        item = new JsonML(TagType.DataProp);
        name = key.getString();
        break ;
        case Token.GETTER_DEF:
        item = new JsonML(TagType.GetterProp);
        name = key.getString();
        break ;
        case Token.SETTER_DEF:
        item = new JsonML(TagType.SetterProp);
        name = key.getString();
        break ;
        default:
        throw new IllegalArgumentException("Illegal type of node.");
      }
      item.setAttribute(TagAttr.NAME, name);
      processNode(value, item);
      element.appendChild(item);
    }
  }
  private void processOneArgExpr(Node node, JsonML currentParent, TagType type) {
    JsonML element = new JsonML(type);
    currentParent.appendChild(element);
    processNode(node.getFirstChild(), element);
  }
  private void processRegExp(Node node, JsonML currentParent) {
    JsonML element = new JsonML(TagType.RegExpExpr);
    currentParent.appendChild(element);
    Node child = node.getFirstChild();
    element.setAttribute(TagAttr.BODY, child.getString());
    String flags = "";
    child = child.getNext();
    if(child != null) {
      flags = child.getString();
    }
    element.setAttribute(TagAttr.FLAGS, flags);
  }
  private void processReturn(Node currentNode, JsonML currentParent) {
    JsonML element = new JsonML(TagType.ReturnStmt);
    currentParent.appendChild(element);
    if(currentNode.hasChildren()) {
      processNode(currentNode.getFirstChild(), element);
    }
  }
  private void processScript(Node node, JsonML currentParent) {
    JsonML element = new JsonML(TagType.Program);
    currentParent.appendChild(element);
    processDirectives(node, element);
    processChildren(node, element);
  }
  private void processSwitch(Node node, JsonML currentParent) {
    JsonML element = new JsonML(TagType.SwitchStmt);
    currentParent.appendChild(element);
    Node child = node.getFirstChild();
    processNode(child, element);
    for(com.google.javascript.rhino.Node c = child.getNext(); c != null; c = c.getNext()) {
      processNode(c, element);
    }
  }
  private void processThis(Node node, JsonML currentParent) {
    currentParent.appendChild(new JsonML(TagType.ThisExpr));
  }
  private void processThrow(Node node, JsonML currentParent) {
    JsonML element = new JsonML(TagType.ThrowStmt);
    currentParent.appendChild(element);
    processNode(node.getFirstChild(), element);
  }
  private void processTry(Node node, JsonML currentParent) {
    JsonML element = new JsonML(TagType.TryStmt);
    currentParent.appendChild(element);
    Node child = node.getFirstChild();
    processNode(child, element);
    child = child.getNext();
    if(child.hasChildren()) {
      processNode(child.getFirstChild(), element);
    }
    else {
      element.appendChild(new JsonML(TagType.Empty));
    }
    child = child.getNext();
    if(child != null) {
      processNode(child, element);
    }
  }
  private void processTwoArgExpr(Node node, JsonML currentParent, TagType type) {
    processTwoArgExpr(node, currentParent, type, null);
  }
  private void processTwoArgExpr(Node node, JsonML currentParent, TagType type, String op) {
    JsonML element = new JsonML(type);
    if(op != null) {
      element.setAttribute(TagAttr.OP, op);
    }
    currentParent.appendChild(element);
    Preconditions.checkState(node.getChildCount() == 2);
    Node child = node.getFirstChild();
    processNode(child, element);
    processNode(child.getNext(), element);
  }
  private void processUnaryExpr(Node node, JsonML currentParent, String op) {
    JsonML element = new JsonML(TagType.UnaryExpr);
    element.setAttribute(TagAttr.OP, op);
    currentParent.appendChild(element);
    processNode(node.getFirstChild(), element);
  }
  private void processVar(Node node, JsonML currentParent) {
    JsonML element = new JsonML(TagType.VarDecl);
    currentParent.appendChild(element);
    Iterator<Node> it = node.children().iterator();
    while(it.hasNext()){
      Node child = it.next();
      JsonML id = new JsonML(TagType.IdPatt);
      id.setAttribute(TagAttr.NAME, child.getString());
      if(child.hasChildren()) {
        JsonML patt = new JsonML(TagType.InitPatt);
        element.appendChild(patt);
        patt.appendChild(id);
        processNode(child.getFirstChild(), patt);
      }
      else {
        element.appendChild(id);
      }
    }
  }
  private void processWith(Node node, JsonML currentParent) {
    JsonML element = new JsonML(TagType.WithStmt);
    currentParent.appendChild(element);
    Node child = node.getFirstChild();
    processNode(child, element);
    child = child.getNext();
    processNode(child, element);
  }
}