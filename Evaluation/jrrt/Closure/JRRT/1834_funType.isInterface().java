package com.google.javascript.jscomp;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.ObjectType;
import java.util.Set;

class TypedCodeGenerator extends CodeGenerator  {
  TypedCodeGenerator(CodeConsumer consumer, CompilerOptions options) {
    super(consumer, options);
  }
  private String getFunctionAnnotation(Node fnNode) {
    Preconditions.checkState(fnNode.isFunction());
    StringBuilder sb = new StringBuilder("/**\n");
    JSType type = fnNode.getJSType();
    if(type == null || type.isUnknownType()) {
      return "";
    }
    FunctionType funType = type.toMaybeFunctionType();
    if(fnNode != null) {
      Node paramNode = NodeUtil.getFunctionParameters(fnNode).getFirstChild();
      for (Node n : funType.getParameters()) {
        if(paramNode == null) {
          break ;
        }
        sb.append(" * ");
        appendAnnotation(sb, "param", getParameterNodeJSDocType(n));
        sb.append(" ").append(paramNode.getString()).append("\n");
        paramNode = paramNode.getNext();
      }
    }
    JSType retType = funType.getReturnType();
    if(retType != null && !retType.isUnknownType() && !retType.isEmptyType()) {
      sb.append(" * ");
      appendAnnotation(sb, "return", retType.toAnnotationString());
      sb.append("\n");
    }
    if(funType.isConstructor() || funType.isInterface()) {
      FunctionType superConstructor = funType.getSuperClassConstructor();
      if(superConstructor != null) {
        ObjectType superInstance = funType.getSuperClassConstructor().getInstanceType();
        if(!superInstance.toString().equals("Object")) {
          sb.append(" * ");
          appendAnnotation(sb, "extends", superInstance.toAnnotationString());
          sb.append("\n");
        }
      }
      boolean var_1834 = funType.isInterface();
      if(var_1834) {
        for (ObjectType interfaceType : funType.getExtendedInterfaces()) {
          sb.append(" * ");
          appendAnnotation(sb, "extends", interfaceType.toAnnotationString());
          sb.append("\n");
        }
      }
      Set<String> interfaces = Sets.newTreeSet();
      for (ObjectType interfaze : funType.getImplementedInterfaces()) {
        interfaces.add(interfaze.toAnnotationString());
      }
      for (String interfaze : interfaces) {
        sb.append(" * ");
        appendAnnotation(sb, "implements", interfaze);
        sb.append("\n");
      }
      if(funType.isConstructor()) {
        sb.append(" * @constructor\n");
      }
      else 
        if(funType.isInterface()) {
          sb.append(" * @interface\n");
        }
    }
    if(fnNode != null && fnNode.getBooleanProp(Node.IS_DISPATCHER)) {
      sb.append(" * @javadispatch\n");
    }
    sb.append(" */\n");
    return sb.toString();
  }
  private String getParameterNodeJSDocType(Node parameterNode) {
    JSType parameterType = parameterNode.getJSType();
    String typeString;
    if(parameterType.isUnknownType()) {
      typeString = "*";
    }
    else {
      if(parameterNode.isOptionalArg()) {
        typeString = parameterType.restrictByNotNullOrUndefined().toAnnotationString() + "=";
      }
      else 
        if(parameterNode.isVarArgs()) {
          typeString = "..." + parameterType.restrictByNotNullOrUndefined().toAnnotationString();
        }
        else {
          typeString = parameterType.toAnnotationString();
        }
    }
    return typeString;
  }
  private String getTypeAnnotation(Node node) {
    JSDocInfo jsdoc = NodeUtil.getBestJSDocInfo(node);
    if(jsdoc == null && !node.isFunction()) {
      return "";
    }
    JSType type = node.getJSType();
    if(type == null) {
      return "";
    }
    else 
      if(type.isFunctionType()) {
        return getFunctionAnnotation(node);
      }
      else 
        if(type.isEnumType()) {
          return "/** @enum {" + type.toMaybeEnumType().getElementsType().toAnnotationString() + "} */\n";
        }
        else 
          if(!type.isUnknownType() && !type.isEmptyType() && !type.isVoidType() && !type.isFunctionPrototypeType()) {
            return "/** @type {" + node.getJSType().toAnnotationString() + "} */\n";
          }
          else {
            return "";
          }
  }
  @Override() void add(Node n, Context context) {
    Node parent = n.getParent();
    if(parent != null && (parent.isBlock() || parent.isScript())) {
      if(n.isFunction()) {
        add(getFunctionAnnotation(n));
      }
      else 
        if(n.isExprResult() && n.getFirstChild().isAssign()) {
          Node rhs = n.getFirstChild().getLastChild();
          add(getTypeAnnotation(rhs));
        }
        else 
          if(n.isVar() && n.getFirstChild().getFirstChild() != null) {
            add(getTypeAnnotation(n.getFirstChild().getFirstChild()));
          }
    }
    super.add(n, context);
  }
  private void appendAnnotation(StringBuilder sb, String name, String type) {
    sb.append("@").append(name).append(" {").append(type).append("}");
  }
}