package valextractor.handlers;

import java.io.File;
import java.util.ArrayList;

import valextractor.utils.Constants;
import valextractor.utils.Utils;

public class NumsCounterHandler {
	String projectName;
	int[] nums;
	public NumsCounterHandler(String projectName, int[] nums) {
		super();
		this.projectName = projectName;
		this.nums = nums;
	}
	public void run() {
		String prefix = Constants.EXP2_RESULT_PATH + this.projectName;
		String oursPath = prefix + "/ours";
		String eclipsePath = prefix + "/eclipse";
		ArrayList<File> oursFiles = new ArrayList<>();
		ArrayList<File> eclipseFiles = new ArrayList<>();
		Utils.getFileList(oursFiles, oursPath);
		Utils.getFileList(eclipseFiles, eclipsePath);
		for(int i=0;i<this.nums.length;++i) {
			int v=nums[i];
			for(int j=0;j<oursFiles.size();++i) {
				
			}
		}
	}
}
