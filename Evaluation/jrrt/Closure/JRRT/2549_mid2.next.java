package com.google.javascript.rhino;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.SimpleSourceFile;
import com.google.javascript.rhino.jstype.StaticSourceFile;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class Node implements Cloneable, Serializable  {
  final private static long serialVersionUID = 1L;
  final public static int JSDOC_INFO_PROP = 29;
  final public static int VAR_ARGS_NAME = 30;
  final public static int INCRDECR_PROP = 32;
  final public static int QUOTED_PROP = 36;
  final public static int OPT_ARG_NAME = 37;
  final public static int SYNTHETIC_BLOCK_PROP = 38;
  final public static int EMPTY_BLOCK = 39;
  final public static int ORIGINALNAME_PROP = 40;
  final public static int SIDE_EFFECT_FLAGS = 42;
  final public static int IS_CONSTANT_NAME = 43;
  final public static int IS_NAMESPACE = 46;
  final public static int IS_DISPATCHER = 47;
  final public static int DIRECTIVES = 48;
  final public static int DIRECT_EVAL = 49;
  final public static int FREE_CALL = 50;
  final public static int STATIC_SOURCE_FILE = 51;
  final public static int LENGTH = 52;
  final public static int INPUT_ID = 53;
  final public static int SLASH_V = 54;
  final public static int INFERRED_FUNCTION = 55;
  final public static int LAST_PROP = 55;
  final public static int DECR_FLAG = 0x1;
  final public static int POST_FLAG = 0x2;
  int type;
  Node next;
  private Node first;
  private Node last;
  private PropListItem propListHead;
  final public static int COLUMN_BITS = 12;
  final public static int MAX_COLUMN_NUMBER = (1 << COLUMN_BITS) - 1;
  final public static int COLUMN_MASK = MAX_COLUMN_NUMBER;
  private int sourcePosition;
  private JSType jsType;
  private Node parent;
  final public static int FLAG_GLOBAL_STATE_UNMODIFIED = 1;
  final public static int FLAG_THIS_UNMODIFIED = 2;
  final public static int FLAG_ARGUMENTS_UNMODIFIED = 4;
  final public static int FLAG_NO_THROWS = 8;
  final public static int FLAG_LOCAL_RESULTS = 16;
  final public static int SIDE_EFFECTS_FLAGS_MASK = 31;
  final public static int SIDE_EFFECTS_ALL = 0;
  final public static int NO_SIDE_EFFECTS = FLAG_GLOBAL_STATE_UNMODIFIED | FLAG_THIS_UNMODIFIED | FLAG_ARGUMENTS_UNMODIFIED | FLAG_NO_THROWS;
  public Node(int nodeType) {
    super();
    type = nodeType;
    parent = null;
    sourcePosition = -1;
  }
  public Node(int nodeType, Node child) {
    super();
    Preconditions.checkArgument(child.parent == null, "new child has existing parent");
    Preconditions.checkArgument(child.next == null, "new child has existing sibling");
    type = nodeType;
    parent = null;
    first = last = child;
    child.next = null;
    child.parent = this;
    sourcePosition = -1;
  }
  public Node(int nodeType, Node child, int lineno, int charno) {
    this(nodeType, child);
    sourcePosition = mergeLineCharNo(lineno, charno);
  }
  public Node(int nodeType, Node left, Node mid, Node mid2, Node right) {
    super();
    Preconditions.checkArgument(left.parent == null);
    Preconditions.checkArgument(left.next == null);
    Preconditions.checkArgument(mid.parent == null);
    Preconditions.checkArgument(mid.next == null);
    Preconditions.checkArgument(mid2.parent == null);
    Node var_2549 = mid2.next;
    Preconditions.checkArgument(var_2549 == null);
    Preconditions.checkArgument(right.parent == null);
    Preconditions.checkArgument(right.next == null);
    type = nodeType;
    parent = null;
    first = left;
    last = right;
    left.next = mid;
    left.parent = this;
    mid.next = mid2;
    mid.parent = this;
    mid2.next = right;
    mid2.parent = this;
    right.next = null;
    right.parent = this;
    sourcePosition = -1;
  }
  public Node(int nodeType, Node left, Node mid, Node mid2, Node right, int lineno, int charno) {
    this(nodeType, left, mid, mid2, right);
    sourcePosition = mergeLineCharNo(lineno, charno);
  }
  public Node(int nodeType, Node left, Node mid, Node right) {
    super();
    Preconditions.checkArgument(left.parent == null);
    Preconditions.checkArgument(left.next == null);
    Preconditions.checkArgument(mid.parent == null);
    Preconditions.checkArgument(mid.next == null);
    Preconditions.checkArgument(right.parent == null);
    Preconditions.checkArgument(right.next == null);
    type = nodeType;
    parent = null;
    first = left;
    last = right;
    left.next = mid;
    left.parent = this;
    mid.next = right;
    mid.parent = this;
    right.next = null;
    right.parent = this;
    sourcePosition = -1;
  }
  public Node(int nodeType, Node left, Node mid, Node right, int lineno, int charno) {
    this(nodeType, left, mid, right);
    sourcePosition = mergeLineCharNo(lineno, charno);
  }
  public Node(int nodeType, Node left, Node right) {
    super();
    Preconditions.checkArgument(left.parent == null, "first new child has existing parent");
    Preconditions.checkArgument(left.next == null, "first new child has existing sibling");
    Preconditions.checkArgument(right.parent == null, "second new child has existing parent");
    Preconditions.checkArgument(right.next == null, "second new child has existing sibling");
    type = nodeType;
    parent = null;
    first = left;
    last = right;
    left.next = right;
    left.parent = this;
    right.next = null;
    right.parent = this;
    sourcePosition = -1;
  }
  public Node(int nodeType, Node left, Node right, int lineno, int charno) {
    this(nodeType, left, right);
    sourcePosition = mergeLineCharNo(lineno, charno);
  }
  public Node(int nodeType, Node[] children) {
    super();
    this.type = nodeType;
    parent = null;
    if(children.length != 0) {
      this.first = children[0];
      this.last = children[children.length - 1];
      for(int i = 1; i < children.length; i++) {
        if(null != children[i - 1].next) {
          throw new IllegalArgumentException("duplicate child");
        }
        children[i - 1].next = children[i];
        Preconditions.checkArgument(children[i - 1].parent == null);
        children[i - 1].parent = this;
      }
      Preconditions.checkArgument(children[children.length - 1].parent == null);
      children[children.length - 1].parent = this;
      if(null != this.last.next) {
        throw new IllegalArgumentException("duplicate child");
      }
    }
  }
  public Node(int nodeType, Node[] children, int lineno, int charno) {
    this(nodeType, children);
    sourcePosition = mergeLineCharNo(lineno, charno);
  }
  public Node(int nodeType, int lineno, int charno) {
    super();
    type = nodeType;
    parent = null;
    sourcePosition = mergeLineCharNo(lineno, charno);
  }
  public AncestorIterable getAncestors() {
    return new AncestorIterable(this.getParent());
  }
  public FileLevelJsDocBuilder getJsDocBuilderForNode() {
    return new FileLevelJsDocBuilder();
  }
  public InputId getInputId() {
    return ((InputId)this.getProp(INPUT_ID));
  }
  public Iterable<Node> children() {
    if(first == null) {
      return Collections.emptySet();
    }
    else {
      return new SiblingNodeIterable(first);
    }
  }
  public Iterable<Node> siblings() {
    return new SiblingNodeIterable(this);
  }
  public JSDocInfo getJSDocInfo() {
    return (JSDocInfo)getProp(JSDOC_INFO_PROP);
  }
  public JSType getJSType() {
    return jsType;
  }
  public Node cloneNode() {
    Node result;
    try {
      result = (Node)super.clone();
      result.next = null;
      result.first = null;
      result.last = null;
      result.parent = null;
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException(e.getMessage());
    }
    return result;
  }
  public Node clonePropsFrom(Node other) {
    Preconditions.checkState(this.propListHead == null, "Node has existing properties.");
    this.propListHead = other.propListHead;
    return this;
  }
  public Node cloneTree() {
    Node result = cloneNode();
    for(com.google.javascript.rhino.Node n2 = getFirstChild(); n2 != null; n2 = n2.getNext()) {
      Node n2clone = n2.cloneTree();
      n2clone.parent = result;
      if(result.last != null) {
        result.last.next = n2clone;
      }
      if(result.first == null) {
        result.first = n2clone;
      }
      result.last = n2clone;
    }
    return result;
  }
  public Node copyInformationFrom(Node other) {
    if(getProp(ORIGINALNAME_PROP) == null) {
      putProp(ORIGINALNAME_PROP, other.getProp(ORIGINALNAME_PROP));
    }
    if(getProp(STATIC_SOURCE_FILE) == null) {
      putProp(STATIC_SOURCE_FILE, other.getProp(STATIC_SOURCE_FILE));
      sourcePosition = other.sourcePosition;
    }
    return this;
  }
  public Node copyInformationFromForTree(Node other) {
    copyInformationFrom(other);
    for(com.google.javascript.rhino.Node child = getFirstChild(); child != null; child = child.getNext()) {
      child.copyInformationFromForTree(other);
    }
    return this;
  }
  public Node detachFromParent() {
    Preconditions.checkState(parent != null);
    parent.removeChild(this);
    return this;
  }
  public Node getAncestor(int level) {
    Preconditions.checkArgument(level >= 0);
    Node node = this;
    while(node != null && level-- > 0){
      node = node.getParent();
    }
    return node;
  }
  public Node getChildAtIndex(int i) {
    Node n = first;
    while(i > 0){
      n = n.next;
      i--;
    }
    return n;
  }
  public Node getChildBefore(Node child) {
    if(child == first) {
      return null;
    }
    Node n = first;
    while(n.next != child){
      n = n.next;
      if(n == null) {
        throw new RuntimeException("node is not a child");
      }
    }
    return n;
  }
  public Node getFirstChild() {
    return first;
  }
  public Node getLastChild() {
    return last;
  }
  public Node getLastSibling() {
    Node n = this;
    while(n.next != null){
      n = n.next;
    }
    return n;
  }
  public Node getNext() {
    return next;
  }
  public Node getParent() {
    return parent;
  }
  public static Node newNumber(double number) {
    return new NumberNode(number);
  }
  public static Node newNumber(double number, int lineno, int charno) {
    return new NumberNode(number, lineno, charno);
  }
  public static Node newString(int type, String str) {
    return new StringNode(type, str);
  }
  public static Node newString(int type, String str, int lineno, int charno) {
    return new StringNode(type, str, lineno, charno);
  }
  public static Node newString(String str) {
    return new StringNode(Token.STRING, str);
  }
  public static Node newString(String str, int lineno, int charno) {
    return new StringNode(Token.STRING, str, lineno, charno);
  }
  public Node removeChildAfter(Node prev) {
    Preconditions.checkArgument(prev.parent == this, "prev is not a child of this node.");
    Preconditions.checkArgument(prev.next != null, "no next sibling.");
    Node child = prev.next;
    prev.next = child.next;
    if(child == last) 
      last = prev;
    child.next = null;
    child.parent = null;
    return child;
  }
  public Node removeChildren() {
    Node children = first;
    for(com.google.javascript.rhino.Node child = first; child != null; child = child.getNext()) {
      child.parent = null;
    }
    first = null;
    last = null;
    return children;
  }
  public Node removeFirstChild() {
    Node child = first;
    if(child != null) {
      removeChild(child);
    }
    return child;
  }
  public Node setJSDocInfo(JSDocInfo info) {
    putProp(JSDOC_INFO_PROP, info);
    return this;
  }
  public Node srcref(Node other) {
    return useSourceInfoFrom(other);
  }
  public Node srcrefTree(Node other) {
    return useSourceInfoFromForTree(other);
  }
  public Node useSourceInfoFrom(Node other) {
    putProp(ORIGINALNAME_PROP, other.getProp(ORIGINALNAME_PROP));
    putProp(STATIC_SOURCE_FILE, other.getProp(STATIC_SOURCE_FILE));
    sourcePosition = other.sourcePosition;
    return this;
  }
  public Node useSourceInfoFromForTree(Node other) {
    useSourceInfoFrom(other);
    for(com.google.javascript.rhino.Node child = getFirstChild(); child != null; child = child.getNext()) {
      child.useSourceInfoFromForTree(other);
    }
    return this;
  }
  public Node useSourceInfoIfMissingFrom(Node other) {
    if(getProp(ORIGINALNAME_PROP) == null) {
      putProp(ORIGINALNAME_PROP, other.getProp(ORIGINALNAME_PROP));
    }
    if(getProp(STATIC_SOURCE_FILE) == null) {
      putProp(STATIC_SOURCE_FILE, other.getProp(STATIC_SOURCE_FILE));
      sourcePosition = other.sourcePosition;
    }
    return this;
  }
  public Node useSourceInfoIfMissingFromForTree(Node other) {
    useSourceInfoIfMissingFrom(other);
    for(com.google.javascript.rhino.Node child = getFirstChild(); child != null; child = child.getNext()) {
      child.useSourceInfoIfMissingFromForTree(other);
    }
    return this;
  }
  NodeMismatch checkTreeEqualsImpl(Node node2) {
    if(!isEquivalentTo(node2, false, false)) {
      return new NodeMismatch(this, node2);
    }
    NodeMismatch res = null;
    Node n;
    Node n2;
    for(n = first, n2 = node2.first; res == null && n != null; n = n.next, n2 = n2.next) {
      if(node2 == null) {
        throw new IllegalStateException();
      }
      res = n.checkTreeEqualsImpl(n2);
      if(res != null) {
        return res;
      }
    }
    return res;
  }
  NodeMismatch checkTreeTypeAwareEqualsImpl(Node node2) {
    if(!isEquivalentTo(node2, true, false)) {
      return new NodeMismatch(this, node2);
    }
    NodeMismatch res = null;
    Node n;
    Node n2;
    for(n = first, n2 = node2.first; res == null && n != null; n = n.next, n2 = n2.next) {
      res = n.checkTreeTypeAwareEqualsImpl(n2);
      if(res != null) {
        return res;
      }
    }
    return res;
  }
  public Object getProp(int propType) {
    PropListItem item = lookupProperty(propType);
    if(item == null) {
      return null;
    }
    return item.getObjectValue();
  }
  PropListItem createProp(int propType, int value, PropListItem next) {
    return new IntPropListItem(propType, value, next);
  }
  PropListItem createProp(int propType, Object value, PropListItem next) {
    return new ObjectPropListItem(propType, value, next);
  }
  PropListItem getPropListHeadForTesting() {
    return propListHead;
  }
  @VisibleForTesting() PropListItem lookupProperty(int propType) {
    PropListItem x = propListHead;
    while(x != null && propType != x.getType()){
      x = x.getNext();
    }
    return x;
  }
  private PropListItem removeProp(PropListItem item, int propType) {
    if(item == null) {
      return null;
    }
    else 
      if(item.getType() == propType) {
        return item.getNext();
      }
      else {
        PropListItem result = removeProp(item.getNext(), propType);
        if(result != item.getNext()) {
          return item.chain(result);
        }
        else {
          return item;
        }
      }
  }
  @SuppressWarnings(value = {"unchecked", }) public Set<String> getDirectives() {
    return (Set<String>)getProp(DIRECTIVES);
  }
  public StaticSourceFile getStaticSourceFile() {
    return ((StaticSourceFile)this.getProp(STATIC_SOURCE_FILE));
  }
  public String checkTreeEquals(Node node2) {
    NodeMismatch diff = checkTreeEqualsImpl(node2);
    if(diff != null) {
      return "Node tree inequality:" + "\nTree1:\n" + toStringTree() + "\n\nTree2:\n" + node2.toStringTree() + "\n\nSubtree1: " + diff.nodeA.toStringTree() + "\n\nSubtree2: " + diff.nodeB.toStringTree();
    }
    return null;
  }
  public String getQualifiedName() {
    if(type == Token.NAME) {
      String name = getString();
      return name.isEmpty() ? null : name;
    }
    else 
      if(type == Token.GETPROP) {
        String left = getFirstChild().getQualifiedName();
        if(left == null) {
          return null;
        }
        return left + "." + getLastChild().getString();
      }
      else 
        if(type == Token.THIS) {
          return "this";
        }
        else {
          return null;
        }
  }
  public String getSourceFileName() {
    StaticSourceFile file = getStaticSourceFile();
    return file == null ? null : file.getName();
  }
  public String getString() throws UnsupportedOperationException {
    if(this.getType() == Token.STRING) {
      throw new IllegalStateException("String node not created with Node.newString");
    }
    else {
      throw new UnsupportedOperationException(this + " is not a string node");
    }
  }
  final private static String propToString(int propType) {
    switch (propType){
      case VAR_ARGS_NAME:
      return "var_args_name";
      case JSDOC_INFO_PROP:
      return "jsdoc_info";
      case INCRDECR_PROP:
      return "incrdecr";
      case QUOTED_PROP:
      return "quoted";
      case OPT_ARG_NAME:
      return "opt_arg";
      case SYNTHETIC_BLOCK_PROP:
      return "synthetic";
      case EMPTY_BLOCK:
      return "empty_block";
      case ORIGINALNAME_PROP:
      return "originalname";
      case SIDE_EFFECT_FLAGS:
      return "side_effect_flags";
      case IS_CONSTANT_NAME:
      return "is_constant_name";
      case IS_NAMESPACE:
      return "is_namespace";
      case IS_DISPATCHER:
      return "is_dispatcher";
      case DIRECTIVES:
      return "directives";
      case DIRECT_EVAL:
      return "direct_eval";
      case FREE_CALL:
      return "free_call";
      case STATIC_SOURCE_FILE:
      return "source_file";
      case INPUT_ID:
      return "input_id";
      case LENGTH:
      return "length";
      case SLASH_V:
      return "slash_v";
      case INFERRED_FUNCTION:
      return "inferred";
      default:
      throw new IllegalStateException("unexpect prop id " + propType);
    }
  }
  @Override() public String toString() {
    return toString(true, true, true);
  }
  public String toString(boolean printSource, boolean printAnnotations, boolean printType) {
    StringBuilder sb = new StringBuilder();
    toString(sb, printSource, printAnnotations, printType);
    return sb.toString();
  }
  public String toStringTree() {
    return toStringTreeImpl();
  }
  private String toStringTreeImpl() {
    try {
      StringBuilder s = new StringBuilder();
      appendStringTree(s);
      return s.toString();
    }
    catch (IOException e) {
      throw new RuntimeException("Should not happen\n" + e);
    }
  }
  private boolean areBitFlagsSet(int value, int flags) {
    return (value & flags) == flags;
  }
  public boolean getBooleanProp(int propType) {
    return getIntProp(propType) != 0;
  }
  public boolean hasChild(Node child) {
    for(com.google.javascript.rhino.Node n = first; n != null; n = n.getNext()) {
      if(child == n) {
        return true;
      }
    }
    return false;
  }
  public boolean hasChildren() {
    return first != null;
  }
  public boolean hasMoreThanOneChild() {
    return first != null && first != last;
  }
  public boolean hasOneChild() {
    return first != null && first == last;
  }
  public boolean isAdd() {
    return this.getType() == Token.ADD;
  }
  public boolean isAnd() {
    return this.getType() == Token.AND;
  }
  public boolean isArrayLit() {
    return this.getType() == Token.ARRAYLIT;
  }
  public boolean isAssign() {
    return this.getType() == Token.ASSIGN;
  }
  public boolean isAssignAdd() {
    return this.getType() == Token.ASSIGN_ADD;
  }
  public boolean isBlock() {
    return this.getType() == Token.BLOCK;
  }
  public boolean isBreak() {
    return this.getType() == Token.BREAK;
  }
  public boolean isCall() {
    return this.getType() == Token.CALL;
  }
  public boolean isCase() {
    return this.getType() == Token.CASE;
  }
  public boolean isCast() {
    return this.getType() == Token.CAST;
  }
  public boolean isCatch() {
    return this.getType() == Token.CATCH;
  }
  public boolean isComma() {
    return this.getType() == Token.COMMA;
  }
  public boolean isContinue() {
    return this.getType() == Token.CONTINUE;
  }
  public boolean isDebugger() {
    return this.getType() == Token.DEBUGGER;
  }
  public boolean isDec() {
    return this.getType() == Token.DEC;
  }
  public boolean isDefaultCase() {
    return this.getType() == Token.DEFAULT_CASE;
  }
  public boolean isDelProp() {
    return this.getType() == Token.DELPROP;
  }
  public boolean isDo() {
    return this.getType() == Token.DO;
  }
  public boolean isEmpty() {
    return this.getType() == Token.EMPTY;
  }
  public boolean isEquivalentTo(Node node) {
    return isEquivalentTo(node, false, true);
  }
  boolean isEquivalentTo(Node node, boolean compareJsType, boolean recurse) {
    if(type != node.getType() || getChildCount() != node.getChildCount() || this.getClass() != node.getClass()) {
      return false;
    }
    if(compareJsType && !JSType.isEquivalent(jsType, node.getJSType())) {
      return false;
    }
    if(type == Token.INC || type == Token.DEC) {
      int post1 = this.getIntProp(INCRDECR_PROP);
      int post2 = node.getIntProp(INCRDECR_PROP);
      if(post1 != post2) {
        return false;
      }
    }
    else 
      if(type == Token.STRING || type == Token.STRING_KEY) {
        if(type == Token.STRING_KEY) {
          int quoted1 = this.getIntProp(QUOTED_PROP);
          int quoted2 = node.getIntProp(QUOTED_PROP);
          if(quoted1 != quoted2) {
            return false;
          }
        }
        int slashV1 = this.getIntProp(SLASH_V);
        int slashV2 = node.getIntProp(SLASH_V);
        if(slashV1 != slashV2) {
          return false;
        }
      }
      else 
        if(type == Token.CALL) {
          if(this.getBooleanProp(FREE_CALL) != node.getBooleanProp(FREE_CALL)) {
            return false;
          }
        }
    if(recurse) {
      Node n;
      Node n2;
      for(n = first, n2 = node.first; n != null; n = n.next, n2 = n2.next) {
        if(!n.isEquivalentTo(n2, compareJsType, true)) {
          return false;
        }
      }
    }
    return true;
  }
  public boolean isEquivalentToTyped(Node node) {
    return isEquivalentTo(node, true, true);
  }
  public boolean isExprResult() {
    return this.getType() == Token.EXPR_RESULT;
  }
  public boolean isFalse() {
    return this.getType() == Token.FALSE;
  }
  public boolean isFor() {
    return this.getType() == Token.FOR;
  }
  public boolean isFromExterns() {
    StaticSourceFile file = getStaticSourceFile();
    return file == null ? false : file.isExtern();
  }
  public boolean isFunction() {
    return this.getType() == Token.FUNCTION;
  }
  public boolean isGetElem() {
    return this.getType() == Token.GETELEM;
  }
  public boolean isGetProp() {
    return this.getType() == Token.GETPROP;
  }
  public boolean isGetterDef() {
    return this.getType() == Token.GETTER_DEF;
  }
  public boolean isHook() {
    return this.getType() == Token.HOOK;
  }
  public boolean isIf() {
    return this.getType() == Token.IF;
  }
  public boolean isIn() {
    return this.getType() == Token.IN;
  }
  public boolean isInc() {
    return this.getType() == Token.INC;
  }
  public boolean isInstanceOf() {
    return this.getType() == Token.INSTANCEOF;
  }
  public boolean isLabel() {
    return this.getType() == Token.LABEL;
  }
  public boolean isLabelName() {
    return this.getType() == Token.LABEL_NAME;
  }
  public boolean isLocalResultCall() {
    return areBitFlagsSet(getSideEffectFlags(), FLAG_LOCAL_RESULTS);
  }
  public boolean isNE() {
    return this.getType() == Token.NE;
  }
  public boolean isName() {
    return this.getType() == Token.NAME;
  }
  public boolean isNew() {
    return this.getType() == Token.NEW;
  }
  public boolean isNoSideEffectsCall() {
    return areBitFlagsSet(getSideEffectFlags(), NO_SIDE_EFFECTS);
  }
  public boolean isNot() {
    return this.getType() == Token.NOT;
  }
  public boolean isNull() {
    return this.getType() == Token.NULL;
  }
  public boolean isNumber() {
    return this.getType() == Token.NUMBER;
  }
  public boolean isObjectLit() {
    return this.getType() == Token.OBJECTLIT;
  }
  public boolean isOnlyModifiesThisCall() {
    return areBitFlagsSet(getSideEffectFlags() & Node.NO_SIDE_EFFECTS, Node.FLAG_GLOBAL_STATE_UNMODIFIED | Node.FLAG_ARGUMENTS_UNMODIFIED | Node.FLAG_NO_THROWS);
  }
  public boolean isOptionalArg() {
    return getBooleanProp(OPT_ARG_NAME);
  }
  public boolean isOr() {
    return this.getType() == Token.OR;
  }
  public boolean isParamList() {
    return this.getType() == Token.PARAM_LIST;
  }
  public boolean isQualifiedName() {
    switch (getType()){
      case Token.NAME:
      return getString().isEmpty() ? false : true;
      case Token.THIS:
      return true;
      case Token.GETPROP:
      return getFirstChild().isQualifiedName();
      default:
      return false;
    }
  }
  public boolean isQuotedString() {
    return false;
  }
  public boolean isRegExp() {
    return this.getType() == Token.REGEXP;
  }
  public boolean isReturn() {
    return this.getType() == Token.RETURN;
  }
  public boolean isScript() {
    return this.getType() == Token.SCRIPT;
  }
  public boolean isSetterDef() {
    return this.getType() == Token.SETTER_DEF;
  }
  public boolean isString() {
    return this.getType() == Token.STRING;
  }
  public boolean isStringKey() {
    return this.getType() == Token.STRING_KEY;
  }
  public boolean isSwitch() {
    return this.getType() == Token.SWITCH;
  }
  public boolean isSyntheticBlock() {
    return getBooleanProp(SYNTHETIC_BLOCK_PROP);
  }
  public boolean isThis() {
    return this.getType() == Token.THIS;
  }
  public boolean isThrow() {
    return this.getType() == Token.THROW;
  }
  public boolean isTrue() {
    return this.getType() == Token.TRUE;
  }
  public boolean isTry() {
    return this.getType() == Token.TRY;
  }
  public boolean isTypeOf() {
    return this.getType() == Token.TYPEOF;
  }
  public boolean isUnscopedQualifiedName() {
    switch (getType()){
      case Token.NAME:
      return getString().isEmpty() ? false : true;
      case Token.GETPROP:
      return getFirstChild().isUnscopedQualifiedName();
      default:
      return false;
    }
  }
  public boolean isVar() {
    return this.getType() == Token.VAR;
  }
  public boolean isVarArgs() {
    return getBooleanProp(VAR_ARGS_NAME);
  }
  public boolean isVoid() {
    return this.getType() == Token.VOID;
  }
  public boolean isWhile() {
    return this.getType() == Token.WHILE;
  }
  public boolean isWith() {
    return this.getType() == Token.WITH;
  }
  public boolean wasEmptyNode() {
    return getBooleanProp(EMPTY_BLOCK);
  }
  public double getDouble() throws UnsupportedOperationException {
    if(this.getType() == Token.NUMBER) {
      throw new IllegalStateException("Number node not created with Node.newNumber");
    }
    else {
      throw new UnsupportedOperationException(this + " is not a number node");
    }
  }
  protected static int extractCharno(int lineCharNo) {
    if(lineCharNo == -1) {
      return -1;
    }
    else {
      return lineCharNo & COLUMN_MASK;
    }
  }
  protected static int extractLineno(int lineCharNo) {
    if(lineCharNo == -1) {
      return -1;
    }
    else {
      return lineCharNo >>> COLUMN_BITS;
    }
  }
  public int getCharno() {
    return extractCharno(sourcePosition);
  }
  public int getChildCount() {
    int c = 0;
    for(com.google.javascript.rhino.Node n = first; n != null; n = n.next) 
      c++;
    return c;
  }
  public int getExistingIntProp(int propType) {
    PropListItem item = lookupProperty(propType);
    if(item == null) {
      throw new IllegalStateException("missing prop: " + propType);
    }
    return item.getIntValue();
  }
  public int getIndexOfChild(Node child) {
    Node n = first;
    int i = 0;
    while(n != null){
      if(child == n) {
        return i;
      }
      n = n.next;
      i++;
    }
    return -1;
  }
  public int getIntProp(int propType) {
    PropListItem item = lookupProperty(propType);
    if(item == null) {
      return 0;
    }
    return item.getIntValue();
  }
  public int getLength() {
    return getIntProp(LENGTH);
  }
  public int getLineno() {
    return extractLineno(sourcePosition);
  }
  public int getSideEffectFlags() {
    return getIntProp(SIDE_EFFECT_FLAGS);
  }
  public int getSourceOffset() {
    StaticSourceFile file = getStaticSourceFile();
    if(file == null) {
      return -1;
    }
    int lineno = getLineno();
    if(lineno == -1) {
      return -1;
    }
    return file.getLineOffset(lineno) + getCharno();
  }
  public int getSourcePosition() {
    return sourcePosition;
  }
  public int getType() {
    return type;
  }
  protected static int mergeLineCharNo(int lineno, int charno) {
    if(lineno < 0 || charno < 0) {
      return -1;
    }
    else 
      if((charno & ~COLUMN_MASK) != 0) {
        return lineno << COLUMN_BITS | COLUMN_MASK;
      }
      else {
        return lineno << COLUMN_BITS | (charno & COLUMN_MASK);
      }
  }
  private int[] getSortedPropTypes() {
    int count = 0;
    for(com.google.javascript.rhino.Node.PropListItem x = propListHead; x != null; x = x.getNext()) {
      count++;
    }
    int[] keys = new int[count];
    for(com.google.javascript.rhino.Node.PropListItem x = propListHead; x != null; x = x.getNext()) {
      count--;
      keys[count] = x.getType();
    }
    Arrays.sort(keys);
    return keys;
  }
  public void addChildAfter(Node newChild, Node node) {
    Preconditions.checkArgument(newChild.next == null, "The new child node has siblings.");
    addChildrenAfter(newChild, node);
  }
  public void addChildBefore(Node newChild, Node node) {
    Preconditions.checkArgument(node != null && node.parent == this, "The existing child node of the parent should not be null.");
    Preconditions.checkArgument(newChild.next == null, "The new child node has siblings.");
    Preconditions.checkArgument(newChild.parent == null, "The new child node already has a parent.");
    if(first == node) {
      newChild.parent = this;
      newChild.next = first;
      first = newChild;
      return ;
    }
    Node prev = getChildBefore(node);
    addChildAfter(newChild, prev);
  }
  public void addChildToBack(Node child) {
    Preconditions.checkArgument(child.parent == null);
    Preconditions.checkArgument(child.next == null);
    child.parent = this;
    child.next = null;
    if(last == null) {
      first = last = child;
      return ;
    }
    last.next = child;
    last = child;
  }
  public void addChildToFront(Node child) {
    Preconditions.checkArgument(child.parent == null);
    Preconditions.checkArgument(child.next == null);
    child.parent = this;
    child.next = first;
    first = child;
    if(last == null) {
      last = child;
    }
  }
  public void addChildrenAfter(Node children, Node node) {
    Preconditions.checkArgument(node == null || node.parent == this);
    for(com.google.javascript.rhino.Node child = children; child != null; child = child.next) {
      Preconditions.checkArgument(child.parent == null);
      child.parent = this;
    }
    Node lastSibling = children.getLastSibling();
    if(node != null) {
      Node oldNext = node.next;
      node.next = children;
      lastSibling.next = oldNext;
      if(node == last) {
        last = lastSibling;
      }
    }
    else {
      if(first != null) {
        lastSibling.next = first;
      }
      else {
        last = lastSibling;
      }
      first = children;
    }
  }
  public void addChildrenToBack(Node children) {
    addChildrenAfter(children, getLastChild());
  }
  public void addChildrenToFront(Node children) {
    for(com.google.javascript.rhino.Node child = children; child != null; child = child.next) {
      Preconditions.checkArgument(child.parent == null);
      child.parent = this;
    }
    Node lastSib = children.getLastSibling();
    lastSib.next = first;
    first = children;
    if(last == null) {
      last = lastSib;
    }
  }
  public void addSuppression(String warning) {
    if(getJSDocInfo() == null) {
      setJSDocInfo(new JSDocInfo(false));
    }
    getJSDocInfo().addSuppression(warning);
  }
  public void appendStringTree(Appendable appendable) throws IOException {
    toStringTreeHelper(this, 0, appendable);
  }
  public void detachChildren() {
    for(com.google.javascript.rhino.Node child = first; child != null; ) {
      Node nextChild = child.getNext();
      child.parent = null;
      child.next = null;
      child = nextChild;
    }
    first = null;
    last = null;
  }
  public void putBooleanProp(int propType, boolean value) {
    putIntProp(propType, value ? 1 : 0);
  }
  public void putIntProp(int propType, int value) {
    removeProp(propType);
    if(value != 0) {
      propListHead = createProp(propType, value, propListHead);
    }
  }
  public void putProp(int propType, Object value) {
    removeProp(propType);
    if(value != null) {
      propListHead = createProp(propType, value, propListHead);
    }
  }
  public void removeChild(Node child) {
    Node prev = getChildBefore(child);
    if(prev == null) 
      first = first.next;
    else 
      prev.next = child.next;
    if(child == last) 
      last = prev;
    child.next = null;
    child.parent = null;
  }
  public void removeProp(int propType) {
    PropListItem result = removeProp(propListHead, propType);
    if(result != propListHead) {
      propListHead = result;
    }
  }
  public void replaceChild(Node child, Node newChild) {
    Preconditions.checkArgument(newChild.next == null, "The new child node has siblings.");
    Preconditions.checkArgument(newChild.parent == null, "The new child node already has a parent.");
    newChild.copyInformationFrom(child);
    newChild.next = child.next;
    newChild.parent = this;
    if(child == first) {
      first = newChild;
    }
    else {
      Node prev = getChildBefore(child);
      prev.next = newChild;
    }
    if(child == last) 
      last = newChild;
    child.next = null;
    child.parent = null;
  }
  public void replaceChildAfter(Node prevChild, Node newChild) {
    Preconditions.checkArgument(prevChild.parent == this, "prev is not a child of this node.");
    Preconditions.checkArgument(newChild.next == null, "The new child node has siblings.");
    Preconditions.checkArgument(newChild.parent == null, "The new child node already has a parent.");
    newChild.copyInformationFrom(prevChild);
    Node child = prevChild.next;
    newChild.next = child.next;
    newChild.parent = this;
    prevChild.next = newChild;
    if(child == last) 
      last = newChild;
    child.next = null;
    child.parent = null;
  }
  public void setCharno(int charno) {
    sourcePosition = mergeLineCharNo(getLineno(), charno);
  }
  public void setDirectives(Set<String> val) {
    putProp(DIRECTIVES, val);
  }
  public void setDouble(double s) throws UnsupportedOperationException {
    if(this.getType() == Token.NUMBER) {
      throw new IllegalStateException("Number node not created with Node.newNumber");
    }
    else {
      throw new UnsupportedOperationException(this + " is not a string node");
    }
  }
  public void setInputId(InputId inputId) {
    this.putProp(INPUT_ID, inputId);
  }
  public void setIsSyntheticBlock(boolean val) {
    putBooleanProp(SYNTHETIC_BLOCK_PROP, val);
  }
  public void setJSType(JSType jsType) {
    this.jsType = jsType;
  }
  public void setLength(int length) {
    putIntProp(LENGTH, length);
  }
  public void setLineno(int lineno) {
    int charno = getCharno();
    if(charno == -1) {
      charno = 0;
    }
    sourcePosition = mergeLineCharNo(lineno, charno);
  }
  public void setOptionalArg(boolean optionalArg) {
    putBooleanProp(OPT_ARG_NAME, optionalArg);
  }
  public void setQuotedString() {
    throw new IllegalStateException("not a StringNode");
  }
  public void setSideEffectFlags(SideEffectFlags flags) {
    setSideEffectFlags(flags.valueOf());
  }
  public void setSideEffectFlags(int flags) {
    Preconditions.checkArgument(getType() == Token.CALL || getType() == Token.NEW, "setIsNoSideEffectsCall only supports CALL and NEW nodes, got " + Token.name(getType()));
    putIntProp(SIDE_EFFECT_FLAGS, flags);
  }
  public void setSourceEncodedPosition(int sourcePosition) {
    this.sourcePosition = sourcePosition;
  }
  public void setSourceEncodedPositionForTree(int sourcePosition) {
    this.sourcePosition = sourcePosition;
    for(com.google.javascript.rhino.Node child = getFirstChild(); child != null; child = child.getNext()) {
      child.setSourceEncodedPositionForTree(sourcePosition);
    }
  }
  public void setSourceFileForTesting(String name) {
    this.putProp(STATIC_SOURCE_FILE, new SimpleSourceFile(name, false));
  }
  public void setStaticSourceFile(StaticSourceFile file) {
    this.putProp(STATIC_SOURCE_FILE, file);
  }
  public void setString(String s) throws UnsupportedOperationException {
    if(this.getType() == Token.STRING) {
      throw new IllegalStateException("String node not created with Node.newString");
    }
    else {
      throw new UnsupportedOperationException(this + " is not a string node");
    }
  }
  public void setType(int type) {
    this.type = type;
  }
  public void setVarArgs(boolean varArgs) {
    putBooleanProp(VAR_ARGS_NAME, varArgs);
  }
  public void setWasEmptyNode(boolean val) {
    putBooleanProp(EMPTY_BLOCK, val);
  }
  private void toString(StringBuilder sb, boolean printSource, boolean printAnnotations, boolean printType) {
    sb.append(Token.name(type));
    if(this instanceof StringNode) {
      sb.append(' ');
      sb.append(getString());
    }
    else 
      if(type == Token.FUNCTION) {
        sb.append(' ');
        if(first == null || first.getType() != Token.NAME) {
          sb.append("<invalid>");
        }
        else {
          sb.append(first.getString());
        }
      }
      else 
        if(type == Token.NUMBER) {
          sb.append(' ');
          sb.append(getDouble());
        }
    if(printSource) {
      int lineno = getLineno();
      if(lineno != -1) {
        sb.append(' ');
        sb.append(lineno);
      }
    }
    if(printAnnotations) {
      int[] keys = getSortedPropTypes();
      for(int i = 0; i < keys.length; i++) {
        int type = keys[i];
        PropListItem x = lookupProperty(type);
        sb.append(" [");
        sb.append(propToString(type));
        sb.append(": ");
        String value;
        switch (type){
          default:
          value = x.toString();
          break ;
        }
        sb.append(value);
        sb.append(']');
      }
    }
    if(printType) {
      if(jsType != null) {
        String jsTypeString = jsType.toString();
        if(jsTypeString != null) {
          sb.append(" : ");
          sb.append(jsTypeString);
        }
      }
    }
  }
  private static void toStringTreeHelper(Node n, int level, Appendable sb) throws IOException {
    for(int i = 0; i != level; ++i) {
      sb.append("    ");
    }
    sb.append(n.toString());
    sb.append('\n');
    for(com.google.javascript.rhino.Node cursor = n.getFirstChild(); cursor != null; cursor = cursor.getNext()) {
      toStringTreeHelper(cursor, level + 1, sb);
    }
  }
  
  abstract private static class AbstractPropListItem implements PropListItem, Serializable  {
    final private static long serialVersionUID = 1L;
    final private PropListItem next;
    final private int propType;
    AbstractPropListItem(int propType, PropListItem next) {
      super();
      this.propType = propType;
      this.next = next;
    }
    abstract @Override() public PropListItem chain(PropListItem next);
    @Override() public PropListItem getNext() {
      return next;
    }
    @Override() public int getType() {
      return propType;
    }
  }
  
  public static class AncestorIterable implements Iterable<Node>  {
    private Node cur;
    AncestorIterable(Node cur) {
      super();
      this.cur = cur;
    }
    @Override() public Iterator<Node> iterator() {
      return new Iterator<Node>() {
          @Override() public boolean hasNext() {
            return cur != null;
          }
          @Override() public Node next() {
            if(!hasNext()) 
              throw new NoSuchElementException();
            Node n = cur;
            cur = cur.getParent();
            return n;
          }
          @Override() public void remove() {
            throw new UnsupportedOperationException();
          }
      };
    }
  }
  
  public class FileLevelJsDocBuilder  {
    public void append(String fileLevelComment) {
      JSDocInfo jsDocInfo = getJSDocInfo();
      if(jsDocInfo == null) {
        jsDocInfo = new JSDocInfo(false);
      }
      String license = jsDocInfo.getLicense();
      if(license == null) {
        license = "";
      }
      jsDocInfo.setLicense(license + fileLevelComment);
      setJSDocInfo(jsDocInfo);
    }
  }
  
  private static class IntPropListItem extends AbstractPropListItem  {
    final private static long serialVersionUID = 1L;
    final int intValue;
    IntPropListItem(int propType, int intValue, PropListItem next) {
      super(propType, next);
      this.intValue = intValue;
    }
    @Override() public Object getObjectValue() {
      throw new UnsupportedOperationException();
    }
    @Override() public PropListItem chain(PropListItem next) {
      return new IntPropListItem(getType(), intValue, next);
    }
    @Override() public String toString() {
      return String.valueOf(intValue);
    }
    @Override() public int getIntValue() {
      return intValue;
    }
  }
  
  static class NodeMismatch  {
    final Node nodeA;
    final Node nodeB;
    NodeMismatch(Node nodeA, Node nodeB) {
      super();
      this.nodeA = nodeA;
      this.nodeB = nodeB;
    }
    @Override() public boolean equals(Object object) {
      if(object instanceof NodeMismatch) {
        NodeMismatch that = (NodeMismatch)object;
        return that.nodeA.equals(this.nodeA) && that.nodeB.equals(this.nodeB);
      }
      return false;
    }
    @Override() public int hashCode() {
      return Objects.hashCode(nodeA, nodeB);
    }
  }
  
  private static class NumberNode extends Node  {
    final private static long serialVersionUID = 1L;
    private double number;
    NumberNode(double number) {
      super(Token.NUMBER);
      this.number = number;
    }
    public NumberNode(double number, int lineno, int charno) {
      super(Token.NUMBER, lineno, charno);
      this.number = number;
    }
    @Override() boolean isEquivalentTo(Node node, boolean compareJsType, boolean recurse) {
      boolean equivalent = super.isEquivalentTo(node, compareJsType, recurse);
      if(equivalent) {
        double thisValue = getDouble();
        double thatValue = ((NumberNode)node).getDouble();
        if(thisValue == thatValue) {
          return (thisValue != 0.0D) || (1 / thisValue == 1 / thatValue);
        }
      }
      return false;
    }
    @Override() public double getDouble() {
      return this.number;
    }
    @Override() public void setDouble(double d) {
      this.number = d;
    }
  }
  
  private static class ObjectPropListItem extends AbstractPropListItem  {
    final private static long serialVersionUID = 1L;
    final private Object objectValue;
    ObjectPropListItem(int propType, Object objectValue, PropListItem next) {
      super(propType, next);
      this.objectValue = objectValue;
    }
    @Override() public Object getObjectValue() {
      return objectValue;
    }
    @Override() public PropListItem chain(PropListItem next) {
      return new ObjectPropListItem(getType(), objectValue, next);
    }
    @Override() public String toString() {
      return objectValue == null ? "null" : objectValue.toString();
    }
    @Override() public int getIntValue() {
      throw new UnsupportedOperationException();
    }
  }
  
  private interface PropListItem  {
    Object getObjectValue();
    PropListItem chain(PropListItem next);
    PropListItem getNext();
    int getIntValue();
    int getType();
  }
  
  final private static class SiblingNodeIterable implements Iterable<Node>, Iterator<Node>  {
    final private Node start;
    private Node current;
    private boolean used;
    SiblingNodeIterable(Node start) {
      super();
      this.start = start;
      this.current = start;
      this.used = false;
    }
    @Override() public Iterator<Node> iterator() {
      if(!used) {
        used = true;
        return this;
      }
      else {
        return (new SiblingNodeIterable(start)).iterator();
      }
    }
    @Override() public Node next() {
      if(current == null) {
        throw new NoSuchElementException();
      }
      try {
        return current;
      }
      finally {
        current = current.getNext();
      }
    }
    @Override() public boolean hasNext() {
      return current != null;
    }
    @Override() public void remove() {
      throw new UnsupportedOperationException();
    }
  }
  
  public static class SideEffectFlags  {
    private int value = Node.SIDE_EFFECTS_ALL;
    public SideEffectFlags() {
      super();
    }
    public SideEffectFlags(int value) {
      super();
      this.value = value;
    }
    public boolean areAllFlagsSet() {
      return value == Node.SIDE_EFFECTS_ALL;
    }
    public int valueOf() {
      return value;
    }
    public void clearAllFlags() {
      value = Node.NO_SIDE_EFFECTS | Node.FLAG_LOCAL_RESULTS;
    }
    public void clearSideEffectFlags() {
      value |= Node.NO_SIDE_EFFECTS;
    }
    private void removeFlag(int flag) {
      value &= ~flag;
    }
    public void setAllFlags() {
      value = Node.SIDE_EFFECTS_ALL;
    }
    public void setMutatesArguments() {
      removeFlag(Node.FLAG_ARGUMENTS_UNMODIFIED);
    }
    public void setMutatesGlobalState() {
      removeFlag(Node.FLAG_GLOBAL_STATE_UNMODIFIED);
      removeFlag(Node.FLAG_ARGUMENTS_UNMODIFIED);
      removeFlag(Node.FLAG_THIS_UNMODIFIED);
    }
    public void setMutatesThis() {
      removeFlag(Node.FLAG_THIS_UNMODIFIED);
    }
    public void setReturnsTainted() {
      removeFlag(Node.FLAG_LOCAL_RESULTS);
    }
    public void setThrows() {
      removeFlag(Node.FLAG_NO_THROWS);
    }
  }
  
  private static class StringNode extends Node  {
    final private static long serialVersionUID = 1L;
    private String str;
    StringNode(int type, String str) {
      super(type);
      if(null == str) {
        throw new IllegalArgumentException("StringNode: str is null");
      }
      this.str = str;
    }
    StringNode(int type, String str, int lineno, int charno) {
      super(type, lineno, charno);
      if(null == str) {
        throw new IllegalArgumentException("StringNode: str is null");
      }
      this.str = str;
    }
    @Override() public String getString() {
      return this.str;
    }
    @Override() boolean isEquivalentTo(Node node, boolean compareJsType, boolean recurse) {
      return (super.isEquivalentTo(node, compareJsType, recurse) && this.str.equals(((StringNode)node).str));
    }
    @Override() public boolean isQuotedString() {
      return getBooleanProp(QUOTED_PROP);
    }
    @Override() public void setQuotedString() {
      putBooleanProp(QUOTED_PROP, true);
    }
    @Override() public void setString(String str) {
      if(null == str) {
        throw new IllegalArgumentException("StringNode: str is null");
      }
      this.str = str;
    }
  }
}