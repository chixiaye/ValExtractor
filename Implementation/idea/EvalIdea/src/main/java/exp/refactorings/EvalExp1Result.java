package exp.refactorings;

import com.intellij.debugger.impl.DebuggerUtilsEx;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiElementFinderImpl;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import exp.Editors;
import exp.data.dataset.ElemInfo;
import exp.data.json.Exp1Record;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;

public class EvalExp1Result {
    private Document document;
    Exp1Record exp1Record;
    private PsiFile psiFile;

    public boolean isRight;
    String newName;
    ArrayList<PsiElement> list;
    HashSet<Integer> set;
    public EvalExp1Result(Exp1Record exp1Record, PsiFile psiFile,Document document,String newName) {
        this.exp1Record = exp1Record;
        this.psiFile = psiFile;
        this.document = document;
        this.newName=newName;
        this.isRight=true;
        //1. 每个语句内查找有没有新的名字
        //2. 有没有落在语句之外的新的名字
        list= new ArrayList<>();
        set=new HashSet<>();
        int minOffset= Integer.MAX_VALUE;
        for(ElemInfo e: exp1Record.getElemList()){
            int offset = document.getLineStartOffset(e.getStartLine()) + e.getStartColumn() -1;
            minOffset=Math.min(minOffset,offset);
            list.add(getStmt(offset));
        }
        PsiVisitor visitor = new PsiVisitor();
        PsiElement body = getBody(minOffset  );
//        System.out.println(body.getText());
        body.accept(visitor);
        System.out.println(this.set.size() +",,, "+this.list.size()+",,," +this.newName);
        this.isRight= this.isRight && this.set.size()==this.list.size();
    }


    private PsiElement getStmt(int offset) {
        PsiElement element= psiFile.findElementAt(offset);
        while (element != null && element.getParent() != null) {
            if ( PsiStatement.class.isInstance(element)
            ) {
                break;
            }
            element = element.getParent();
        }
        return element;
    }

    private PsiElement getBody(int offset) {
        PsiElement element= psiFile.findElementAt(offset);
        while (element != null && element.getParent()!=null) {
            if ( element.getText().replace(" ","").contains(newName+"="+exp1Record.getOldName().replace(" ","")+";")) {
                break;
            }
            element =  element.getParent();
        }
        return element;
    }

    class PsiVisitor extends PsiRecursiveElementVisitor{

        @Override
        public void visitElement(@NotNull PsiElement element) {
            super.visitElement(element);
//            System.out.println(element.getText()+", "+element.getClass().toString());
            if(isRight && element instanceof  PsiReferenceExpressionImpl
            && element.getText().equals(newName)){
                boolean flag=false;
                PsiReferenceExpressionImpl var = (PsiReferenceExpressionImpl) element;
                for(PsiElement e:list){
                    if(e instanceof  PsiStatement){
                        PsiStatement s= (PsiStatement)e;
                        if( var.getStartOffset()>=s.getTextOffset()
                                &&  var.getTextLength()+
                                var.getStartOffset()
                                <= s.getTextOffset() +s.getTextLength()){
                            set.add(s.getTextOffset());
                            flag=true;
                            break;
                        }
                    }
                }

                if(flag==false){
                    System.out.println(element.getParent().getText());
                    isRight=false;
                }
            }

        }
    }
}
