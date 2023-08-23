package extractvariable.ast.rw.writer;

import extractvariable.ast.EntityVisitor;
import extractvariable.ast.Field;
import extractvariable.ast.rw.RWVisitor;
import extractvariable.ast.rw.reader.ReaderVisitor;
import extractvariable.detector.Comparator;
import extractvariable.detector.NullChecker;
import valextractor.log.MyLog;
import valextractor.utils.ASTNodeHandleUtils;
import valextractor.utils.Constants;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.*;

import data.json.APIJson;
import data.json.HandleAPIJsonFile;

import static valextractor.utils.ASTNodeHandleUtils.findInFieldRecords;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

public class WriterVisitor extends RWVisitor {
	boolean staticFlag;

	private boolean addElemToWriterSet(String name, ASTNode astNode) {
		if (name.startsWith("this.")) {
			name = name.substring(5);
		}
		if (astNode instanceof NumberLiteral) {
			return false;
		} else if (astNode instanceof FieldAccess) {
			IVariableBinding variableBinding = ((FieldAccess) astNode).resolveFieldBinding();

			if (variableBinding == null || variableBinding.isField() == false) {
				return false;
			}
			if (Modifier.isStatic(variableBinding.getModifiers())) {
				name = variableBinding.getDeclaringClass().getQualifiedName() + "." + variableBinding.getName();
			}
			if (Modifier.isStatic(variableBinding.getModifiers()) || findInFieldRecords(astNode, key) || key == owner) {
				name = name.replaceAll("null\\.", "");
				addToAPIList(name, variableBinding);
				staticFlag = true;
			}
			return true;
		} else if (astNode instanceof QualifiedName) {
			IBinding iBinding = ((QualifiedName) astNode).resolveBinding();
			if (iBinding instanceof IVariableBinding) {
				IVariableBinding variableBinding = (IVariableBinding) iBinding;
				Name qualifier = ((QualifiedName) astNode).getQualifier();
				while (qualifier instanceof QualifiedName) {
					qualifier = ((QualifiedName) qualifier).getQualifier();
				}
				if (qualifier instanceof SimpleName) {
					SimpleName sn = (SimpleName) qualifier;
					IBinding svb = sn.resolveBinding();
					if (this.paramHashmap.get(sn.toString()) == null && this.layer != 0 && svb != null
							&& svb.getJavaElement() != null
							&& svb.getJavaElement().getElementType() == IJavaElement.LOCAL_VARIABLE) {

						if (!(svb instanceof IVariableBinding
								&& (((IVariableBinding) (svb)).isParameter() || ((IVariableBinding) (svb)).isField()
										|| ((IVariableBinding) (svb)).isEnumConstant()))) {
							return false;
						}
					}
				}
				if (Modifier.isStatic(variableBinding.getModifiers())) {
					name = variableBinding.getDeclaringClass().getQualifiedName() + "." + variableBinding.getName();
				}
				name = name.replaceAll("null\\.", "");
				if (variableBinding.getType().isArray()) {
					name = name + "[]";
				}
				if (Modifier.isStatic(variableBinding.getModifiers()) || findInFieldRecords(astNode, key)
						|| key == owner) {
					addToAPIList(name, variableBinding);
					staticFlag = true;
					return true;
				}
			}
		} else if (astNode instanceof SimpleName) {
			IBinding iBinding = ((SimpleName) astNode).resolveBinding();
			if (iBinding instanceof IVariableBinding) {

				IVariableBinding variableBinding = (IVariableBinding) iBinding;
				if (this.layer != 0 && variableBinding != null && variableBinding.getJavaElement() != null
						&& variableBinding.getJavaElement().getElementType() == IJavaElement.LOCAL_VARIABLE) {
					if (!(variableBinding instanceof IVariableBinding
							&& (((IVariableBinding) (variableBinding)).isParameter()
									|| ((IVariableBinding) (variableBinding)).isField()
									|| ((IVariableBinding) (variableBinding)).isEnumConstant()))) {
						return true;
					}
				}
				if (Modifier.isStatic(variableBinding.getModifiers())) {
					name = variableBinding.getDeclaringClass().getQualifiedName() + "." + variableBinding.getName();
				}
				name = name.replaceAll("null\\.", "");
				if (variableBinding.getType().isArray()) {
					name = name + "[]";
				}
				if (Modifier.isStatic(variableBinding.getModifiers()) || findInFieldRecords(astNode, key)
						|| key == owner) {
					addToAPIList(name, variableBinding);
					if (Modifier.isStatic(variableBinding.getModifiers()))
						staticFlag = true;
					return true;
				}
			}
		} else if (astNode instanceof ArrayAccess) {
			Expression arrayExpression = ((ArrayAccess) astNode).getArray();
			if (arrayExpression instanceof SimpleName) {
				IBinding iBinding = ((SimpleName) arrayExpression).resolveBinding();
				if (iBinding instanceof IVariableBinding) {
					IVariableBinding variableBinding = (IVariableBinding) iBinding;
					if (Modifier.isStatic(variableBinding.getModifiers())) {
						name = variableBinding.getDeclaringClass().getQualifiedName() + "." + variableBinding.getName();
					}
					name = name.replaceAll("null\\.", "");
					if (Modifier.isStatic(variableBinding.getModifiers()) || findInFieldRecords(astNode, key)
							|| key == owner) {
						addToAPIList(name, variableBinding);
						if (Modifier.isStatic(variableBinding.getModifiers()))
							staticFlag = true;
						return true;
					}
				}
			}
		}
		return false;
	}

