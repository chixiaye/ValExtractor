package test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;

import org.junit.Test;

import valextractor.handlers.ComparePatchHandler;
import valextractor.utils.Utils;

public class ComparePatchHandlerTest {

	@Test
	public void test() {
//		new ComparePatchHandler("Math").run();
//		new ComparePatchHandler("commons-math").run2("commons-math");
//		new ComparePatchHandler("Mockito").run();
//		new ComparePatchHandler("Time").run(); 
//		int[] a = {8,32,70,110,111,139,140,189,217,221,261,297,336,519,594,653,686,862,869,880,893,897,916,931,982,1015,1053,1079,1084,1096,1102,1170,1181,1225,1245,1272,1374,1402,1418,1449,1476,1498,1559,1608,1636,1646,1649,1679,1700,1702,1709,1715,1748,1823,1862,1865,1879,1950,1975,1976,1990,2009,2011,2039,2056,2157,2176,2301,2386,2438,2477,2482,2488,2562,2579,2600,2702,2812,2862,2877,2938,2947,2963,3081,3091,3116,3180,3183,3252,3260,3284,3299,3440,3469,3476,3482,3488,3493,3536,3540,3551,3607,3729,3734,3754,3822,3845,3867,3870,3935,4033,4034,4041,4074,4075,4257,4282,4295,4312};
//		foo2("/patch/Math/compare", a, false);
	}

	public void foo1(String path) {

		ArrayList<File> list = new ArrayList<File>();
		Utils.getFileList(list, path);
		int diffCnt = 0;
		int sameCnt = 0;
		for (int i = 0; i < list.size(); ++i) {
			File file = list.get(i);
			if (file.length() > 0) {
				diffCnt++;
			} else {
				sameCnt++;
			}
		}
		System.out.println((diffCnt + sameCnt) + "," + sameCnt + "," + diffCnt);
	}

	public void foo2(String path,int[] arr,boolean flag) {

		ArrayList<File> list = new ArrayList<File>();
		Utils.getFileList(list, path); 
		for (int i = 0; i < list.size(); ++i) {
			String[] str=list.get(i).getName().split("_");
			int  v= Integer.valueOf(str[0]);
			long  len= list.get(i).length();
			for(int j=0;j<arr.length;++j) {
				if(v==arr[j]) {
					if(flag&&len>0 || !flag&&len==0 ) {
						
					}else {
						System.err.println("error in "+ list.get(i).getName());
					} 
					break;
				}
			}
		} 
	}
}
