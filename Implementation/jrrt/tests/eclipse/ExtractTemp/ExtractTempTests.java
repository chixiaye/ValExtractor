/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Max Schaefer    - rewrite of driver code to work with JRRT
 *******************************************************************************/
package tests.eclipse.ExtractTemp;

import junit.framework.TestCase;
import AST.ASTNode;
import AST.AbstractDot;
import AST.Access;
import AST.AddExpr;
import AST.CompilationUnit;
import AST.Expr;
import AST.MulExpr;
import exp.handler.SampleHandler;

public class ExtractTempTests extends TestCase {

	private static final String TEST_PATH_PREFIX = "tests/eclipse/ExtractTemp/";

	public ExtractTempTests(String name) {
		super(name);
	}

	private boolean old_printCUNames;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		old_printCUNames = CompilationUnit.printCUNames;
		CompilationUnit.printCUNames = false;
	}
	@Override
	protected void tearDown() throws Exception {
		CompilationUnit.printCUNames = old_printCUNames;
		super.tearDown();
	}

	private String getSimpleTestFileName(boolean canExtract, boolean input){
		String fileName = "A_" + getName();
		if (canExtract)
			fileName += input ? "_in": "_out";
		return fileName + ".java";
	}

	private String getTestFileName(boolean canExtract, boolean input){
		String fileName= TEST_PATH_PREFIX;
		fileName += canExtract ? "canExtract/": "cannotExtract/";
		return fileName + getSimpleTestFileName(canExtract, input);
	}
	
	public static Expr findExpr(ASTNode p, int startLine, int startColumn, int endLine, int endColumn, String str) {
		if(p == null)
			return null;
		if(p instanceof Expr) {
			int start = p.getStart(), end = p.getEnd();
			if(startLine == ASTNode.getLine(start) &&
					startColumn == ASTNode.getColumn(start) &&
				(	(endLine == ASTNode.getLine(end) &&
					endColumn == ASTNode.getColumn(end)+1) ||
					p.value.toString().equals(str) )
				)
				return (Expr)p;
		}
		for(int i=0;i<p.getNumChild();++i) {
			Expr res = findExpr(p.getChild(i), startLine, startColumn, endLine, endColumn,str);
			if(res != null)
				return res;
		}
		if(p instanceof AbstractDot) {
			AbstractDot pdot = (AbstractDot)p;
			if(pdot.getRight() instanceof AbstractDot) {
				Expr l = pdot.getLeft();
				AbstractDot r = (AbstractDot)pdot.getRight();
				Access rl = (Access)r.getLeft(), rr = r.getRight();
				AbstractDot l2 = new AbstractDot(l, rl);
				l2.setStart(l.getStart());
				l2.setEnd(rl.getEnd());
				pdot.setLeft(l2);
				pdot.setRight(rr);
				Expr res = findExpr(p, startLine, startColumn, endLine, endColumn,str);
				if(res != null)
					return res;
				pdot.setLeft(l);
				r.setLeft(rl);
				r.setRight(rr);
				pdot.setRight(r);
			}
		} else if(p instanceof MulExpr) {
			MulExpr m = (MulExpr)p;
			if(m.getLeftOperand() instanceof MulExpr) {
				MulExpr lm = (MulExpr)m.getLeftOperand();
				// so we have (x * y) * z, where lm = x * y and m = lm * z
				// we want to re-organise this into x * (y * z)
				m.setLeftOperand(lm.getLeftOperand());
				lm.setLeftOperand(lm.getRightOperand());
				lm.setRightOperand(m.getRightOperand());
				m.setRightOperand(lm);
				lm.setStart(lm.getLeftOperand().getStart());
				lm.setEnd(lm.getRightOperand().getEnd());
				m.setStart(m.getLeftOperand().getStart());
				return findExpr(p, startLine, startColumn, endLine, endColumn,str);
			}
		} else if(p instanceof AddExpr) {
			AddExpr m = (AddExpr)p;
			if(m.getLeftOperand() instanceof AddExpr) {
				AddExpr lm = (AddExpr)m.getLeftOperand();
				m.setLeftOperand(lm.getLeftOperand());
				lm.setLeftOperand(lm.getRightOperand());
				lm.setRightOperand(m.getRightOperand());
				m.setRightOperand(lm);
				lm.setStart(lm.getLeftOperand().getStart());
				lm.setEnd(lm.getRightOperand().getEnd());
				m.setStart(m.getLeftOperand().getStart());
				return findExpr(p, startLine, startColumn, endLine, endColumn,str);
			}
		}
		return null;
	}


    public void testMockito() {
    	SampleHandler sh=new SampleHandler("Mockito","src/","lib/");
    	try {
			sh.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		assert(new String() !=null);
    }
	
    public void testLang() {
    	SampleHandler sh=new SampleHandler("Lang","src/main/java/","lib/");
    	try {
			sh.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		assert(new String() !=null);
    }
	
    public void testMath() {
    	SampleHandler sh=new SampleHandler("Math","src/main/java/","lib/");
    	try {
			sh.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		assert(new String() !=null);
    }
    
    public void testClosure() {
    	SampleHandler sh=new SampleHandler("Closure","src/","lib/");
    	try {
			sh.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		assert(new String() !=null);
    }
    public void testTime() {
    	SampleHandler sh=new SampleHandler("Time","src/main/java/","lib/");
    	try {
			sh.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		assert(new String() !=null);
    }
    
    public void testChart() {
    	SampleHandler sh=new SampleHandler("Chart","source/","lib/");
    	try {
			sh.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		assert(new String() !=null);
    }
    
    
}
