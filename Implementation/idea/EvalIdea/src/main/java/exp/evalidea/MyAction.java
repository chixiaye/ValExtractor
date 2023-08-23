package exp.evalidea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import exp.AfterWorkHandler;
import exp.data.json.Exp1Record;
import exp.data.json.Exp2Record;
import exp.data.json.HandleDataSetJsonFile;
import exp.log.MyLog;
import exp.refactorings.ExtractVariable;
import exp.refactorings.ExtractVariable4Exp1;
import exp.utils.Constants;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static exp.AfterWorkHandler.exp1Checkout;
import static exp.utils.Constants.DATASET_ROOT;
import static exp.utils.Constants.Project_NAME;

public class MyAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Messages.showMessageDialog(project, "finished! ",
                "Introduce Variable", Messages.getInformationIcon());
    }

    @Override
    public void beforeActionPerformedUpdate(@NotNull AnActionEvent e) {
        super.beforeActionPerformedUpdate(e);
        MyLog.clear();
//         exp1(e);
       exp2(e);

    }

    void exp1(AnActionEvent e) {
        String prefix = "";
        String dataPath = prefix + "sample/httpcomponents-core.json";
        String projectName ="Httpcomponents_core_h2";
        try {
            ArrayList<Exp1Record> allRecordList = HandleDataSetJsonFile.JsonParserExp1(dataPath);
            for (int i =1 ; i <= allRecordList.size(); ++i) {
                FileDocumentManager.getInstance().reloadFiles();
                Project project = e.getProject();
                Exp1Record record = allRecordList.get(i - 1);
                String tempPName=record.getProjectName();
                record.setProjectName(projectName);
//                record.setNo(i);
                String tempName = record.getOldName().replace('/', '／').replace('\\', '＼');
                String name = Constants.EXP1_RESULT_PATH + record.getProjectName() + "/" + "idea" + "/"
                        + record.getNo() + "_" + tempName + ".patch";
                if (new File(name).exists())
                    continue;
                System.out.println("no: " + i + "," + record.getPath() + ", " +
                        record.getOldName()  );

                boolean isRight=false;
                ExtractVariable4Exp1 ev = null;
                boolean valid=false;
                for (int j = 0; j < record.getElemList().size(); ++j) {
                    exp1Checkout(record);
                    FileDocumentManager.getInstance().reloadFiles();
                    ev = new ExtractVariable4Exp1(project, record,j);
                    ev.extractVariable();
                    if(ev.expr!=null){
                        valid=true;
                    }
                    if(ev.expr!=null && ev.isRight){
                        isRight=true;
                        exp1Checkout(record);
                        break;
                    }
                    ev = null;
                    System.gc();
                }
                if(isRight && valid )
                    MyLog.add(i+","+tempPName+","+record.getCommitId()+","+record.getNewName()+","
                            + 1+",IDEA");
                else if(valid)
                    MyLog.add(i+","+tempPName+","+record.getCommitId()+","+record.getNewName()+","
                            + 0+",IDEA");
//                if(i%33==0)
//                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        MyLog.show();
    }


    void exp2(AnActionEvent e) {
        String prefix = DATASET_ROOT +"/exp2/";
        String dataPath = prefix + Project_NAME  + ".json";
        try {
            List<Exp2Record> allRecordList = HandleDataSetJsonFile.JsonParserExp2(dataPath);
            for (int i = 1; i <= allRecordList.size(); ++i) {
                FileDocumentManager.getInstance().reloadFiles();
                Project project = e.getProject();
                Exp2Record exp2Record = allRecordList.get(i - 1);
                String tempName = exp2Record.getOldName().replace('/', '／').replace('\\', '＼');
                String name = Constants.EXP2_RESULT_PATH + exp2Record.getProjectName() + "/" + "idea" + "/"
                        + exp2Record.getNo() + "_" + tempName + ".patch";
                if (new File(name).exists())
                    continue;
                System.out.println("no: " + i + "," + exp2Record.getPath() + ", " +
                        exp2Record.getOldName() + ", " + exp2Record.getOffset());
                ExtractVariable ev = new ExtractVariable(project, exp2Record);
                ev.extractVariable();
                ev = null;
                System.gc();
//                FileDocumentManager.getInstance().reloadFiles(project.getBaseDir());

//               Thread.sleep(3000);
//                if(i==1384)
//                    break;
            }

//            new GitHandler(Constants.EXP2_PROJECT_ROOT+allRecordList.get(0).getProjectName()).restoreCode();
//            ev.extractVariable();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}


