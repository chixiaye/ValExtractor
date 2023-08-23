package valextractor.handlers;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;

import valextractor.utils.Constants;
import valextractor.utils.Utils;

public class ComparePatchHandler {
	String projectName;

	public ComparePatchHandler(String projectName) {
		this.projectName = projectName;
	}

	public void run() {
		String prefix = Constants.EXP2_RESULT_PATH + this.projectName;
		String ideaPath = prefix + "/idea";
		String oursPath = prefix + "/ours";
		String eclipsePath = prefix + "/eclipse";
		ArrayList<File> ideaFiles = new ArrayList<>();
		ArrayList<File> oursFiles = new ArrayList<>();
		ArrayList<File> eclipseFiles = new ArrayList<>();
		Utils.getFileList(ideaFiles, ideaPath);
		Utils.getFileList(oursFiles, oursPath);
		Utils.getFileList(eclipseFiles, eclipsePath);
		System.out.println(ideaFiles.size());
		assertTrue(ideaFiles.size() == oursFiles.size() && oursFiles.size() == eclipseFiles.size());
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
		Collections.sort(ideaFiles, cmp);
		Collections.sort(oursFiles, cmp);
		Collections.sort(eclipseFiles, cmp);
//		oursFiles.sort(cmp);
		for (int i = 1 - 1; i < oursFiles.size(); ++i) {
			String outputPath = prefix + "/compare/" + oursFiles.get(i).getName();
//			if (new File(outputPath).length() == 0) {
//				continue;
//			}

			System.out.println(oursFiles.get(i).getName());
			File tempOursFile = new File("/tmp/" + "ours.java");
			generateTemp(Constants.EXP2_PROJECT_ROOT + this.projectName + "/", oursFiles.get(i), tempOursFile);
			File tempIdeaFile = new File("/tmp/" + "idea.java");
			generateTemp(Constants.EXP2_PROJECT_ROOT + this.projectName + "/", ideaFiles.get(i), tempIdeaFile);
			File tempEclipseFile = new File("/tmp/" + "eclipse.java");
			generateTemp(Constants.EXP2_PROJECT_ROOT + this.projectName + "/", eclipseFiles.get(i), tempEclipseFile);
			long eclipseLineNum = getFileLineNum(tempEclipseFile);
			long ideaLineNum = getFileLineNum(tempIdeaFile);
			long oursLineNum = getFileLineNum(tempOursFile);
			if (oursFiles.get(i).length() == 0 && eclipseFiles.get(i).length() == 0) {
				continue;
			} else if (oursLineNum == ideaLineNum && oursLineNum == eclipseLineNum) {
				comparePatchFile(outputPath);
			} else {
				comparePatchFile(outputPath, tempOursFile, tempEclipseFile, tempIdeaFile);
			}

//			if (ideaLineNum != eclipseLineNum) {
//				System.out.println(oursFiles.get(i).getName() + ", " + ideaLineNum + ", " + eclipseLineNum);
//			}
//			break;
//			try {
//				comparePatchFile(oursFiles.get(i).getName(), tempOursFile, tempEclipseFile, tempIdeaFile);
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//			}
		}
	}

