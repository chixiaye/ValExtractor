package com.google.javascript.jscomp.jsonml;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Iterator;

public class JsonMLUtil  {
  private static JsonML parseElement(JSONArray element) throws Exception {
    JsonML jsonMLElement = new JsonML(TagType.valueOf(element.getString(0)));
    JSONObject attrs = element.getJSONObject(1);
    Iterator<?> it = attrs.keys();
    while(it.hasNext()){
      String key = (String)it.next();
      Object value = attrs.get(key);
      TagAttr tag = TagAttr.get(key);
      if(tag == null) {
        continue ;
      }
      if(value instanceof Number) {
        value = ((Number)value).doubleValue();
      }
      switch (tag){
        case NAME:
        case BODY:
        case FLAGS:
        case OP:
        case TYPE:
        case IS_PREFIX:
        case LABEL:
        jsonMLElement.setAttribute(tag, value);
        break ;
        case VALUE:
        if(value != null && value.equals(null)) {
          value = null;
        }
        if(value instanceof Number) {
          jsonMLElement.setAttribute(tag, ((Number)value).doubleValue());
        }
        else {
          jsonMLElement.setAttribute(tag, value);
        }
        break ;
        default:
      }
    }
    for(int i = 2; i < element.length(); ++i) {
      jsonMLElement.appendChild(parseElement(element.getJSONArray(i)));
    }
    return jsonMLElement;
  }
  public static JsonML parseString(String jsonml) throws Exception {
    return parseElement(new JSONArray(jsonml));
  }
  public static String compare(JsonML tree1, JsonML tree2) {
    return (new JsonMLComparator(tree1, tree2)).compare();
  }
  static boolean compareSilent(JsonML tree1, JsonML tree2) {
    return (new JsonMLComparator(tree1, tree2)).compareSilent();
  }
  public static boolean isExpression(JsonML element) {
    switch (element.getType()){
      case ArrayExpr:
      case AssignExpr:
      case BinaryExpr:
      case CallExpr:
      case ConditionalExpr:
      case CountExpr:
      case DeleteExpr:
      case EvalExpr:
      case FunctionExpr:
      case IdExpr:
      case InvokeExpr:
      case LiteralExpr:
      case LogicalAndExpr:
      case LogicalOrExpr:
      case MemberExpr:
      case NewExpr:
      case ObjectExpr:
      case RegExpExpr:
      case ThisExpr:
      case TypeofExpr:
      case UnaryExpr:
      return true;
      default:
      return false;
    }
  }
  
  private static class JsonMLComparator  {
    final private static TagAttr[] ATTRS_TO_COMPARE = { TagAttr.BODY, TagAttr.FLAGS, TagAttr.IS_PREFIX, TagAttr.LABEL, TagAttr.NAME, TagAttr.OP, TagAttr.TYPE, TagAttr.VALUE } ;
    private JsonML treeA;
    private JsonML treeB;
    private JsonML mismatchA;
    private JsonML mismatchB;
    JsonMLComparator(JsonML treeA, JsonML treeB) {
      super();
      this.treeA = treeA;
      this.treeB = treeB;
      if(compareElements(treeA, treeB)) {
        mismatchA = null;
        mismatchB = null;
      }
    }
    private String compare() {
      if(compareSilent()) {
        return null;
      }
      return "The trees are not equal: " + "\n\nTree1:\n " + treeA.toStringTree() + "\n\nTree2:\n " + treeB.toStringTree() + "\n\nSubtree1:\n " + mismatchA.toStringTree() + "\n\nSubtree2:\n " + mismatchB.toStringTree();
    }
    private boolean areEquivalent(JsonML a, JsonML b) {
      if(a.getType() != b.getType()) {
        return false;
      }
      for (TagAttr attr : ATTRS_TO_COMPARE) {
        if(!compareAttribute(attr, a, b)) {
          return false;
        }
      }
      return true;
    }
    private boolean compareAttribute(TagAttr attr, JsonML a, JsonML b) {
      Object valueA = a.getAttributes().get(attr);
      Object valueB = b.getAttributes().get(attr);
      if(valueA == null && valueB == null) {
        return true;
      }
      if(valueA == null || valueB == null) {
        return false;
      }
      if(!(valueA.equals(valueB))) {
        Double doubleA = null;
        Double doubleB = null;
        if(valueA instanceof Number) {
          doubleA = ((Number)valueA).doubleValue();
        }
        else 
          if(valueA instanceof String) {
            doubleA = Double.valueOf((String)valueA);
          }
          else {
            return false;
          }
        if(valueB instanceof Number) {
          doubleB = ((Number)valueB).doubleValue();
        }
        else 
          if(valueB instanceof String) {
            doubleB = Double.valueOf((String)valueB);
          }
          else {
            return false;
          }
        if(!doubleA.equals(doubleB)) {
          return false;
        }
      }
      return true;
    }
    private boolean compareElements(JsonML a, JsonML b) {
      if(a == null || b == null) {
        if(a == null && b == null) {
          return true;
        }
        else {
          return setMismatch(a, b);
        }
      }
      if(!areEquivalent(a, b)) {
        boolean var_2187 = setMismatch(a, b);
        return var_2187;
      }
      if(a.childrenSize() != b.childrenSize()) {
        return setMismatch(a, b);
      }
      Iterator<JsonML> itA = a.getChildren().listIterator();
      Iterator<JsonML> itB = b.getChildren().listIterator();
      while(itA.hasNext()){
        if(!compareElements(itA.next(), itB.next())) {
          return false;
        }
      }
      return true;
    }
    private boolean compareSilent() {
      return mismatchA == null && mismatchB == null;
    }
    private boolean setMismatch(JsonML a, JsonML b) {
      mismatchA = a;
      mismatchB = b;
      return false;
    }
  }
}