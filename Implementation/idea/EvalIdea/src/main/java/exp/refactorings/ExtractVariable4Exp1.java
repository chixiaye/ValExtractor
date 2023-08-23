package exp.refactorings;

import com.intellij.lang.LanguageRefactoringSupport;
import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.refactoring.introduceVariable.IntroduceVariableBase;
import com.intellij.refactoring.introduceVariable.IntroduceVariableHandler;
import exp.AfterWorkHandler;
import exp.Editors;
import exp.data.json.Exp1Record;
import exp.utils.Constants;

import java.io.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static exp.Editors.release;


public class ExtractVariable4Exp1 implements Runnable {
    public PsiExpression expr;
    private PsiFile psiFile;
    private Document document;
    private Exp1Record exp1Record;
    private Editor editor;
    private Project project;

    private boolean isWIN;

    public boolean isRight;

    private int index;

    public ExtractVariable4Exp1(Project project, Exp1Record exp1Record, int index) {
        this.project = project;
        this.exp1Record = exp1Record;
        this.index = index;
        this.isRight = true;
        String filePath = Constants.EXP1_PROJECT_ROOT + exp1Record.getProjectName() + "/" + exp1Record.getPath();

        PsiDocumentManager instance = PsiDocumentManager.getInstance(project);
        document = Editors.getCurrentDocument(filePath);
        try {
            byte[] bytes = new byte[1024];
            StringBuffer sb = new StringBuffer();
            FileInputStream in = new FileInputStream(filePath);
            int len;
            while ((len = in.read(bytes)) != -1) {
                sb.append(new String(bytes, 0, len));
                if(sb.toString().endsWith("\r\n"))
                    break;
            }
            in.close();
            isWIN= sb.toString().contains("\r\n");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("doc size: " + document.getTextLength());
        int offset = document.getLineStartOffset(exp1Record.getElemList().get(index).getStartLine() - 1) - 1 + exp1Record.getElemList().get(index).getStartColumn();
        System.out.println(exp1Record.getOldName()+", "+offset + "," + exp1Record.getElemList().get(index).getStartLine()
                + ", " + exp1Record.getElemList().get(index).getStartColumn());
        editor = Editors.createSourceEditor(project, filePath, "java", false, offset);
        psiFile = instance.getPsiFile(document);
        expr = findPsiExpression(offset);
    }

    private PsiExpression findPsiExpression(int offset) {
        PsiElement element;
        element = psiFile.findElementAt(offset);

        while (element != null && element.getParent() != null) {
            if (element.getText().replace(" ","").equals(this.exp1Record.getOldName().replace(" ",""))) {
                break;
            }
            element = element.getParent();
        }
        if (PsiExpression.class.isInstance(element)) {
            return (PsiExpression) element;
        }

        element = psiFile.findElementAt(offset);
        while (element != null && element.getParent() != null) {
            if (PsiStatement.class.isInstance(element)
            ) {
                break;
            }
            element = element.getParent();
        }
        System.out.println(element.getText());
        return findPsiExpressionInChildren(element);
    }

    private PsiExpression findPsiExpressionInChildren(PsiElement element) {
        if (element == null)
            return null;
        if (PsiExpression.class.isInstance(element)  &&
                element.getText().replace(" ","").equals(this.exp1Record.getOldName().replace(" ","")))
            return (PsiExpression) element;
        for (PsiElement e : element.getChildren()) {
            PsiExpression expr = findPsiExpressionInChildren(e);
            if (expr != null)
                return expr;
        }
        return null;
    }

    public void extractVariable() {
        System.out.println("expr: " + expr);
        if (expr == null) {
            return;
        }
        IntroduceVariableHandler handler = new IntroduceVariableHandler();
//        editor.getCaretModel().moveToOffset(expr.getTextOffset());
//        editor.getSelectionModel().setSelection(expr.getTextOffset(), expr.getTextOffset() + expr.getTextLength());
//        editor.getSettings().setVariableInplaceRenameEnabled(true);

        Map<String, IntroduceVariableBase.JavaReplaceChoice> choices = handler.getPossibleReplaceChoices(project, expr);// handler.getPossibleReplaceChoices(project, expr);
        IntroduceVariableBase.JavaReplaceChoice choice = IntroduceVariableBase.JavaReplaceChoice.NO;
        int maxReplacedExpr = 0;
        for (Map.Entry<String, IntroduceVariableBase.JavaReplaceChoice> e : choices.entrySet()) {
            IntroduceVariableBase.JavaReplaceChoice c = e.getValue();
//            System.out.println(e.getKey() + ", " + c.isAll());
            if (choice == IntroduceVariableBase.JavaReplaceChoice.NO && !e.getKey().contains("will change")) {
                choice = c;
//                System.out.println("chosen! ");
            }
            if (e.getKey().contains("all occurrences but write")) {
                choice = c;
//                System.out.println("chosen! ");
                break;
            } else if (e.getKey().contains("occurrences") && !e.getKey().contains("will change")) {
                String regEx = "[^0-9]";
                Pattern p = Pattern.compile(regEx);
                Matcher m = p.matcher(e.getKey());
                int v = Integer.valueOf(m.replaceAll("").trim());
                if (v >= maxReplacedExpr) {
                    choice = c;
                    maxReplacedExpr = v;
//                    System.out.println("chosen! ");
                }
            }
        }
        try {
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document);
            handler.invokeImpl(project, expr, (PsiElement) null, choice, editor);
        } catch (IllegalStateException e) {
            System.out.println("exception: " + e.getMessage());
        }
        String filePath = Constants.EXP1_PROJECT_ROOT + this.exp1Record.getProjectName() + "/" + this.exp1Record.getPath();
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
            if (isWIN)
                out.write(document.getText().replaceAll("\\n", "\r\n"));//win
            else
                out.write(document.getText());
            out.close();
        } catch (IOException e) {
        }

        System.out.println("before doc size: " + document.getTextLength());
        EvalExp1Result eval = new EvalExp1Result(this.exp1Record, this.psiFile, this.document, handler.getInplaceIntroducer().getInitialName());
        this.isRight = eval.isRight;
        System.out.println("OKK "+ isRight);
        new AfterWorkHandler(project, this.exp1Record, "idea");
        FileDocumentManager.getInstance().reloadFromDisk(document);
        System.out.println("after doc size: " + document.getTextLength());
        release(editor);

//        System.out.println(document.getText());
    }


    @Override
    public void run() {

    }



}