	public void run2(String jsonName) {
//		ArrayList<>
		String prefix = Constants.EXP1_RESULT_PATH + jsonName;
		String ideaPath = prefix + "/idea";
		String oursPath = prefix + "/ours";
		String eclipsePath = prefix + "/eclipse";
		ArrayList<File> ideaFiles = new ArrayList<>();
		ArrayList<File> oursFiles = new ArrayList<>();
		ArrayList<File> eclipseFiles = new ArrayList<>();
		Utils.getFileList(ideaFiles, ideaPath);
		Utils.getFileList(oursFiles, oursPath);
		Utils.getFileList(eclipseFiles, eclipsePath);
		System.out.println(ideaFiles.size());
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
//		Collections.sort(ideaFiles, cmp);
		Collections.sort(oursFiles, cmp);
//		Collections.sort(eclipseFiles, cmp);
//		oursFiles.sort(cmp); 
		
		for (int i = 1-1;    i < oursFiles.size(); ++i) {
			String outputPath = prefix + "/compareEclipseAndIdea/" + oursFiles.get(i).getName();
			String[] str = oursFiles.get(i).getName().split("_");

			System.out.println(oursFiles.get(i).getName());

			File ideaPatch = null;
			for (File f : ideaFiles) {
				if (f.getName().equals(oursFiles.get(i).getName())) {
					ideaPatch = f;
					break;
				}
			}
			File eclipsePatch = null;
			for (File f : eclipseFiles) {
				if (f.getName().equals(oursFiles.get(i).getName())) {
					eclipsePatch = f;
					break;
				}
			}
			if(ideaFiles==null)
				continue;
			assertTrue(eclipsePatch != null && ideaFiles != null);
			File tempIdeaFile = new File("/tmp/" + "idea.java");
//			ideaFiles.get(i);
			generateTemp(Constants.EXP1_PROJECT_ROOT + this.projectName + "/", ideaPatch, tempIdeaFile, str[1]);
//			System.out.println(tempIdeaFile.length());
			File tempEclipseFile = new File("/tmp/" + "eclipse.java");
			generateTemp(Constants.EXP1_PROJECT_ROOT + this.projectName + "/", eclipsePatch, tempEclipseFile, str[1]);
//			System.out.println(tempEclipseFile.length());
			long eclipseLineNum = getFileLineNum(tempEclipseFile);
			long ideaLineNum = getFileLineNum(tempIdeaFile);
			if (ideaLineNum == eclipseLineNum) {
				comparePatchFile(outputPath);
			} else {
				comparePatchFile(outputPath, tempEclipseFile, tempIdeaFile);
			}
//			break;
		}
	}

