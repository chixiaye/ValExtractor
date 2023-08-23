package valextractor.handlers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import valextractor.log.MyLog;
import valextractor.utils.Constants;
import valextractor.utils.Utils;

public class EvaluatiaJRRTHandler {
	String projectName;
	ArrayList<String> fileList;

	public EvaluatiaJRRTHandler(String projectName) {
		this.projectName = projectName;
	}

	public void runDiff() throws IOException {

		String before = "" + this.projectName + "/ours";
		String after = Constants.JRRT_PATH + this.projectName+ "/ours"; 
		ArrayList<String> JRRTFiles = Utils.getJavaFiles(before);
		ArrayList<String> oursFiles = Utils.getJavaFiles(after);
		ArrayList<EvalRecord> erList = new ArrayList<>();
		assert (oursFiles.size() == JRRTFiles.size());
		for (int i = 1; i <= oursFiles.size(); ++i) {
			for (int j = 0; j < oursFiles.size(); ++j) {
				String s1 = oursFiles.get(j);
				if (getNum(s1, after) == i) {
					for (int k = 0; k < JRRTFiles.size(); ++k) {
						String s2 = JRRTFiles.get(k);
						if (getNum(s2, before) == i) {
							int cntOurs = Utils.appearNumber(Utils.getCodeFromFile(new File(s1)), "var_" + i);
							int JRRTOurs = Utils.appearNumber(Utils.getCodeFromFile(new File(s2)), "var_" + i);
							erList.add(new EvalRecord(i, JRRTOurs, cntOurs));
							break;
						}
					}
				}
			}
		} 
		for (int i = 1; i <= erList.size(); ++i) {
			EvalRecord evalRecord = erList.get(i - 1);
			if(evalRecord.jrrtCnt!=evalRecord.oursCnt) {
				System.out.println(i);
			}
//			if (evalRecord.jrrtCnt == 2 && evalRecord.oursCnt == 2
//					|| evalRecord.jrrtCnt == 1 && evalRecord.oursCnt == 1)
//				v1++;
////			else if ((evalRecord.jrrtCnt == 2 || evalRecord.jrrtCnt == 1) && evalRecord.oursCnt == 0) {
////				v2++;
//////				System.out.println(evalRecord.id);
////			} else if (evalRecord.jrrtCnt == 0 && (evalRecord.oursCnt == 2 || evalRecord.oursCnt == 1)) {
////				v3++;
////			} else if (evalRecord.jrrtCnt == 0 && evalRecord.oursCnt == 0) {
////				v4++;
////			} else
////				System.out.println("no:" + i + " error!!!!!!! " + evalRecord.jrrtCnt + ", " + evalRecord.oursCnt);
		}
//		System.out.println(this.projectName + " total: " + erList.size());
//		System.out.println("JRRT and Ours do: " + v1);
//		System.out.println("JRRT does but Ours not does: " + v2);
//		System.out.println("Ours does but JRRT not does: " + v3);
//		System.out.println("JRRT and Ours not do: " + v4);
//		Utils.getCodeFromFile
	}
	
	public void run() throws IOException {

		String JRRTPath = Constants.JRRT_PATH + this.projectName+"/JRRT";
		String oursPath = Constants.JRRT_PATH + this.projectName+ "/ours";
		ArrayList<String> JRRTFiles = Utils.getJavaFiles(JRRTPath);
		ArrayList<String> oursFiles = Utils.getJavaFiles(oursPath);
		ArrayList<EvalRecord> erList = new ArrayList<>();
		assert (oursFiles.size() == JRRTFiles.size());
		for (int i = 1; i <= oursFiles.size(); ++i) {
			for (int j = 0; j < oursFiles.size(); ++j) {
				String s1 = oursFiles.get(j);
				if (getNum(s1, oursPath) == i) {
					for (int k = 0; k < JRRTFiles.size(); ++k) {
						String s2 = JRRTFiles.get(k);
						if (getNum(s2, JRRTPath) == i) {
							int cntOurs = Utils.appearNumber(Utils.getCodeFromFile(new File(s1)), "var_" + i);
							int JRRTOurs = Utils.appearNumber(Utils.getCodeFromFile(new File(s2)), "var_" + i);
							erList.add(new EvalRecord(i, JRRTOurs, cntOurs));
							break;
						}
					}
				}
			}
		}
		int v1 = 0;
		int v2 = 0;
		int v3 = 0;
		int v4 = 0;
		for (int i = 1; i <= erList.size(); ++i) {
			EvalRecord evalRecord = erList.get(i - 1);
			if (evalRecord.jrrtCnt == 2 && evalRecord.oursCnt == 2
					|| evalRecord.jrrtCnt == 1 && evalRecord.oursCnt == 1)
				v1++;
			else if ((evalRecord.jrrtCnt == 2 || evalRecord.jrrtCnt == 1) && evalRecord.oursCnt == 0) {
				v2++;
//				System.out.println(evalRecord.id);
			} else if (evalRecord.jrrtCnt == 0 && (evalRecord.oursCnt == 2 || evalRecord.oursCnt == 1)) {
				v3++;
			} else if (evalRecord.jrrtCnt == 0 && evalRecord.oursCnt == 0) {
				v4++;
			} else
				MyLog.add("no:" + i + " error!!!!!!! " + evalRecord.jrrtCnt + ", " + evalRecord.oursCnt);
		}
		MyLog.add(this.projectName + " total: " + erList.size());
		MyLog.add("JRRT and Ours do: " + v1);
		MyLog.add("JRRT does but Ours not does: " + v2);
		MyLog.add("Ours does but JRRT not does: " + v3);
		MyLog.add("JRRT and Ours not do: " + v4);
		MyLog.show();
		MyLog.clear();
//		Utils.getCodeFromFile
	}

	int getNum(String s, String prefix) {
		int indexS = prefix.length() + 1;
		int indexE = s.indexOf('_');
//		System.out.println(s.substring(indexS, indexE));
		return Integer.valueOf(s.substring(indexS, indexE));
	}

	class EvalRecord {
		int id;
		int jrrtCnt;
		int oursCnt;

		public EvalRecord(int id, int jrrtCnt, int oursCnt) {
			super();
			this.id = id;
			this.jrrtCnt = jrrtCnt;
			this.oursCnt = oursCnt;
		}

	}

}
