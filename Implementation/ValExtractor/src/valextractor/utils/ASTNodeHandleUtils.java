package valextractor.utils;

import extractvariable.api.APIFieldRecord;
import extractvariable.api.APIRecorder;
import extractvariable.ast.ASTPart;
import extractvariable.ast.LightASTParser;
import extractvariable.detector.Comparator;
import valextractor.log.MyLog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ASTNodeHandleUtils {
	public static boolean isParameterizedType(Type type) {
		if (type == null && type.resolveBinding() != null) {
			return true;
		}
		// string int byte boolean short long float double char
		// Short Integer Long Character Byte Float Boolean Double
		String[] arr = { "String", "char", "int", "long", "float", "double", "byte", "boolean", "short", "Integer",
				"Short", "Long", "Charater", "Byte", "Float", "Boolean", "Double" };

		String name = type.resolveBinding().getName();
		for (String s : arr) {
			if (name.equals(s)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isParameterizedType(String name) {

		String[] arr = { "String", "char", "int", "long", "float", "double", "byte", "boolean", "short", "Integer",
				"Short", "Long", "Charater", "Byte", "Float", "Boolean", "Double" };

		for (String s : arr) {
			if (name.equals(s)) {
				return true;
			}
		}
		return false;
	}

	public static ArrayList<ASTNode> generateNodeVisitArrayList(ASTNode node) {
		ArrayList<ASTNode> nodeArrayList = new ArrayList<ASTNode>();
		ASTNode tempASTNode = node;
		while (tempASTNode != null) {
			nodeArrayList.add(tempASTNode);
			tempASTNode = tempASTNode.getParent();
		}
		return nodeArrayList;
	}

	public static ASTNode findMinimumCommonParentNode(ArrayList<ASTNode> firstNodeVisitArrayList,
			ArrayList<ASTNode> secondNodeVisitArrayList) {
		int len1 = firstNodeVisitArrayList.size();
		int len2 = secondNodeVisitArrayList.size();
		int i = len1 - 1;
		int j = len2 - 1;
		ASTNode resNode = null;
		while (i >= 0 && j >= 0) {
			if (isSameNode(firstNodeVisitArrayList.get(i).hashCode(), secondNodeVisitArrayList.get(j).hashCode())) {
				resNode = firstNodeVisitArrayList.get(i);
				i--;
				j--;
			} else {
				break;
			}
		}
		return resNode;
	}

	private static boolean isSameNode(int a, int b) {
		return a == b;
	}
	
	private static void findTypes(IType it, ITypeHierarchy ith, ArrayList<IType> iTypes) {
		IType[] allSubtypes = ith.getAllSubtypes(it);
		for (IType i : allSubtypes) {
			iTypes.add(i);
		}
		iTypes.add(it);
		return;
	}

	public static MethodDeclaration findFunctionDefinition(ITypeBinding iTypeBinding, IMethodBinding methodBinding) {
		if (methodBinding == null || iTypeBinding == null) {
			return null;
		}
		if (!(iTypeBinding.getJavaElement() instanceof IType))
			return null;
		IType it = (IType) (iTypeBinding.getJavaElement());
		try {
			IJavaElement root = it.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
			if (root instanceof IPackageFragmentRoot) {
				IClasspathEntry cpEntry = ((IPackageFragmentRoot) root).getRawClasspathEntry();
				if (cpEntry.getEntryKind() == IClasspathEntry.CPE_CONTAINER
						&& cpEntry.getPath().toString().startsWith("org.eclipse.jdt.launching.JRE_CONTAINER")) { //$NON-NLS-1$
					return null;
				}
			}
			ITypeHierarchy ith = it.newTypeHierarchy(iTypeBinding.getJavaElement().getJavaProject(), null);
			IMethod iMethod = (IMethod) methodBinding.getJavaElement();
			if (iMethod == null || ith == null) {
				return null;
			}

			ArrayList<IType> iTypes = new ArrayList<>();
			findTypes(it, ith, iTypes);
			 
			for (IType t : iTypes) {
				IMethod tmp = JavaModelUtil.findMethod(iMethod.getElementName(), iMethod.getParameterTypes(), false, t);
				if (tmp != null && !Flags.isAbstract(tmp.getFlags()) ) { 
					LightASTParser lightASTParser = new LightASTParser(tmp.getCompilationUnit());
					if(lightASTParser.getCompilationUnit()==null) { 
						return null;
					}
					NodeFinder finder = new NodeFinder(lightASTParser.getCompilationUnit(), tmp.getSourceRange().getOffset(),
							tmp.getSourceRange().getLength()); 
					ASTNode node = finder.getCoveredNode();
					while (node != null && !(node instanceof MethodDeclaration)) {
						node = node.getParent();
					} 
					if (node instanceof MethodDeclaration ) {
						MethodDeclaration md = (MethodDeclaration) node;
						if ( md.getBody()==null || md.getBody().getLength() > Constants.MAX_OFFSET)
							continue; 
						return md; 
					}
				}
			}
		} catch (JavaModelException e) {
		}
		return null;
	}

	public static Boolean findInFieldRecords(ASTNode astNode, String key) {
		HashSet<APIFieldRecord> apiFieldRecorder = new HashSet<>();
		if (valextractor.utils.GlobalClass.recorderHashMap != null
				&& valextractor.utils.GlobalClass.recorderHashMap.get(key) != null) {
			APIRecorder apiRecorder = valextractor.utils.GlobalClass.recorderHashMap.get(key);

			apiFieldRecorder = apiRecorder.getApiFieldRecordHashSet();
		} else {
			return false;
		}
		if (astNode instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) astNode;
			IBinding iBinding = fieldAccess.resolveFieldBinding();
			if (iBinding instanceof IVariableBinding) {
				IVariableBinding variableBinding = (IVariableBinding) iBinding;
				if (variableBinding.isField()) {
					APIFieldRecord apiFieldRecord = new APIFieldRecord(variableBinding);
					if (apiFieldRecord.getIVariableBinding().getVariableDeclaration() != null) {
						for (APIFieldRecord afr : apiFieldRecorder) {
							String tatget = apiFieldRecord.getIVariableBinding().getVariableDeclaration().getKey();
							if (apiFieldRecord.getIVariableBinding().getDeclaringClass() != null
									&& afr.getIVariableBinding().getVariableDeclaration().getKey().equals(tatget)) {
								return true;
							}
						}
					}
				}
			}
		} else if (astNode instanceof QualifiedName) {
			QualifiedName qualifiedName = (QualifiedName) astNode;
			IBinding iBinding = qualifiedName.getQualifier().resolveBinding();
			if (iBinding instanceof IVariableBinding) {
				IVariableBinding variableBinding = (IVariableBinding) iBinding;
				if (variableBinding.isField()) {
					APIFieldRecord apiFieldRecord = new APIFieldRecord(variableBinding);
					if (apiFieldRecord.getIVariableBinding().getVariableDeclaration() != null) {
						for (APIFieldRecord afr : apiFieldRecorder) {
							String tatget = apiFieldRecord.getIVariableBinding().getVariableDeclaration().getKey();
							if (apiFieldRecord.getIVariableBinding().getDeclaringClass() != null
									&& afr.getIVariableBinding().getVariableDeclaration().getKey().equals(tatget)) {
								return true;
							}
						}
					}
				}
			}
		} else if (astNode instanceof SimpleName) {
			SimpleName simpleName = (SimpleName) astNode;
			IBinding iBinding = simpleName.resolveBinding();
			if (iBinding instanceof IVariableBinding) {
				IVariableBinding variableBinding = (IVariableBinding) iBinding;
				APIFieldRecord apiFieldRecord = new APIFieldRecord(variableBinding);

				if (variableBinding.isField()) {
					if (apiFieldRecord.getIVariableBinding().getVariableDeclaration() != null) {
						for (APIFieldRecord afr : apiFieldRecorder) {
							String tatget = apiFieldRecord.getIVariableBinding().getVariableDeclaration().getKey();
							if (apiFieldRecord.getIVariableBinding().getDeclaringClass() != null
									&& afr.getIVariableBinding().getVariableDeclaration().getKey().equals(tatget)) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

}
