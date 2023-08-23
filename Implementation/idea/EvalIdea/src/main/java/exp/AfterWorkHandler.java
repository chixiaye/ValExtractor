package exp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Scanner;

import com.intellij.openapi.project.Project;
import exp.data.json.Exp1Record;
import exp.data.json.Exp2Record;
import exp.log.MyLog;
import exp.utils.Constants;
import exp.utils.Utils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.Patch;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import static exp.utils.Constants.CaseStudyHashMap;

public class AfterWorkHandler {

	public static void reset(String projectPath) {
		Git git = null;
		try {
			git = Git.open(new File(projectPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Repository repository = git.getRepository();

		try {
			Iterable<RevCommit> iterable = git.log().call();
			Iterator<RevCommit> iterator = iterable.iterator();
			git.checkout().setForced(true).setName(iterator.next().getName()).call();
		} catch (GitAPIException e1) {
			e1.printStackTrace();
		}
		repository.close();
		git.clean();
		git.close();
	}

	public AfterWorkHandler(Project javaProject, Exp2Record exp2Record, String approach) {
		String projectPath = javaProject.getBasePath();
		if (javaProject == null || !javaProject.isOpen()) {
			return;
		}
		Git git = null;
		try {
			git = Git.open(new File(projectPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String tempName = exp2Record.getOldName().replace('/', '／').replace('\\', '＼');

		try {
			git.diff().setPathFilter(PathFilter.create(exp2Record.getPath())).setOutputStream(outputStream).call();

//			MyLog.add("res diff is:\n" + outputStream.toString());
//			System.out.println("res diff is:\n" + outputStream.toString());
			String name = Constants.EXP2_RESULT_PATH + exp2Record.getProjectName() + "/" + approach + "/"
					+ exp2Record.getNo() + "_" + tempName + ".patch";
			createPatchFile(name, outputStream.toString());
			Iterable<RevCommit> iterable = git.log().call();
			Iterator<RevCommit> iterator = iterable.iterator();
			git.checkout().setForced(true).setName(iterator.next().getName()).call();
			git.gc().call();
		} catch (GitAPIException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		git.close();
	}

	public AfterWorkHandler(Project javaProject, Exp1Record exp1Record, String approach) {
		String projectPath = javaProject.getBasePath();
		if (javaProject == null || !javaProject.isOpen()) {
			return;
		}
		Git git = null;
		try {
			git = Git.open(new File(projectPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String tempName = exp1Record.getOldName().replace('/', '／').replace('\\', '＼');

		try {
			git.diff().setPathFilter(PathFilter.create(exp1Record.getPath())).setOutputStream(outputStream).call();

//			MyLog.add("res diff is:\n" + outputStream.toString());
//			System.out.println("res diff is:\n" + outputStream.toString());
			String name = Constants.EXP1_RESULT_PATH + CaseStudyHashMap.get(exp1Record.getProjectName()) + "/" + approach + "/"
					+ exp1Record.getNo() + "_" + tempName + ".patch";
			createPatchFile(name, outputStream.toString());
			Iterable<RevCommit> iterable = git.log().call();
			Iterator<RevCommit> iterator = iterable.iterator();
			git.checkout().setForced(true).setName(iterator.next().getName()).call();
			git.gc().call();
		} catch (GitAPIException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		git.close();
	}

	/**
	 * @param git
	 */
	public void restoreCode(Git git) {
		try {
			git.reset().setMode(ResetType.HARD).call();
		} catch (RefAlreadyExistsException e) {
			e.printStackTrace();
		} catch (RefNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidRefNameException e) {
			e.printStackTrace();
		} catch (CheckoutConflictException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		git.close();
//		MyLog.add("restore!\n");
	}
	public static boolean createPatchFile(String name, String content)
			throws FileNotFoundException, UnsupportedEncodingException {
		try {
			File file = new File(name);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			Writer write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			write.write(content);
			write.flush();
			write.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean comparePatchFile(String inputFile1, String inputFile2, String outputFile)
			throws FileNotFoundException, UnsupportedEncodingException {
		File file1 = new File(inputFile1);
		File file2 = new File(inputFile2); 
		if (!file1.exists() || !file2.exists()) {
			System.out.println(file1.getAbsolutePath() + ", " + file1.exists());
//			java.util.logging.Logger log = new java.util.logging.Logger (outputFile);
			return false;
		}

		try {
			File file = new File(outputFile);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Runtime r1 = Runtime.getRuntime();
		Process p1 = null;
		try {
			String cmd0 = "git diff \"" + inputFile1 + "\" \"" + inputFile2 + "\" > \"" + outputFile + "\"";
			int status;
			String[] cmd = new String[] { "sh", "-c", cmd0 };
			p1 = r1.exec(cmd);
			status = p1.waitFor();
			p1.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("diff got!");
		return true;
	}

	public static void exp1Checkout(Exp1Record exp1Record){
		String projectPath=Constants.EXP1_PROJECT_ROOT +  exp1Record.getProjectName() + "/";
		Git git=null;
		try {
			git=Git.open(new File(projectPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Repository repository = git.getRepository();
//		MyLog.add("commit id:"+exp1Record.getCommitId());
		try {
			git.checkout().setForced(true).setName(exp1Record.getCommitId()).call();
		} catch (CheckoutConflictException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
//		try {
//			Iterable<RevCommit> iterable = git.log().call();
//			Iterator<RevCommit> iterator = iterable.iterator();
//			MyLog.add("switch to "+iterator.next().getName()+" !");
//		} catch (GitAPIException e) {
//			e.printStackTrace();
//		}
		repository.close();
		git.close();
	}
}