	public WriterVisitor(HashSet<ITypeBinding> allBindings, String key, String owner, String methodKey, int middleFlag,
			ASTNode extractVariable, int layer, boolean isLocal, List<String> argList) {
		super(layer, isLocal, middleFlag, argList);
		staticFlag = false;
		this.methodKey = methodKey;
		this.allBindings = allBindings;
		this.key = key;
		this.owner = owner;
		this.APIList = new ArrayList<>();
		this.extractVariable = extractVariable;
	}

	@Override
	public boolean visit(Assignment assignment) {
		super.visit(assignment);
		String op = assignment.getOperator().toString();
		if (op.contains("=")) {
			if (this.middleFlag == -1 && op.equals("=")) {
				ASTNode temp = assignment;
				while (!(temp instanceof IfStatement) && !(temp instanceof MethodDeclaration)) {
					temp = temp.getParent();
				}
				if (temp instanceof IfStatement) {
					Expression exp = ((IfStatement) temp).getExpression();
					NullChecker nc = new NullChecker(exp, assignment.getLeftHandSide(), exp.getStartPosition(),
							exp.getStartPosition() + exp.getLength(), 1);
					if (nc.isExistNull()) {
						return false;
					}
				}
			}
			if (assignment.getLeftHandSide() instanceof SimpleName) {
				SimpleName sn = (SimpleName) (assignment.getLeftHandSide());
				if (this.layer != 0 && sn.resolveBinding() != null && sn.resolveBinding().getJavaElement() != null
						&& sn.resolveBinding().getJavaElement().getElementType() == IJavaElement.LOCAL_VARIABLE) {
					if ((sn.resolveBinding() instanceof IVariableBinding
							&& ((IVariableBinding) (sn.resolveBinding())).isParameter())) {
						return true;
					}
				}
			}

			addElemToWriterSet(assignment.getLeftHandSide().toString(), assignment.getLeftHandSide());
		}
		return true;
	}

	@Override
	public boolean visit(PrefixExpression prefixExpression) {
		if (valextractor.utils.Utils.isStartWithNumber(prefixExpression.getOperand().toString())
				|| prefixExpression.getOperand().toString().startsWith(".")
				|| prefixExpression.getOperator().toString().equals("!")
				|| prefixExpression.getOperator().toString().equals("-")
				|| prefixExpression.getOperator().toString().equals("/")
				|| prefixExpression.getOperator().toString().equals("~")) {
			return false;
		} else {
			addElemToWriterSet(prefixExpression.getOperand().toString(), prefixExpression.getOperand());
		}
		return true;
	}

