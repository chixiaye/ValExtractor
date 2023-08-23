package exp.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import exp.data.json.TimeRecord;

public class Utils {

	public static void createFileAndWrite(String path, String content) {
		byte[] sourceByte = content.getBytes();
		if (null != sourceByte) {
			try {
				File file = new File(path); // 文件路径（路径+文件名）
				if (!file.exists()) { // 文件不存在则创建文件，先创建目录
					File dir = new File(file.getParent());
					dir.mkdirs();
					file.createNewFile();
				}
				FileOutputStream outStream = new FileOutputStream(file); // 文件输出流用于将数据写入文件
				outStream.write(sourceByte);
				outStream.close(); // 关闭文件输出流
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static int[] convertEclipseOffsetToCoordinatePos(String path, int offset, int tabSize) {
		int[] res = new int[2]; 
		res[0]=0;
		res[1]=0;
		try {
			FileReader fr=new FileReader(path);
			int tempChar=-1;
			int cnt=0;
			while((tempChar=fr.read())!=-1){//循环读取，每次循环读取一个字，每个汉字都有对应的char数字对应，因此需要将汉字对应的数字强转成char。
				cnt++;
				res[1]++;
				if(tempChar=='\t'){
					res[1]+=tabSize-1;
				} 
				if(tempChar=='\n'){
					res[0]++;
					res[1]=0;
				}
				if(cnt==offset){
					break;
				}
			}
			res[0]++;
			res[1]++; 
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return res;
	}
	
	
//	public static int[] convertEclipseOffsetToCoordinatePos(String path, int offset, int size) {
//		int[] res = new int[2];
//		int sum = 0;
//		int lineNum = 0;
//		try {
//			Scanner scanner = new Scanner(new File(path));
//			while (scanner.hasNextLine()) {
//				String nextLine = scanner.nextLine();
//				if (sum + nextLine.length() < offset) {
//					sum += nextLine.length();
//					lineNum++;
//				} else {
//					res[0] = lineNum + 1;
//					res[1] = offset - sum + 1;
//					 
//					break;
//				}
//				sum += size;
//			}
//			scanner.close();
//		} catch (FileNotFoundException ex) {
//			ex.printStackTrace();
//		}
//		return res;
//	}

	public static ArrayList<String> getFiles(String path) {
		ArrayList<String> files = new ArrayList<String>();
		ArrayList<File> tempList = new ArrayList<File>();
		getFileList(tempList, path);
		for (int i = 0; i < tempList.size(); i++) {
			files.add(tempList.get(i).toString());
		}
		return files;
	}

//	public static String readFile(File file) {
//		String content = "";
//		StringBuilder builder = new StringBuilder();
//		String absoluteFile = file.getAbsolutePath();
//		InputStreamReader streamReader;
//		try {
//			streamReader = new InputStreamReader(new FileInputStream(absoluteFile));
//			BufferedReader bufferedReader = new BufferedReader(streamReader);
//			while ((content = bufferedReader.readLine()) != null)
//				builder.append(content + "\n");
//			return builder.toString();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return "";
//	}

	public static void getFileList(ArrayList<File> arrayList, String strPath) {
		File fileDir = new File(strPath);
		if (null != fileDir && fileDir.isDirectory()) {
			File[] files = fileDir.listFiles();
			if (null != files) {
				for (int i = 0; i < files.length; i++) {
					// 如果是文件夹 继续读取
					if (files[i].isDirectory()) {
						getFileList(arrayList, files[i].getPath());
					} else {
						String strFileName = files[i].getPath();
						if (files[i].exists() && (strFileName.endsWith(".java") || strFileName.endsWith(".jar")
								|| strFileName.endsWith(".class"))) {
							arrayList.add(files[i]);
						}
					}
				}
			} else {
				if (null != fileDir) {
					String strFileName = fileDir.getPath();
					if (fileDir.exists() && (strFileName.endsWith(".java") || strFileName.endsWith(".jar")
							|| strFileName.endsWith(".class"))) {
						arrayList.add(fileDir);
					}
				}
			}
		}
	}

	public static boolean createTimeFile(List<TimeRecord> list, String outputPath)
			throws FileNotFoundException, UnsupportedEncodingException {
		String filePath = outputPath;
		StringBuffer sb = new StringBuffer("no,eclipse,ours\n");
		for (TimeRecord r : list) {
			sb.append(r.getId() + "," + r.getEclipseTime() + "," + r.getOursTime() + "\n");
		}
		boolean flag = true;
		try {
			File file = new File(filePath);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			Writer write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			write.write(sb.toString());
			write.flush();
			write.close();
		} catch (Exception e) {
			flag = false;
			e.printStackTrace();
		}
		return flag;
	}

}
