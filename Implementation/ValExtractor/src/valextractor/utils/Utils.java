package valextractor.utils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.*;

import data.json.Exp2Record;
import valextractor.log.MyLog;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	
	public static int appearNumber(String srcText, String findText) {
		int count = 0;
		Pattern p = Pattern.compile(findText);
		Matcher m = p.matcher(srcText);
		while (m.find()) {
			count++;
		}
		return count;
	}

	
	public static int getNum(String s) {
		int indexS = 0;
		int indexE = s.indexOf('_');
//		System.out.println(s.substring(indexS, indexE));
		return Integer.valueOf(s.substring(indexS, indexE));
	}

	public static CompilationUnit getCompilationUnit(ICompilationUnit iCompilationUnit) {
		ASTParser astParser = ASTParser.newParser(AST.JLS8);
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		astParser.setSource(iCompilationUnit);
		astParser.setResolveBindings(true);
		astParser.setBindingsRecovery(true);
		CompilationUnit unit = (CompilationUnit) (astParser.createAST(null));
		return unit;
	}

	public static ASTParser getNewASTParser(String[] sourcepathEntries, String[] encodings) {
		ASTParser astParser;
		astParser = ASTParser.newParser(AST.JLS8);
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		astParser.setEnvironment(null, sourcepathEntries, encodings, true);
//	        astParser.setEnvironment(classpathEntries, sourcepathEntries, encodings, true);
		astParser.setResolveBindings(true);
		astParser.setBindingsRecovery(true);
		astParser.setUnitName("");
		Map options = JavaCore.getOptions();
		astParser.setCompilerOptions(options);
		return astParser;
	}

	public static ASTParser getNewASTParser() {
		ASTParser astParser;
		astParser = ASTParser.newParser(AST.JLS8);
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		return astParser;
	}

	static public HashSet<String> getSourceTypeNames(ITypeBinding type) {
		HashSet<String> nomes = new HashSet<String>();

		if (type.isFromSource()) {
			nomes.add(type.getQualifiedName());
		}

		// A[] returns if A is from Source
		if (type.isArray() && type.getElementType().isFromSource())
			nomes.add(type.getElementType().getQualifiedName());

		// T<A>, T<A,B> - true if A is from Source or if A or B are source types and its
		// a Collection
		// Collections with source as parameters are considered because represents a
		// relationship with a source type
		if (type.isParameterizedType()) {
			ITypeBinding[] interfaces = type.getInterfaces();
			boolean isCollection = false;
			for (ITypeBinding iTypeBinding : interfaces) {
				isCollection = isCollection || iTypeBinding.getBinaryName().equals("java.util.Collection");
			}

			if (isCollection) {
				for (ITypeBinding typeArg : type.getTypeArguments()) {
					if (typeArg.isFromSource())
						nomes.add(typeArg.getQualifiedName());
				}
			}
		}
		return nomes;
	}

	static public boolean isStatic(Object obj) {
		HashSet<Modifier> modifiers = new HashSet<Modifier>();
		if (obj instanceof FieldDeclaration) {
			FieldDeclaration field = (FieldDeclaration) obj;
			for (Object o : field.modifiers()) {
				if (o instanceof Modifier) {
					Modifier modifier = (Modifier) o;
					modifiers.add(modifier);
				}
			}
		}
		if (obj instanceof MethodDeclaration) {
			MethodDeclaration method = (MethodDeclaration) obj;
			for (Object o : method.modifiers()) {
				if (o instanceof Modifier) {
					Modifier modifier = (Modifier) o;
					modifiers.add(modifier);
				}
			}
		}
		for (Modifier modifier : modifiers)
			if (modifier.isStatic())
				return true;
		return false;
	}

	static public <E> Collection<E> getIntersection(Collection<E> a, Collection<E> b) {
		Collection<E> res = new HashSet<E>();
		res.addAll(a);
		res.retainAll(b);
		return res;
	}

	static public <E> Collection<E> getUnion(Collection<E> a, Collection<E> b) {
		Collection<E> res = new HashSet<E>();
		res.addAll(a);
		res.addAll(b);
		return res;
	}

	static public String getCodeFromFile(File javaFile)   {
		byte[] input = null ; 
		try {
			BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(javaFile));
			input = new byte[bufferedInputStream.available()];
			bufferedInputStream.read(input);
			bufferedInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		 
		return new String(input);
	}
 

	public static String getVariableQualifiedName(VariableDeclaration variableDeclaration) {
		String qualifiedName = "";
		if (variableDeclaration.resolveBinding() instanceof IVariableBinding) {
			IVariableBinding variableBinding = variableDeclaration.resolveBinding();
			ITypeBinding typeBinding = variableBinding.getDeclaringClass();
			if (typeBinding != null) {
				qualifiedName = typeBinding.getQualifiedName() + "." + variableBinding.getName();
			}
		}
		return qualifiedName;
	}

	public static String getMethodQualifiedName(MethodDeclaration methodDeclaration) {
		String qualifiedName = "";
		if (methodDeclaration.resolveBinding() instanceof IMethodBinding) {
			IMethodBinding methodBinding = methodDeclaration.resolveBinding();
			ITypeBinding typeBinding = methodBinding.getDeclaringClass();
			if (typeBinding != null) {
				qualifiedName = typeBinding.getQualifiedName() + "." + methodBinding.getName();
			}
		}
		return qualifiedName;
	}

	public static String getMethodSignature(MethodDeclaration method) {
		String signature = "";
		signature += method.getName().toString() + "(";
		int parameterSize = method.parameters().size();
		for (int i = 0; i < parameterSize; i++) {
			Object obj = method.parameters().get(i);
			if (obj instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration parameter = (SingleVariableDeclaration) obj;
				signature += parameter.getType().toString() + " " + parameter.getName().toString();
				if (i != parameterSize - 1) {
					signature += ", ";
				}
			}

		}
		return signature + ")";
	}

	public static String getMethodSignature(IMethodBinding methodBinding) {
		String signature = "";
		signature += methodBinding.getName() + "(";
		ITypeBinding[] typeBindings = methodBinding.getParameterTypes();
		int parameterSize = typeBindings.length;
		for (int i = 0; i < parameterSize; i++) {
			signature += typeBindings[i].getName();
			if (i != parameterSize - 1) {
				signature += ", ";
			}
		} 
		return signature + ")";
	}
 
 
	public static boolean isStartWithNumber(String str) {
	    Pattern pattern = Pattern.compile("[0-9]*");
	    Matcher isNum = pattern.matcher(str.charAt(0)+"");
	    if (!isNum.matches()) {
	        return false;
	    }
	    return true;
	}

	static public ICompilationUnit getICompilationUnit(IJavaProject javaProject, Exp2Record extractVariableRecord)
			throws JavaModelException {
		ICompilationUnit iCompilationUnit = null;
		if (javaProject == null || !javaProject.exists()) {
			return null;
		}
		IPackageFragment[] fragments = javaProject.getPackageFragments();
		if (fragments.length == 0) {
			MyLog.add("null fragments!");
		}
		for (int j = 0; j < fragments.length; j++) {
			IPackageFragment fragment = fragments[j];
			if (!fragment.exists()) {
				continue;
			}
			ICompilationUnit[] iCompilationUnits = fragment.getCompilationUnits();
			for (int k = 0; k < iCompilationUnits.length; k++) {
				if (iCompilationUnits[k].getPath().toString().endsWith(extractVariableRecord.getPath())) {
					return  iCompilationUnit = iCompilationUnits[k];
				}
			}
		}
		return iCompilationUnit;
	}
	
	public static void createFileAndWrite(String path, String content) {
		byte[] sourceByte = content.getBytes();
		if (null != sourceByte) {
			try {
				File file = new File(path);  
				if (!file.exists()) {  
					File dir = new File(file.getParent());
					dir.mkdirs();
					file.createNewFile();
				}
				FileOutputStream outStream = new FileOutputStream(file);
				outStream.write(sourceByte);
				outStream.close(); 
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static ArrayList<String> getJavaFiles(String path) {
		ArrayList<String> files = new ArrayList<String>();
		ArrayList<File> tempList = new ArrayList<File>();
		getFileList(tempList, path);
		for (int i = 0; i < tempList.size(); i++) {
			files.add(tempList.get(i).toString());
		}
		return files;
	}
	
	public static void getFileList(ArrayList<File> arrayList, String strPath) {
		File fileDir = new File(strPath);
		if (null != fileDir && fileDir.isDirectory()) {
			File[] files = fileDir.listFiles();
			if (null != files) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						getFileList(arrayList, files[i].getPath());
					} else {
						String strFileName = files[i].getPath();
						if (files[i].exists() && (strFileName.endsWith(".java") || strFileName.endsWith(".jar")
								|| strFileName.endsWith(".class")
								|| strFileName.endsWith(".patch"))) {
							arrayList.add(files[i]);
						}
					}
				}
			} else {
				if (null != fileDir) {
					String strFileName = fileDir.getPath();
					if (fileDir.exists() && (strFileName.endsWith(".java") || strFileName.endsWith(".jar")
							|| strFileName.endsWith(".class")
							|| strFileName.endsWith(".patch"))) {
						arrayList.add(fileDir);
					}
				}
			}
		}
	}
	
	public static ArrayList<String[]> CSVReader(String csvFile) {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        ArrayList<String[]> list=new ArrayList<>();
        try {
 
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
 
                // use comma as separator
                String[] country = line.split(cvsSplitBy); 
                list.add(country);
            }
 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }
}
