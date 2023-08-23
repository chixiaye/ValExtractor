package exp.refactorings;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.impl.CoreProgressManager;
import com.intellij.openapi.progress.impl.ProgressManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.introduceVariable.IntroduceVariableBase;
import com.intellij.refactoring.introduceVariable.IntroduceVariableHandler;
import com.intellij.util.DocumentUtil;
import exp.AfterWorkHandler;
import exp.Editors;
import exp.data.json.Exp2Record;
import exp.utils.Constants;

import java.io.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static exp.Editors.release;
import static exp.utils.Utils.convertEclipseOffsetToCoordinatePos;


public class ExtractVariable implements Runnable {
    private PsiExpression expr;
    private PsiFile psiFile;
    private Document document;
    private Exp2Record exp2Record;
    private Editor editor;
    private Project project;

    private boolean isWIN;
    public ExtractVariable(Project project, Exp2Record exp2Record) {
        this.project = project;
        this.exp2Record = exp2Record;
        String filePath = Constants.EXP2_PROJECT_ROOT + exp2Record.getProjectName() + "/" + exp2Record.getPath();
        PsiDocumentManager instance = PsiDocumentManager.getInstance(project);
        document = Editors.getCurrentDocument(filePath);
        System.out.println("doc size: " + document.getTextLength());
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
        // read until end of file

        editor = Editors.createSourceEditor(project, filePath, "java", false, exp2Record.getOffset());
        psiFile = instance.getPsiFile(document);
        expr = findPsiExpression(exp2Record.getOffset());
    }

    private PsiExpression findPsiExpression(int offset) {
        PsiElement element ;
        if(isWIN ){
            String filePath = Constants.EXP2_PROJECT_ROOT + exp2Record.getProjectName() + "/" + exp2Record.getPath();
            int[] pos = convertEclipseOffsetToCoordinatePos(filePath, offset, 4);
            offset = DocumentUtil.calculateOffset(document, pos[0]-1, pos[1]-1, 4);
            System.out.println("re-pos:"+ pos[0]+","+pos[1]+", "+ offset);
//            System.out.println("com-pos:"+document.getLineNumber(offset)+ ", "+ document.getLineStartOffset(pos[0]-1)+", "+ document.getLineEndOffset(pos[0]-1));
            element = psiFile.findElementAt(offset);
            while (element.getParent() != null) {
                if (element.getNode() != null &&
                        ( element.getNode().getText().length()==
                                this.exp2Record.getLength()   ||replaceBlank(element.getNode().getText()).equals(replaceBlank(this.exp2Record.getOldName()))) ) {
                    break;
                }
                element = element.getParent();
            }
        }else {
            element = psiFile.findElementAt(offset);
            while (element!=null && element.getParent() != null) {
                if (element.getNode() != null &&
                        element.getNode().getTextLength() == this.exp2Record.getLength()) {
                    break;
                }
                element = element.getParent();
            }
        }
//        System.out.println("element:"+element.getNode().getClass().getName());
        if (PsiExpression.class.isInstance(element)) {
            return (PsiExpression) element;
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
            System.out.println(e.getKey() + ", " + c.isAll());
            if (choice == IntroduceVariableBase.JavaReplaceChoice.NO && !e.getKey().contains("will change")) {
                choice = c;
                System.out.println("chosen! ");
            }
            if (e.getKey().contains("all occurrences but write")) {
                choice = c;
                System.out.println("chosen! ");
                break;
            } else if (e.getKey().contains("occurrences")&& !e.getKey().contains("will change")) {
                String regEx = "[^0-9]";
                Pattern p = Pattern.compile(regEx);
                Matcher m = p.matcher(e.getKey());
                int v = Integer.valueOf(m.replaceAll("").trim());
                if (v >= maxReplacedExpr) {
                    choice = c;
                    maxReplacedExpr = v;
                    System.out.println("chosen! ");
                }
            }
        }
//        handler.invokeImpl(project,expr,(PsiElement)null, IntroduceVariableBase.JavaReplaceChoice.ALL,editor);
//        LinkedHashMap<IntroduceVariableBase.JavaReplaceChoice, List<PsiExpression>> occurrencesMap =
//                occurrencesInfo.buildOccurrencesMap(expr);
//        System.out.println("map");
//        for (Map.Entry<IntroduceVariableBase.JavaReplaceChoice, List<PsiExpression>> e:occurrencesMap.entrySet()){
//            if (e.getKey().isAll()) {
//                choice = e.getKey();
//            }
//            System.out.println(e.getKey().toString()+", "+e.getValue().size());
//        }
        try{
            PsiDocumentManager.getInstance(project) .doPostponedOperationsAndUnblockDocument(document);
            handler.invokeImpl(project, expr, (PsiElement) null, choice, editor);
        }catch(IllegalStateException e){
            System.out.println("exception: "+e.getMessage());
        }
//        FileDocumentManager.getInstance().saveAllDocuments();
//        FileDocumentManager.getInstance().saveDocument(document);
//        PsiDocumentManager.getInstance(project).commitDocument(document);
        String filePath = Constants.EXP2_PROJECT_ROOT + exp2Record.getProjectName() + "/" + exp2Record.getPath();
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
            if(isWIN)
                out.write(document.getText().replaceAll("\\n", "\r\n"));//win
            else
                out.write(document.getText());
            out.close();
        } catch (IOException e) {
        }
//        PsiDocumentManager.getInstance(project).commitDocument(document);
//        FileDocumentManager.getInstance().saveAllDocuments();
        System.out.println("OKK ");

        System.out.println("before doc size: " + document.getTextLength());

        new AfterWorkHandler(project, this.exp2Record, "idea");
        FileDocumentManager.getInstance().reloadFromDisk(document);
        System.out.println("after doc size: " + document.getTextLength());
        release(editor);
    }


    @Override
    public void run() {

    }

    static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\\t|\\r|\\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

}
