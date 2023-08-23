package valextractor.handlers;

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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import data.json.Exp1Record;
import data.json.Exp2Record;
import valextractor.log.MyLog;
import valextractor.utils.Constants;
import valextractor.utils.Utils;

public class AfterWorkHandler {
	public AfterWorkHandler(IJavaProject javaProject) {
		String projectPath = Constants.EXP1_PROJECT_ROOT + javaProject.getPath().toString();
		Git git = null;
		try {
			git = Git.open(new File(projectPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		restoreCode(git);
	}

	public AfterWorkHandler(IJavaProject javaProject, Exp1Record exp1Record, String approach) {
		String projectPath = Constants.EXP1_PROJECT_ROOT + javaProject.getPath().toString();
		Git git = null;
		try {
			git = Git.open(new File(projectPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		String tempName = exp1Record.getNo() + "_" + exp1Record.getCommitId() + "_" + exp1Record.getNewName();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			git.diff().setPathFilter(PathFilter.create(exp1Record.getPath())).setOutputStream(outputStream).call();

			MyLog.add("res diff is:\n" + outputStream.toString());
			String name = Constants.EXP1_RESULT_PATH + exp1Record.getProjectName() + "/" + approach + "/" + tempName
					+ ".patch";
			createPatchFile(name, outputStream.toString());
		} catch (GitAPIException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		restoreCode(git);
		String file1 = Constants.EXP1_RESULT_PATH + exp1Record.getProjectName() + "/" + "eclipse" + "/" + tempName
				+ ".patch";
		String file2 = Constants.EXP1_RESULT_PATH + exp1Record.getProjectName() + "/" + "ours" + "/" + tempName
				+ ".patch";
		String file3 = Constants.EXP1_RESULT_PATH + exp1Record.getProjectName() + "/" + "compare" + "/" + tempName
				+ ".patch";
		try {
			if (approach.equals("ours"))
				comparePatchFile(file1, file2, file3);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * MockitoAnnotations.initMocks(this); initializes fields annotated with Mockito
	 * annotations.
	 * <p>
	 * <ul>
	 * <li>Allows shorthand creation of objects required for testing.</li>
	 * <li>Minimizes repetitive mock creation code.</li>
	 * <li>Makes the test class more readable.</li>
	 * <li>Makes the verification error easier to read because <b>field name</b> is
	 * used to identify the mock.</li>
	 * </ul>
	 * 
	 * <pre class="code">
	 * <code class=
	 * "java"> public class ArticleManagerTest extends SampleBaseTestCase {  &#064;Mock private ArticleCalculator calculator; &#064;Mock private ArticleDatabase database; &#064;Mock private UserProvider userProvider; private ArticleManager manager; &#064;Before public void setup() { manager = new ArticleManager(userProvider, database, calculator); } } public class SampleBaseTestCase { &#064;Before public void initMocks() { MockitoAnnotations.initMocks(this); } } </code>
	 * </pre>
	 * <p>
	 * Read also about other annotations &#064; {@link Spy}, &#064; {@link Captor},
	 * &#064; {@link InjectMocks}
	 * <p>
	 * <b><code>MockitoAnnotations.initMocks(this)</code></b> method has to called
	 * to initialize annotated fields.
	 * <p>
	 * In above example, <code>initMocks()</code> is called in &#064;Before (JUnit4)
	 * method of test's base class. For JUnit3 <code>initMocks()</code> can go to
	 * <code>setup()</code> method of a base class. You can also put initMocks() in
	 * your JUnit runner (&#064;RunWith) or use built-in runner:
	 * {@link MockitoJUnitRunner}
	 */
	public AfterWorkHandler(IJavaProject javaProject, Exp2Record exp2Record, String approach, boolean flag) {
		String projectPath = Constants.EXP2_PROJECT_ROOT + javaProject.getPath().toString();
		String tempName = exp2Record.getOldName().replace('/', '／').replace('\\', '＼');
		String name = Constants.JRRT_PATH + exp2Record.getProjectName() + "/" + approach + "/" + exp2Record.getNo()
				+ "_" + tempName + ".java";
		Path path = Paths.get(projectPath + "/" + exp2Record.getPath());
		Scanner scanner;
		StringBuffer sb = new StringBuffer();
		try {
			scanner = new Scanner(path);
			while (scanner.hasNextLine()) {
				sb.append(scanner.nextLine() + "\n");
			}
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Utils.createFileAndWrite(name, flag ? sb.toString() : "");
		reset(projectPath);
	}

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

	public AfterWorkHandler(IJavaProject javaProject, Exp2Record exp2Record,boolean flag, String approach)
			throws JavaModelException {
		String projectPath = Constants.EXP2_PROJECT_ROOT + javaProject.getPath().toString();
		if (javaProject == null || !javaProject.exists()) {
			return;
		}
		IPackageFragment[] fragments = javaProject.getPackageFragments();
		if (fragments.length == 0) {
			MyLog.add("null fragments!");
		}
		Git git = null;
		try {
			git = Git.open(new File(projectPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Repository repository = git.getRepository();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String tempName = exp2Record.getOldName().replace('/', '／').replace('\\', '＼');

		try {
			git.diff().setPathFilter(PathFilter.create(exp2Record.getPath())).setOutputStream(outputStream).call();

			MyLog.add("res diff is:\n" + outputStream.toString());
			System.out.println("res diff is:\n" + outputStream.toString());
			String name = Constants.EXP2_RESULT_PATH + exp2Record.getProjectName() + "/" + approach + "/"
					+ exp2Record.getNo() + "_" + tempName + ".patch";
			Iterable<RevCommit> iterable = git.log().call();
			Iterator<RevCommit> iterator = iterable.iterator();
			git.checkout().setForced(true).setName(iterator.next().getName()).call();
			createPatchFile(name, flag==true? outputStream.toString():"");
		} catch (GitAPIException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		repository.close();
		git.clean();
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
		MyLog.add("restore!\n");
	}

	public static boolean createPatchFile(String name, String content)
			throws FileNotFoundException, UnsupportedEncodingException {
		String filePath = name;
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
			write.write(content);
			write.flush();
			write.close();
		} catch (Exception e) {
			flag = false;
			e.printStackTrace();
		}
		return flag;
	}

	public static boolean comparePatchFile(String inputFile1, String inputFile2, String outputFile)
			throws FileNotFoundException, UnsupportedEncodingException {
		File file1 = new File(inputFile1);
		File file2 = new File(inputFile2); 
		if (!file1.exists() || !file2.exists()) {
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

}
