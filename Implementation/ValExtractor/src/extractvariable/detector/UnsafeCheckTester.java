package extractvariable.detector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.internal.corext.dom.fragments.ASTFragmentFactory;
import org.eclipse.jdt.internal.corext.dom.fragments.IASTFragment;

public class UnsafeCheckTester {
	private ASTNode fExpression;

	private int fStartOffset;

	private int fEndOffset;

	private ASTNode fCommonNode;

	private CompilationUnit fCompilationUnitNode;

	private ICompilationUnit fCu;

	private Set<Position> fMatchNodePosSet;

	private Set<IBinding> fInvocationSet;

	private HashMap<IBinding, ITypeBinding> fInvocationHashMap;

	private HashMap<Position, ITypeBinding> fMatchNodePosHashMap;

	class Position {
		int start;

		int length;

		public Position(int start, int length) {
			this.start = start;
			this.length = length;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + length;
			result = prime * result + start;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof Position))
				return false;
			Position other = (Position) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			if (length != other.length)
				return false;
			if (start != other.start)
				return false;
			return true;
		}

		private UnsafeCheckTester getEnclosingInstance() {
			return UnsafeCheckTester.this;
		}
	}

	public UnsafeCheckTester(CompilationUnit fCompilationUnitNode, ICompilationUnit fCu, ASTNode commonNode,
			ASTNode expression, int startOffset, int endOffset) {
		this.fCompilationUnitNode = fCompilationUnitNode;
		this.fCu = fCu;
		this.fCommonNode = commonNode;
		this.fExpression = expression;
		this.fStartOffset = startOffset;
		this.fEndOffset = endOffset;
		this.fMatchNodePosSet = new HashSet<>();
		this.fInvocationSet = new HashSet<>();
		this.fInvocationHashMap = new HashMap<>();
		this.fMatchNodePosHashMap = new HashMap<>();
		InvocationVisitor iv = new InvocationVisitor();
		this.fExpression.accept(iv);
	}

	public boolean hasUnsafeCheck() {
		MiddleCodeVisitor middleCodeVisitor = new MiddleCodeVisitor(this.fStartOffset, this.fEndOffset);
		this.fCommonNode.accept(middleCodeVisitor);
		return middleCodeVisitor.hasNullCheck() || middleCodeVisitor.hasCastCheck();
	}

	private IASTFragment getIASTFragment(ASTNode astNode) throws JavaModelException {
		int length = astNode.getLength();
		int startPosition = astNode.getStartPosition();
		return ASTFragmentFactory.createFragmentForSourceRange(new SourceRange(startPosition, length),
				this.fCompilationUnitNode, this.fCu);

	}

	private ASTNode getEnclosingBodyNode(ASTNode node) {
		StructuralPropertyDescriptor location = null;
		while (node != null && !(node instanceof BodyDeclaration)) {
			location = node.getLocationInParent();
			node = node.getParent();
			if (node instanceof LambdaExpression) {
				break;
			}
		}
		if (node != null) {
			if (location == MethodDeclaration.BODY_PROPERTY || location == Initializer.BODY_PROPERTY
					|| (location == LambdaExpression.BODY_PROPERTY
							&& ((LambdaExpression) node).resolveMethodBinding() != null)) {
				return (ASTNode) node.getStructuralProperty(location);
			}
		}
		return null;
	}

	private Expression getOriginalExpression(Expression expr) {
		while (expr instanceof ParenthesizedExpression || expr instanceof CastExpression) {
			if (expr instanceof ParenthesizedExpression) {
				ParenthesizedExpression pe = (ParenthesizedExpression) expr;
				expr = pe.getExpression();
			} else {
				CastExpression ce = (CastExpression) expr;
				expr = ce.getExpression();
			}
		}
		return expr;

	}

	private class InvocationVisitor extends ASTVisitor {

		@Override
		public void preVisit(ASTNode node) {
			Expression temp = null;
			if (node instanceof CastExpression) {
				CastExpression castExpression = (CastExpression) node;
				ITypeBinding resolveBinding = castExpression.getType().resolveBinding();
				Expression expression = getOriginalExpression(castExpression.getExpression());
				IBinding targetBinding = null;
				if (expression instanceof Name && (targetBinding = ((Name) expression).resolveBinding()) != null) {
					fInvocationHashMap.put(targetBinding, resolveBinding);
				} else {
					fMatchNodePosHashMap.put(new Position(expression.getStartPosition(), expression.getLength()),
							resolveBinding);
				}
			} else if (node instanceof MethodInvocation) {
				MethodInvocation mi = (MethodInvocation) node;
				IMethodBinding rmb = mi.resolveMethodBinding();
				String str = rmb == null ? "" : rmb.getMethodDeclaration().getKey();
				if (str.equals("Ljava/lang/Class;.cast(Ljava/lang/Object;)TT;")
						&& mi.getExpression() instanceof TypeLiteral) {
					ITypeBinding resolveBinding = ((TypeLiteral) mi.getExpression()).getType().resolveBinding();
					IBinding targetBinding = null;
					ASTNode object = (ASTNode) mi.arguments().get(0);
					while (object instanceof ParenthesizedExpression) {
						if (object instanceof ParenthesizedExpression) {
							ParenthesizedExpression pe = (ParenthesizedExpression) object;
							object = pe.getExpression();
						}
					}
					if (object instanceof Name && (targetBinding = ((Name) object).resolveBinding()) != null) {
						fInvocationHashMap.put(targetBinding, resolveBinding);
					} else {
						fMatchNodePosHashMap.put(new Position(object.getStartPosition(), object.getLength()),
								resolveBinding);
					} 
				}
				temp = mi.getExpression();
			} else if (node instanceof FieldAccess) {
				FieldAccess fa = (FieldAccess) node;
				temp = fa.getExpression();
			} else if (node instanceof QualifiedName) {
				QualifiedName qn = (QualifiedName) node;
				temp = qn.getQualifier();
			} else if (node instanceof ArrayAccess) {
				ArrayAccess aa = (ArrayAccess) node;
				temp = aa.getArray();
			}

			if (temp != null) {
				try {
					temp = getOriginalExpression(temp);
					IASTFragment[] allMatches = ASTFragmentFactory
							.createFragmentForFullSubtree(getEnclosingBodyNode(temp))
							.getSubFragmentsMatching(getIASTFragment(temp));
					for (IASTFragment match : allMatches) {
						fMatchNodePosSet.add(new Position(match.getStartPosition(), match.getLength()));

					}
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
				IBinding resolveBinding = null;
				if (temp instanceof Name)
					resolveBinding = ((Name) temp).resolveBinding();
				if (resolveBinding != null)
					fInvocationSet.add(resolveBinding);
			}
		}

	}

	private class MiddleCodeVisitor extends ASTVisitor {
		int startPosition;

		int endPosition;

		boolean nullFlag;

		boolean castFlag;

		public MiddleCodeVisitor(int startPosition, int endPosition) {
			this.startPosition = startPosition;
			this.endPosition = endPosition;
			this.nullFlag = false;
			this.castFlag = false;
		}

		public boolean hasNullCheck() {
			return this.nullFlag;
		}

		public boolean hasCastCheck() {
			return this.castFlag;
		}

		@Override
		public boolean preVisit2(ASTNode node) {
			int sl = node.getStartPosition();
			int el = node.getStartPosition() + node.getLength();
			if (el < startPosition || sl > endPosition || this.nullFlag == true || this.castFlag == true) {
				return false;
			}
			if (!(sl >= startPosition && el <= endPosition)) {
				return super.preVisit2(node);
			}
			if (node instanceof InstanceofExpression) {
				InstanceofExpression instanceofExpression = (InstanceofExpression) node;
				Expression leftOperand = getOriginalExpression(instanceofExpression.getLeftOperand());
				Type rightOperand = instanceofExpression.getRightOperand();
				ITypeBinding resolveBinding = rightOperand.resolveBinding();
				IBinding targetBinding = null;
				if (leftOperand instanceof Name && (targetBinding = ((Name) leftOperand).resolveBinding()) != null
						&& hasInheritanceRelationship(fInvocationHashMap.get(targetBinding), resolveBinding)) {
					this.castFlag = true;
					return false;
				} else if (hasInheritanceRelationship(
						fMatchNodePosHashMap.get(new Position(leftOperand.getStartPosition(), leftOperand.getLength())),
						resolveBinding)) {
					this.castFlag = true;
					return false;
				}
				return super.preVisit2(node);
			} else if (node instanceof MethodInvocation) {
				MethodInvocation mi = (MethodInvocation) node;
				IMethodBinding rmb = mi.resolveMethodBinding();
				String str = rmb == null ? "" : rmb.getMethodDeclaration().getKey();
				if (str.startsWith("Ljava/lang/Class;.isInstance(Ljava/lang/Object;)Z") && mi.getExpression() instanceof TypeLiteral) {
					ASTNode object = (ASTNode) mi.arguments().get(0);
					while (object instanceof ParenthesizedExpression) {
						if (object instanceof ParenthesizedExpression) {
							ParenthesizedExpression pe = (ParenthesizedExpression) object;
							object = pe.getExpression();
						}
					}
					ASTNode leftOperand = object;
					Type rightOperand = ((TypeLiteral) mi.getExpression()).getType();
					ITypeBinding resolveBinding = rightOperand.resolveBinding();
					IBinding targetBinding = null;
					if (leftOperand instanceof Name && (targetBinding = ((Name) leftOperand).resolveBinding()) != null
							&& hasInheritanceRelationship(fInvocationHashMap.get(targetBinding), resolveBinding)) {
						this.castFlag = true;
						return false;
					} else if (hasInheritanceRelationship(
							fMatchNodePosHashMap
									.get(new Position(leftOperand.getStartPosition(), leftOperand.getLength())),
							resolveBinding)) {
						this.castFlag = true;
						return false;
					}
					return super.preVisit2(node);
//					ITypeBinding resolveBinding =( (TypeLiteral)mi.getExpression()).getType().resolveBinding();
//					IBinding targetBinding = null;
//					if (object instanceof Name && (targetBinding = ((Name) object).resolveBinding()) != null) {
//						fInvocationHashMap.put(targetBinding, resolveBinding);
//					} else {
//						fMatchNodePosHashMap.put(new Position(object.getStartPosition(), object.getLength()),
//								resolveBinding);
//					}
				}
			}

			Expression target = null;
			if (node instanceof InfixExpression) {
				InfixExpression infixExpression = (InfixExpression) node;
				Operator op = infixExpression.getOperator();
				if (Operator.toOperator(op.toString()) == Operator.EQUALS
						|| Operator.toOperator(op.toString()) == Operator.NOT_EQUALS) {
					Expression leftExpression = infixExpression.getLeftOperand();
					Expression rightExpression = infixExpression.getRightOperand();
					if (rightExpression.getNodeType() == ASTNode.NULL_LITERAL) {
						target = leftExpression;
					} else if (leftExpression.getNodeType() == ASTNode.NULL_LITERAL) {
						target = rightExpression;
					}
				}
			}

			if (target != null) {
				target = getOriginalExpression(target);
				IBinding targetBinding = null;
				if (target instanceof Name && (targetBinding = ((Name) target).resolveBinding()) != null
						&& fInvocationSet.contains(targetBinding)) {
					this.nullFlag = true;
					return false;
				} else if (fMatchNodePosSet.contains(new Position(target.getStartPosition(), target.getLength()))) {
					this.nullFlag = true;
					return false;
				}
			}
			return super.preVisit2(node);
		}

		private boolean hasInheritanceRelationship(ITypeBinding itb1, ITypeBinding itb2) {
			if (itb2 == null || itb1 == null) {
				return false;
			} else if (itb1 == itb2) {
				return true;
			}
			ITypeBinding superclass = itb2.getSuperclass();
			ITypeBinding[] interfaces = itb2.getInterfaces();
			for (int i = 0; i < interfaces.length; ++i) {
				if (hasInheritanceRelationship(itb1, interfaces[i]))
					return true;
			}
			if (superclass != null && hasInheritanceRelationship(itb1, superclass)) {
				return true;
			}
			return false;

		}
	}

}