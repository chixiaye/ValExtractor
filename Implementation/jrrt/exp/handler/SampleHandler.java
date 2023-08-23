package exp.handler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

import AST.ASTNode;
import AST.CompilationUnit;
import AST.Expr;
import AST.Program;
import AST.RefactoringException;
import beaver.Symbol;
import exp.data.json.Exp2Record;
import exp.data.json.HandleDataSetJsonFile;
import exp.data.json.TimeRecord;
import exp.utils.Constants;
import exp.utils.Utils;
import parser.JavaParser;
import tests.CompileHelper;
import tests.eclipse.ExtractTemp.ExtractTempTests;

public class SampleHandler {
	String projectName;
	String sourcePath;
	String libPath;

	public SampleHandler(String projectName, String sourcePath, String libPath) {
		this.projectName = projectName;
		this.sourcePath = sourcePath;
		this.libPath = libPath;
	}

	public void run() throws Exception {
		String dataPath = Constants.DATASET_ROOT + "/exp2/" + projectName + ".json";
		int no = -1;
		exp2(projectName, dataPath, no, "JRRT");
	}

	/**
	 * @param dataPath
	 * @param no
	 * @throws Exception
	 * @throws JavaModelException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void exp2(String pName, String dataPath, int no, String approach)
			throws Exception, IOException, InterruptedException {
		List<Exp2Record> allRecordList = HandleDataSetJsonFile.JsonParserExp2(dataPath);
		System.out.println("total " + allRecordList.size());

		for (int i = no > 0 ? no : 1; i <= allRecordList.size(); ++i) {

			Exp2Record exp2Record = allRecordList.get(i - 1);
//			MyLog.add("no:" + i);
			System.out.println("refactoring " + i + " " + exp2Record.getPath() + "," + exp2Record.getOldName() + ","
					+ exp2Record.getOffset());
			refactoredCode = new String();
			String oldName = exp2Record.getOldName().replace('/', '／').replace('\\', '＼');
			System.out.println(i + "_" + oldName + ".java");
//			if(new File(Constants.JRRT_PATH + this.projectName + "/" + approach + "/" + i + "_" + oldName + ".java").exists())
//				continue;
			doJRRTRefactoring(Constants.EXP2_PROJECT_ROOT + this.projectName,
					Constants.EXP2_PROJECT_ROOT + this.projectName + "/" + this.sourcePath,
					Constants.EXP2_PROJECT_ROOT + this.projectName + "/" + this.libPath, exp2Record);
			Utils.createFileAndWrite(
					Constants.JRRT_PATH + this.projectName + "/" + approach + "/" + i + "_" + oldName + ".java",
					refactoredCode);
//			break;
		}
//		if (timeList.size() > 1)
//			createTimeFile(timeList, Constants.EXP2_RUNTIME_PATH + pName + ".csv");

	}

	CompilationUnit visitCU(ASTNode in, String p) {
		if (in instanceof CompilationUnit) {
			CompilationUnit cu = (CompilationUnit) in;
			if (p.equals(cu.pathName())) {
				return cu;
			}
		}
		for (int i = 0; i < in.getNumChildNoTransform(); ++i) {

			CompilationUnit cu = visitCU(in.getChildNoTransform(i), p);
			if (cu != null)
				return cu;
		}
		return null;
	}

	public static String refactoredCode = new String();

	void doJRRTRefactoring(String projectPath, String sourcePath, String libPath, Exp2Record exp2Record) {

		ArrayList<String> list = Utils.getFiles(sourcePath);
		list.addAll(Utils.getFiles(libPath));
		if (this.projectName.equals("Closure"))
			list.add(Constants.EXP2_PROJECT_ROOT + this.projectName + "/build/lib/rhino.jar");
		String[] array = list.toArray(new String[list.size()]);

		Program in = CompileHelper.compile(array);
		String fullPath = Constants.EXP2_PROJECT_ROOT + this.projectName + "/" + exp2Record.getPath();
		CompilationUnit compilationUnit = visitCU(in, fullPath);
		int offset = exp2Record.getOffset();
		int length = exp2Record.getLength();

		int[] cooStart = Utils.convertEclipseOffsetToCoordinatePos(fullPath, offset, 4);
		int[] cooEnd = Utils.convertEclipseOffsetToCoordinatePos(fullPath, offset + length, 4);

		System.out.println(cooStart[0] + " " + cooStart[1] + ", " + cooEnd[0] + " " + cooEnd[1] + ", " + length);

		Expr e = ExtractTempTests.findExpr(compilationUnit, cooStart[0], cooStart[1], cooEnd[0], cooEnd[1],
				exp2Record.getOldName());
		if (e == null) {
			return;
		}
		System.out.println(e.value.toString() + ", " + ASTNode.getLine(e.start()) + "," + ASTNode.getColumn(e.start()));
		try {
			e.doExtract(exp2Record.getNewName());
		} catch (RefactoringException rfe) {
			rfe.printStackTrace();
		}
	}

}
