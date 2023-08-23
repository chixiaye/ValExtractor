package com.google.javascript.jscomp.jsonml;
import java.util.Arrays;

public class Validator  {
  final public static String MISSING_ARGUMENT = "" + "No %s attribute specified for %s.";
  final public static String NOT_ENOUGH_CHILDREN_FMT = "" + "Not enough children for %s. Expected: %d. Found: %d.";
  final public static String TOO_MANY_CHILDREN_FMT = "" + "Too many children for %s. Expected: %d. Found: %d.";
  final public static String WRONG_CHILD_TYPE_FMT = "" + "Wrong type of child number %d for %s. Expected: %s. Found: %s.";
  public static TagType[] exprTypes = { TagType.ArrayExpr, TagType.AssignExpr, TagType.BinaryExpr, TagType.CallExpr, TagType.ConditionalExpr, TagType.CountExpr, TagType.DeleteExpr, TagType.EvalExpr, TagType.IdExpr, TagType.InvokeExpr, TagType.LiteralExpr, TagType.LogicalAndExpr, TagType.LogicalOrExpr, TagType.MemberExpr, TagType.NewExpr, TagType.ObjectExpr, TagType.RegExpExpr, TagType.ThisExpr, TagType.TypeofExpr, TagType.UnaryExpr, TagType.FunctionExpr } ;
  final private StringBuilder b;
  private boolean error;
  private Validator() {
    super();
    b = new StringBuilder();
    error = false;
  }
  private String doValidate(JsonML element) {
    String message;
    switch (element.getType()){
      case AssignExpr:
      validateAssignExpr(element);
      break ;
      case BinaryExpr:
      validateBinaryExpr(element);
      break ;
      case BreakStmt:
      case ContinueStmt:
      validateJmpStmt(element);
      break ;
      case Case:
      validateCase(element);
      break ;
      case CatchClause:
      validateCatchClause(element);
      break ;
      case ConditionalExpr:
      validateConditionalExpr(element);
      break ;
      case CountExpr:
      validateCountExpr(element);
      break ;
      case DataProp:
      validateProp(element);
      break ;
      case GetterProp:
      validateProp(element);
      break ;
      case SetterProp:
      validateProp(element);
      break ;
      case DeleteExpr:
      validateDeleteExpr(element);
      break ;
      case DoWhileStmt:
      validateDoWhileStmt(element);
      break ;
      case EmptyStmt:
      validateEmptyStmt(element);
      break ;
      case ForInStmt:
      validateForInStmt(element);
      break ;
      case ForStmt:
      validateForStmt(element);
      break ;
      case FunctionDecl:
      validateFunctionDecl(element);
      break ;
      case FunctionExpr:
      validateFunctionExpr(element);
      break ;
      case IdExpr:
      validateIdExpr(element);
      break ;
      case IdPatt:
      validateIdPatt(element);
      break ;
      case IfStmt:
      validateIfStmt(element);
      break ;
      case InvokeExpr:
      validateInvokeExpr(element);
      break ;
      case LabelledStmt:
      validateLabelledStmt(element);
      break ;
      case LiteralExpr:
      validateLiteralExpr(element);
      break ;
      case LogicalAndExpr:
      case LogicalOrExpr:
      validateLogicalExpr(element);
      break ;
      case MemberExpr:
      validateMemberExpr(element);
      break ;
      case NewExpr:
      validateNewExpr(element);
      break ;
      case ObjectExpr:
      validateObjectExpr(element);
      break ;
      case ParamDecl:
      validateParamDecl(element);
      break ;
      case RegExpExpr:
      validateRegExpExpr(element);
      break ;
      case ReturnStmt:
      validateReturnStmt(element);
      break ;
      case SwitchStmt:
      validateSwitchStmt(element);
      break ;
      case ThisExpr:
      validateThisExpr(element);
      break ;
      case ThrowStmt:
      validateThrowStmt(element);
      break ;
      case TryStmt:
      validateTryStmt(element);
      break ;
      case TypeofExpr:
      validateTypeofExpr(element);
      break ;
      case UnaryExpr:
      validateUnaryExpr(element);
      break ;
      case VarDecl:
      validateVarDecl(element);
      break ;
      case WhileStmt:
      validateWhileStmt(element);
      break ;
      case WithStmt:
      validateWithStmt(element);
      break ;
    }
    return b.length() != 0 ? b.toString() : null;
  }
  public static String printList(Object[] list) {
    StringBuilder builder = new StringBuilder("");
    if(list.length == 1) {
      builder.append(list[0].toString());
    }
    else 
      if(list.length > 1) {
        builder.append('[');
        for(int i = 0; i < list.length; ++i) {
          builder.append(list[i].toString());
          if(i < list.length - 1) {
            builder.append(", ");
          }
        }
        builder.append("]");
      }
    return builder.toString();
  }
  public static String validate(JsonML element) {
    return (new Validator()).doValidate(element);
  }
  private void appendLine(String line) {
    b.append(String.format("%s", line));
  }
  private void validateArgument(JsonML element, TagAttr attr) {
    Object value = element.getAttribute(attr);
    if(value == null) {
      String type;
      if((type = (String)element.getAttribute(TagAttr.TYPE)) != null && type.equals("null")) {
        return ;
      }
      error = true;
      appendLine(String.format(MISSING_ARGUMENT, attr, element.getType()));
    }
  }
  private void validateAssignExpr(JsonML element) {
    validateChildrenSize(element, 2);
    validateArgument(element, TagAttr.OP);
  }
  private void validateBinaryExpr(JsonML element) {
    validateChildrenSize(element, 2);
    validateArgument(element, TagAttr.OP);
  }
  private void validateCase(JsonML element) {
    validateMinChildrenSize(element, 1);
    if(!error) {
      validateIsChildExpression(element, 0);
    }
  }
  private void validateCatchClause(JsonML element) {
    validateChildrenSize(element, 2);
    if(!error) {
      validateChildType(element, TagType.IdPatt, 0);
      validateChildType(element, TagType.BlockStmt, 1);
    }
  }
  private void validateChildType(JsonML element, TagType expected, int index) {
    TagType[] types = { expected } ;
    validateChildType(element, types, index);
  }
  private void validateChildType(JsonML element, TagType[] expected, int index) {
    TagType type = element.getChild(index).getType();
    if(!Arrays.asList(expected).contains(type)) {
      appendLine(String.format(WRONG_CHILD_TYPE_FMT, index, element.getType(), printList(expected), type));
      error = true;
    }
  }
  private void validateChildrenSize(JsonML element, int expected) {
    validateChildrenSize(element, expected, expected);
  }
  private void validateChildrenSize(JsonML element, int min, int max) {
    validateMinChildrenSize(element, min);
    if(!error) {
      validateMaxChildrenSize(element, max);
    }
  }
  private void validateConditionalExpr(JsonML element) {
    validateChildrenSize(element, 3);
  }
  private void validateCountExpr(JsonML element) {
    validateChildrenSize(element, 1);
    validateArgument(element, TagAttr.IS_PREFIX);
    validateArgument(element, TagAttr.OP);
  }
  private void validateDeleteExpr(JsonML element) {
    validateChildrenSize(element, 1);
  }
  private void validateDoWhileStmt(JsonML element) {
    validateChildrenSize(element, 2);
  }
  private void validateEmptyStmt(JsonML element) {
    validateChildrenSize(element, 0);
  }
  private void validateForInStmt(JsonML element) {
    validateChildrenSize(element, 3);
  }
  private void validateForStmt(JsonML element) {
    validateChildrenSize(element, 4);
  }
  private void validateFunction(JsonML element, boolean needsName) {
    validateMinChildrenSize(element, 2);
    if(error) {
      return ;
    }
    if(needsName) {
      validateChildType(element, new TagType[]{ TagType.IdPatt } , 0);
    }
    else {
      validateChildType(element, new TagType[]{ TagType.IdPatt, TagType.Empty } , 0);
    }
    validateChildType(element, TagType.ParamDecl, 1);
  }
  private void validateFunctionDecl(JsonML element) {
    validateFunction(element, true);
  }
  private void validateFunctionExpr(JsonML element) {
    validateFunction(element, false);
  }
  private void validateIdExpr(JsonML element) {
    validateChildrenSize(element, 0);
    if(!error) {
      validateArgument(element, TagAttr.NAME);
    }
  }
  private void validateIdPatt(JsonML element) {
    validateChildrenSize(element, 0);
    validateArgument(element, TagAttr.NAME);
  }
  private void validateIfStmt(JsonML element) {
    validateChildrenSize(element, 3);
    if(!error) {
    }
  }
  private void validateInvokeExpr(JsonML element) {
    validateMinChildrenSize(element, 2);
    validateArgument(element, TagAttr.OP);
  }
  private void validateIsChildExpression(JsonML element, int index) {
    validateChildType(element, exprTypes, index);
  }
  private void validateJmpStmt(JsonML element) {
    validateChildrenSize(element, 0);
  }
  private void validateLabelledStmt(JsonML element) {
    validateChildrenSize(element, 1);
    validateArgument(element, TagAttr.LABEL);
  }
  private void validateLiteralExpr(JsonML element) {
    validateChildrenSize(element, 0);
    validateArgument(element, TagAttr.TYPE);
    validateArgument(element, TagAttr.VALUE);
  }
  private void validateLogicalExpr(JsonML element) {
    validateChildrenSize(element, 2);
  }
  private void validateMaxChildrenSize(JsonML element, int max) {
    int size = element.childrenSize();
    if(size > max) {
      appendLine(String.format(TOO_MANY_CHILDREN_FMT, element.getType().toString(), max, size));
      error = true;
    }
  }
  private void validateMemberExpr(JsonML element) {
    validateChildrenSize(element, 2);
    validateArgument(element, TagAttr.OP);
  }
  private void validateMinChildrenSize(JsonML element, int min) {
    int size = element.childrenSize();
    if(size < min) {
      appendLine(String.format(NOT_ENOUGH_CHILDREN_FMT, element.getType(), min, size));
      error = true;
    }
  }
  private void validateNewExpr(JsonML element) {
    validateMinChildrenSize(element, 1);
  }
  private void validateObjectExpr(JsonML element) {
    TagType[] expected = { TagType.DataProp, TagType.GetterProp, TagType.SetterProp } ;
    for(int i = 0; i < element.childrenSize(); ++i) {
      validateChildType(element, expected, i);
    }
  }
  private void validateParamDecl(JsonML element) {
    for(int i = 0; i < element.childrenSize(); ++i) {
      validateChildType(element, TagType.IdPatt, i);
    }
  }
  private void validateProp(JsonML element) {
    validateChildrenSize(element, 1);
    if(!error) {
      validateArgument(element, TagAttr.NAME);
    }
  }
  private void validateRegExpExpr(JsonML element) {
    validateChildrenSize(element, 0);
    validateArgument(element, TagAttr.BODY);
    validateArgument(element, TagAttr.FLAGS);
  }
  private void validateReturnStmt(JsonML element) {
    validateMaxChildrenSize(element, 1);
  }
  private void validateSwitchStmt(JsonML element) {
    validateMinChildrenSize(element, 1);
    boolean defaultStmt = false;
    for(int i = 1; i < element.childrenSize(); ++i) {
      if(!defaultStmt) {
        validateChildType(element, new TagType[]{ TagType.Case, TagType.DefaultCase } , i);
      }
      else {
        validateChildType(element, TagType.Case, i);
      }
      if(error) {
        break ;
      }
      if(element.getChild(i).getType() == TagType.DefaultCase) {
        defaultStmt = true;
      }
    }
  }
  private void validateThisExpr(JsonML element) {
    validateChildrenSize(element, 0);
  }
  private void validateThrowStmt(JsonML element) {
    validateChildrenSize(element, 1);
  }
  private void validateTryStmt(JsonML element) {
    validateChildrenSize(element, 2, 3);
    if(error) {
      return ;
    }
    TagType var_2231 = TagType.BlockStmt;
    validateChildType(element, var_2231, 0);
    TagType[] types = new TagType[]{ TagType.CatchClause, TagType.Empty } ;
    validateChildType(element, types, 1);
    if(element.childrenSize() > 2) {
      validateChildType(element, TagType.BlockStmt, 2);
    }
  }
  private void validateTypeofExpr(JsonML element) {
    validateChildrenSize(element, 1);
  }
  private void validateUnaryExpr(JsonML element) {
    validateChildrenSize(element, 1);
    if(!error) {
      validateArgument(element, TagAttr.OP);
    }
  }
  private void validateVarDecl(JsonML element) {
    validateMinChildrenSize(element, 1);
    TagType[] types = new TagType[]{ TagType.InitPatt, TagType.IdPatt } ;
    for(int i = 0; i < element.childrenSize(); ++i) {
      validateChildType(element, types, i);
    }
  }
  private void validateWhileStmt(JsonML element) {
    validateChildrenSize(element, 2);
  }
  private void validateWithStmt(JsonML element) {
    validateChildrenSize(element, 2);
  }
}