	@Override
	public boolean visit(PostfixExpression postfixExpression) {
		if (valextractor.utils.Utils.isStartWithNumber(postfixExpression.getOperand().toString())
				|| postfixExpression.getOperand().toString().startsWith(".")) {
			return false;
		} else {
			addElemToWriterSet(postfixExpression.getOperand().toString(), postfixExpression.getOperand());
		}
		return true;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		IMethodBinding iMethodBinding = methodInvocation.resolveMethodBinding();
		MethodDeclaration targetFunctionDefinition = null;
		ITypeBinding itb = null;
		if (iMethodBinding == null) {
			return true;
		}
		if (iMethodBinding != null && iMethodBinding.getKey().equals(this.methodKey)) {
			return true;
		}
		List<String> args = new ArrayList<String>();
		List<ASTNode> methodInvocationArgList = methodInvocation.arguments();
		for (int i = 0; i < methodInvocationArgList.size(); ++i) {
			ASTNode astNode = methodInvocationArgList.get(i);
			args.add(astNode.toString());
		}
		if (Modifier.isNative(iMethodBinding.getModifiers()) && this.layer == 0 && this.middleFlag != 1) {
			String s = iMethodBinding.getDeclaringClass().getQualifiedName() + "." + iMethodBinding.getName();
			Comparator.visitedNativeMethodSet.add(s);
			return true;
		}
		if (methodInvocation.getExpression() != null) {
			itb = methodInvocation.getExpression().resolveTypeBinding();
			targetFunctionDefinition = ASTNodeHandleUtils.findFunctionDefinition(itb, iMethodBinding);
		} 
		if( targetFunctionDefinition==null){
			itb = iMethodBinding.getDeclaringClass();
			targetFunctionDefinition = ASTNodeHandleUtils.findFunctionDefinition(itb, iMethodBinding);
			
		}
		Expression temp=methodInvocation.getExpression();
		while (temp instanceof Expression ) {
			if(temp instanceof MethodInvocation)
				temp=((MethodInvocation)temp).getExpression();
			else if(temp instanceof QualifiedName)
				temp=((QualifiedName)temp).getQualifier();
			else 
				break;
			if(temp instanceof SimpleName)
				break;
		}
		if ( temp instanceof SimpleName) {
			SimpleName sn = (SimpleName) temp;
			if (this.layer != 0 && sn.resolveBinding() != null && sn.resolveBinding().getJavaElement() != null
					&& sn.resolveBinding().getJavaElement().getElementType() == IJavaElement.LOCAL_VARIABLE) {
				if (!(sn.resolveBinding() instanceof IVariableBinding
						&& (((IVariableBinding) (sn.resolveBinding())).isParameter()
								|| ((IVariableBinding) (sn.resolveBinding())).isField()
								|| ((IVariableBinding) (sn.resolveBinding())).isEnumConstant()))) {
					return true;
				}
			}
		}
		if (targetFunctionDefinition != null) {
			IMethodBinding resolveBinding = targetFunctionDefinition.resolveBinding();
			if (resolveBinding == null) {
				return true;
			}
			EntityVisitor entityVisitor = new EntityVisitor(resolveBinding.getDeclaringClass());
			targetFunctionDefinition.accept(entityVisitor);
			String tempOwner = methodInvocation.getExpression() == null ? this.owner
					: methodInvocation.getExpression().toString();
			if (this.paramHashmap.containsKey(tempOwner)) {
				tempOwner = this.paramHashmap.get(tempOwner);
			}
			String tempKey = methodInvocation.getExpression() == null ? this.key
					: resolveBinding.getDeclaringClass().getKey();
			WriterVisitor writerVisitor = new WriterVisitor(entityVisitor.getTargetClass(), tempKey, tempOwner,
					iMethodBinding.getKey(), this.middleFlag, this.extractVariable, layer + 1, false, args);
			targetFunctionDefinition.accept(writerVisitor);
			if (this.staticFlag == false) {
				this.staticFlag = writerVisitor.isStaticFlag();
			}
			List<String> writerList = writerVisitor.getAPIList();
			writerList.forEach(k -> APIList.add(k));
		} else if (this.layer < 2) {
			String apiPath = iMethodBinding.getMethodDeclaration().getDeclaringClass().getBinaryName().replace(".", "/")
					+ ".json";

			HashMap<String, APIJson> apiJsonHashMap;
			try {
				apiJsonHashMap = HandleAPIJsonFile.JsonParser(Constants.JDK_Prefix + apiPath);
				if (apiJsonHashMap == null
						|| apiJsonHashMap.get(iMethodBinding.getMethodDeclaration().getKey()) == null) {
					return true;
				}
				APIJson apiJson = apiJsonHashMap.get(iMethodBinding.getMethodDeclaration().getKey());
				List<Field> writerList = apiJson.getWriteList();
				writerList.forEach(k -> {
//					System.out.println(methodInvocation.getExpression() + "." + k.getName());
					
					if (k.getIsStatic() == 1) {
					} else {
						APIList.add(methodInvocation.getExpression() + "." + k.getName());
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation classInstanceCreation) {
		IMethodBinding iMethodBinding = classInstanceCreation.resolveConstructorBinding(); 
		if (iMethodBinding == null || iMethodBinding.getKey().equals(this.methodKey)) {
			return true;
		} 
		if (classInstanceCreation.getExpression() instanceof SimpleName) {
			SimpleName sn = (SimpleName) (classInstanceCreation.getExpression());
			if (this.layer != 0 && sn.resolveBinding() != null && sn.resolveBinding().getJavaElement() != null
					&& sn.resolveBinding().getJavaElement().getElementType() == IJavaElement.LOCAL_VARIABLE) {
				if (!(sn.resolveBinding() instanceof IVariableBinding
						&& (((IVariableBinding) (sn.resolveBinding())).isParameter()
								|| ((IVariableBinding) (sn.resolveBinding())).isField()
								|| ((IVariableBinding) (sn.resolveBinding())).isEnumConstant()))) {
					return true;
				}
			}
		}
		return true;
	}

	public boolean isStaticFlag() {
		return staticFlag;
	}

	private String convertName(String name, ASTNode astNode) {
		if (name.startsWith("this.")) {
			name = name.substring(5);
		}
		if (astNode instanceof NumberLiteral) {
			return null;
		} else if (astNode instanceof FieldAccess) {
			IVariableBinding variableBinding = ((FieldAccess) astNode).resolveFieldBinding();

			if (variableBinding == null || variableBinding.isField() == false) {
				return null;
			}
			if (Modifier.isStatic(variableBinding.getModifiers())) {
				name = variableBinding.getDeclaringClass().getQualifiedName() + "." + variableBinding.getName();
			}
			if (Modifier.isStatic(variableBinding.getModifiers()) || findInFieldRecords(astNode, key) || key == owner) {
				name = name.replaceAll("null\\.", "");
			}
		} else if (astNode instanceof QualifiedName) {
			IBinding iBinding = ((QualifiedName) astNode).resolveBinding();
			if (iBinding instanceof IVariableBinding) {
				IVariableBinding variableBinding = (IVariableBinding) iBinding;

				if (Modifier.isStatic(variableBinding.getModifiers())) {
					name = variableBinding.getDeclaringClass().getQualifiedName() + "." + variableBinding.getName();
				}
				name = name.replaceAll("null\\.", "");
				if (variableBinding.getType().isArray()) {
					name = name + "[]";
				}
			}
		} else if (astNode instanceof SimpleName) {
			IBinding iBinding = ((SimpleName) astNode).resolveBinding();
			if (iBinding instanceof IVariableBinding) {
				IVariableBinding variableBinding = (IVariableBinding) iBinding;
				if (Modifier.isStatic(variableBinding.getModifiers())) {
					name = variableBinding.getDeclaringClass().getQualifiedName() + "." + variableBinding.getName();
				}
				name = name.replaceAll("null\\.", "");
				if (variableBinding.getType().isArray()) {
					name = name + "[]";
				}
			}
		} else if (astNode instanceof ArrayAccess) {
		}
		return name;
	}

	@Override
	public boolean visit(ReturnStatement node) {
		if (this.layer == 1 && this.middleFlag == -1) {
			if (node.getExpression() instanceof ClassInstanceCreation) {
				Comparator.visitedNativeMethodSet.add("return new instance");
			}
		}
		return super.visit(node);
	}

}