	// apply patch
	// format -> output to tempFile
	// reset
	String applyPatch(String projectPath, File patch) {
		try {
//			if(patch.getPath().contains("/idea/")) {
//				patch=convertEncoding(patch);
//			}
			Git git = Git.open(new File(projectPath));
			InputStream in = new FileInputStream(patch);
			git.apply().setPatch(in).call();
			Set<String> changed = git.status().call().getModified();
			for (String s : changed) {
//				System.out.println(s);
				if (s.endsWith(".java")) {
					return s;
				}
			}
//			in = new FileInputStream(convertEncoding(patch));
//			git.apply().setPatch(in).call();
//			changed = git.status().call().getModified();
//			for (String s : changed) {
//				System.out.println(s);
//				if (s.endsWith(".java")) {
//					return s;
//				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static File convertEncoding(File f) throws Exception {
		if (!f.isFile()) {
			return null;
		}
		InputStreamReader isr = new InputStreamReader(new FileInputStream(f), "utf-16le");
		File filenew = new File("/tmp/1.patch");
 
		filenew.createNewFile(); 
		FileOutputStream fos = new FileOutputStream(filenew);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
		int len = 0;
		char[] c = new char[1024];
		while ((len = isr.read(c)) > 0) {
			osw.write(c, 0, len);
		}
		isr.close();
		osw.close();  
		filenew.renameTo(f);
		return f;
	}

//	void format(File inFile) {
//		try {
//			BufferedReader in = new BufferedReader(new FileReader(inFile));
//			StringBuffer sb = new StringBuffer();
//			while (in.readLine() != null) {
//				sb.append(in.readLine());
//			}
//			in.close();
//			String formattedSource = new Formatter().formatSource(sb.toString()); 
//			System.out.println(formattedSource.equals(sb.toString()));
//			BufferedWriter out = new BufferedWriter(new FileWriter(inFile));
//			out.write(formattedSource);
//            out.close();
//		} catch (IOException | FormatterException e) { 
//			e.printStackTrace();
//		}
//	}
//
	void reset(String projectPath) {
		Git git = null;
		try {
			git = Git.open(new File(projectPath));
			Iterable<RevCommit> iterable = git.log().call();
			Iterator<RevCommit> iterator = iterable.iterator();
			git.checkout().setForced(true).setName(iterator.next().getName()).call();
			git.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void checkout(String projectPath, String sha) {
		Git git = null;
		try {
			git = Git.open(new File(projectPath));
			git.checkout().setForced(true).setName(sha).call();
			git.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void generateTemp(String projectPath, File patch, File tempFile, String... sha) {
		reset(projectPath);
		if (sha != null)
			checkout(projectPath, sha[0]);
		String appliedFileName = projectPath + applyPatch(projectPath, patch);
//		System.out.println(appliedFileName);
		createPatchFile(tempFile, projectPath);
		reset(projectPath);
	}

	public static boolean comparePatchFile(String outputPath, File... inputFile) {

		File file = new File(outputPath);
//		try {
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		if (file.exists()) {
			file.delete();
		}
//			file.createNewFile();
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
		Runtime r1 = Runtime.getRuntime();
		Process p1 = null;
		try {
			StringBuffer sb = new StringBuffer();
			if (inputFile == null || inputFile.length == 0) {
				sb.append("touch " + " \"" + outputPath.replace("\"", "\\\"") + "\"");
			} else {
				sb.append("tail -n +1 ");
				for (File f : inputFile) {
					String s = f.getPath();
					sb.append("\"" + s + "\" ");
				}
				sb.append("> \"" + outputPath.replace("\"", "\\\"") + "\"");
			}
			String[] cmd = new String[] { "sh", "-c", sb.toString() };
			p1 = r1.exec(cmd);
			p1.waitFor();
			p1.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	static boolean createPatchFile(File file, String path) {
		Git git = null;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String diffText = "";
		try {
			git = Git.open(new File(path));
			DiffFormatter df = new DiffFormatter(outputStream);
			df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
			df.setRepository(git.getRepository());
			df.setPathFilter(PathSuffixFilter.create(".java"));
//			List<DiffEntry> diff = git.diff().setPathFilter(PathSuffixFilter.create(".java")).setOutputStream(outputStream).call();
			String string = git.log().call().iterator().next().getId().getName();
			AbstractTreeIterator commitTreeIterator = prepareTreeParser(git.getRepository(), string);
			FileTreeIterator workTreeIterator = new FileTreeIterator(git.getRepository());
			List<DiffEntry> diffEntries = df.scan(commitTreeIterator, workTreeIterator);

			// git.diff().setPathFilter(PathSuffixFilter.create(".java")).setOutputStream(outputStream).call();

//            git.diff().setPathFilter()
			int validSize = 0;
			if (diffEntries.size() > 1) {
				for (DiffEntry diffEntry : diffEntries) {
					if (diffEntry.getOldPath().endsWith("java")) {
//						System.out.println(diffEntry.getOldPath()+","+diffEntry.getScore());
						validSize++;
					}
				}
			}
//			if(diffEntries.size() > 1 && validSize > 1) {
//				return null;
//			}
//			assertTrue(diffEntries.size() <= 1 || validSize <= 1);
			for (DiffEntry diffEntry : diffEntries) {
				if (diffEntry.getOldPath().endsWith(".java") ) {
					df.format(diffEntry);
					diffText= outputStream.toString("UTF-8");
				}
//				System.out.println(diffText);
			}
			git.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			Writer write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			write.write(diffText);
//			System.out.println(outputStream.toString());
			write.flush();
			write.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	long getFileLineNum(File file) {
		try {
			return Files.lines(file.toPath()).count();
		} catch (IOException e) {
			return -1;
		}
	}

	private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
		// from the commit we can build the tree which allows us to construct the
		// TreeParser
		// noinspection Duplicates
		try (RevWalk walk = new RevWalk(repository)) {
			RevCommit commit = walk.parseCommit(ObjectId.fromString(objectId));
			RevTree tree = walk.parseTree(commit.getTree().getId());

			CanonicalTreeParser treeParser = new CanonicalTreeParser();
			try (ObjectReader reader = repository.newObjectReader()) {
				treeParser.reset(reader, tree.getId());
			}

			walk.dispose();

			return treeParser;
		}
	}

}
