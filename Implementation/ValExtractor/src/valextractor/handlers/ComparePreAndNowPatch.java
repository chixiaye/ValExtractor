package valextractor.handlers;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import valextractor.utils.Utils;

public class ComparePreAndNowPatch {
	String[] projectName;
	String beforePath;
	String afterPath;

	public ComparePreAndNowPatch(String before, String after, String... projectName) {
		this.beforePath = before;
		this.afterPath = after;
		this.projectName = projectName;
	}

	public void run(String approach) {
		int cnt = 0;
		int sum = 0;
		for (String name : projectName) {
			ArrayList<File> beforeFiles = new ArrayList<>();
			ArrayList<File> afterFiles = new ArrayList<>();
			Utils.getFileList(beforeFiles, beforePath + name + "/" + approach + "/");
			Utils.getFileList(afterFiles, afterPath + name + "/" + approach + "/");

//			System.out.println(beforeFiles.size()+", "+afterFiles.size());
			assertTrue(beforeFiles.size() == afterFiles.size());
			Comparator<File> cmp = new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					int v1 = Utils.getNum(o1.getName());
					int v2 = Utils.getNum(o2.getName());
					if (v1 == v2) {
						return 0;
					} else {
						return v1 > v2 ? 1 : -1;
					}
				}

			};
			Collections.sort(beforeFiles, cmp);
			Collections.sort(afterFiles, cmp);
			sum+=beforeFiles.size();
			for (int i = 0; i < beforeFiles.size(); ++i) {
				File before = beforeFiles.get(i);
				File after = afterFiles.get(i);
				String content = getContent(after);
				boolean changed = getContent(before).equals(content);
				if (!changed) {
					cnt++;
					if(content!=null&& !content.equals("") && name.equals("Time"))
					System.out.println(beforeFiles.get(i).getName() + " changed!");	
				}
			}

		}
		System.out.println("sum: "+sum+", different: "+cnt);
	}

	public static String getContent(File file) {
		FileInputStream fis;
		StringBuffer sb = new StringBuffer();
		try {
			fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);

			String line;
			while ((line = br.readLine()) != null) {
				// process the line
				sb.append(line);
			}
			br.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}

}
