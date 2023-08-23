package valextractor.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import valextractor.utils.Constants;
import valextractor.utils.Utils;

public class RandomSampleHandler {
	static Object[][] dataArray = { { "Chart", 4501 }, { "Closure", 2700 }, { "Lang", 624 }, { "Math", 4358 },
			{ "Mockito", 138 }, { "Time", 714 } };

	public void run(int totalSize, int sampleSize, boolean flag) {
		Random r = new Random();
		HashSet<String> set = new HashSet<>();
		while (sampleSize > 0) {
			int value = r.nextInt(totalSize + 1);
			for (int i = 0; i < dataArray.length; ++i) {
				if (value > (Integer) dataArray[i][1]) {
					value -= (Integer) dataArray[i][1];
				} else {
					String string = dataArray[i][0] + "," + value;
					if (set.contains(string))
						break;

//							.getJavaFiles(Constants.JRRT_PATH + dataArray[i][0] + "/JRRT/");
					ArrayList<String> list = Utils
							.getJavaFiles(Constants.EXP2_RESULT_PATH + dataArray[i][0] + "/compare/");
					File f1 = getFileFromNum(value, list);
					if (f1 == null || !f1.exists()) {
						set.add(string);
						break;
					}
					if (f1.length() != 0) {
						sampleSize--;
						if (i == 0)
							System.out.println(dataArray[i][0] + "," + value);
						set.add(string);
					}
//					File f1 = getFileFromNum(value, oursFileList);
//					File f2 = getFileFromNum(value, JRRTFileList);
//					int cntOurs = Utils.appearNumber(Utils.getCodeFromFile(f1), "var_" + value);
//					int JRRTOurs = Utils.appearNumber(Utils.getCodeFromFile(f2), "var_" + value);
//					if( f1.getPath().contains("Chart")) {
//						System.out.println(value+","+cntOurs+", "+JRRTOurs+" var_" + value );
//					}

//					if ( cntOurs ==  JRRTOurs && JRRTOurs+ cntOurs > 0) {
//						sampleSize--;
//						System.out.println(dataArray[i][0] + "," + value);
//						set.add(string);
//					}
					break;
				}
			}
		}
//		set.forEach(any -> System.out.println(any));
		// [1-totalSize+1)
	}

	File getFileFromNum(int num, ArrayList<String> fileList) {
		for (int i = 0; i < fileList.size(); ++i) {
			String s = fileList.get(i);
			File file = new File(s);
			String name = file.getName();
			int indexE = name.indexOf('_');
			if (Integer.valueOf(name.substring(0, indexE)) == num) {
				return new File(s);
			}
		}
		return null;
	}

	public void run(int totalSize, int maxTime) {
		int diffCnt = 0;
		int sameCnt = 0;
		for (int i = 0; i < dataArray.length; ++i) {
			ArrayList<String> list = Utils.getJavaFiles(Constants.EXP2_RESULT_PATH + dataArray[i][0] + "/compare/");
			ArrayList<String[]> csv = Utils.CSVReader(Constants.EXP2_RUNTIME_PATH + dataArray[i][0] + ".csv");
			 
			for (int j = 1; j <= (Integer) dataArray[i][1]; ++j) {
				File f1 = getFileFromNum(j, list); 
				if( f1==null || !f1.exists())
					continue; 
				int time = Integer.valueOf(csv.get(j)[2]);
				if (time > maxTime) {
					if (f1.length() > 0)
						diffCnt++;
					else
						sameCnt++;
				}
			}
		}
		System.out.println("diff: "+diffCnt+", same:"+sameCnt);
	}

}
