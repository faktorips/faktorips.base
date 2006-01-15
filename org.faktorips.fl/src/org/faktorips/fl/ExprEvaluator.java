package org.faktorips.fl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.lang.SystemUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;


/**
 * The <code>ExprEvaluator</code> evaluates a given expression and returns it's result.
 * <p> 
 * Technically this is done by first compiling the expression to Java sourcecode
 * using the {@link ExprCompiler}. The temporary file containing the Java sourcecode
 * is created via <code>java.io.File#createTempFile(String)</code> and starts with
 * "Expression". The Java sourcefile is then compiled via javac. To compile successfully
 * <code>tools.jar</code> has to be in the classpath along with all classes referenced
 * in the expression, e.g. <code>{@link org.faktorips.values.Decimal}</code>.
 * Then the new class is loaded via an <code>URLClassloader</code> that is a child of this 
 * class' classloader. Finally an instance of the class is created and 
 * it's <code>execute()</code> method called.
 *     
 * @author Jan Ortmann
 */
public class ExprEvaluator
{

	// The compiler used to compile the formula into standard Java sourcecode
	private ExprCompiler compiler_;
	
	// These classpath used to compile the Java sourcecode.
	private String classpath_;
	
	/**
	 * Constructs a new processor for the given compiler. The system classpath 
	 * obtained via <code>System.getProperty("java.class.path")</code> is used to
	 * compile the generated Java sourcecode.
	 */
	public ExprEvaluator(ExprCompiler compiler) {
	    this(compiler, SystemUtils.JAVA_CLASS_PATH);
	}
    
	/**
	 * Constructs a new processor for the given compiler using the given classpath
	 * to compile the generated Java sourcecode.
	 */
	public ExprEvaluator(ExprCompiler compiler, String classpath)
	{
		ArgumentCheck.notNull(compiler);
		ArgumentCheck.notNull(classpath);
		compiler_ = compiler;
		classpath_ = classpath;
	}

	public Object evaluate(String expression) throws Exception
	{
	    // compiles the expression to Java sourcecode 
	    JavaCodeFragment fragment = compileExpressionToJava(expression);
	    
	    // put the expression into a Java sourcefile in a class implementing
	    // the CompiledExpression interface.
	    File srcFile = createJavaSourceFile(fragment);
	    
	    // compile the Java sourcefile into a class file.
	    File classFile = compileJavaSrcFile(srcFile);
	    
	    // get an instance of the created class 
		CompiledExpression compiledExpression = getCompiledExpression(classFile);
		
		// execute the expression.
		return compiledExpression.execute();
	}
	
	/**
	 * Compiles expression to Java and returns the CompilationResult.
	 */
	private JavaCodeFragment compileExpressionToJava(String expression) 
		throws Exception
	{
	    compiler_.setEnsureResultIsObject(true);
		CompilationResult result = compiler_.compile(expression);
		if(result.failed())
		{
			throw new Exception(result.getMessages().toString());
		}
		return result.getCodeFragment();
	}
	
	/**
	 * Create an executable formula by creating a Java Class encapsulating the compiled formula.
	 * The created Java class implements the CompiledExpression interface.
	 */ 
	private File createJavaSourceFile(JavaCodeFragment fragment)
		throws Exception
	{
		File sourceFile = File.createTempFile("Expression", ".java");
		sourceFile.deleteOnExit();
		int dotPos = sourceFile.getName().indexOf('.');
		String className = sourceFile.getName().substring(0, dotPos);
		
	    JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
	    builder.javaDoc("Generated class used for executing the formula");
		builder.classBegin(Modifier.PUBLIC, className, Object.class, new Class[] {CompiledExpression.class});
		builder.methodBegin(Modifier.PUBLIC, Object.class, "execute", null, null);
		builder.append("return ");
		builder.append(fragment);
		builder.appendln(";");
		builder.methodEnd();
		builder.classEnd();
		
		FileWriter writer = new FileWriter(sourceFile);
		writer.write(builder.getFragment().toString());
		writer.close();
		return sourceFile;
	}
	
	/**
	 * Compiles the source file and returns the class file.
	 */
	private File compileJavaSrcFile(File sourceFile) 
		throws Exception
	{
		// Load the java compiler class
		Class javaCompilerClass = Class.forName(JAVAC_CLASS_NAME);
		
		// Construct the parameter types for the java compiler class constructor 
		Class[] javaCompilerConstructorParamTypes = {Class.forName("java.io.OutputStream"), Class.forName("java.lang.String")};	
		
		// Get the compiler class constructor
		Constructor javaCompilerConstructor = javaCompilerClass.getConstructor(javaCompilerConstructorParamTypes);
		
		// Construct the parameters for the java compiler class constructor
		OutputStream outputStream = new ByteArrayOutputStream();
		Object[] javaCompilerConstructorParams = {outputStream, "javac"};
		
		// Create a new instance of the compiler class
		Object javaCompilerObject = javaCompilerConstructor.newInstance(javaCompilerConstructorParams);
		
		// Construct the parameter types for the compilation method
		Class[] compilationMethodParamTypes = {(new String[0]).getClass()};
		
		// Get the compilation method
		Method compilationMethod = javaCompilerClass.getMethod("compile", compilationMethodParamTypes);
		
		// Construct the parameters for the compilation method
		// The first four arguments specify the class path and the output directory 
		// The last argument is the source file name
		String[] compileParams = new String[5];
		compileParams[0] = "-classpath";
		compileParams[1] = classpath_;
		compileParams[2] = "-d";
		compileParams[3] = SystemUtils.JAVA_IO_TMPDIR;
		compileParams[4] = sourceFile.getPath() ;
		
		// Construct the parameters for the compilation method.
		Object[] compileParamsObjects = {compileParams};

		// Invoke the compilation method of the compiler object
		if (!((Boolean)compilationMethod.invoke(javaCompilerObject, compileParamsObjects)).booleanValue())
		{ 
			throw new Exception("Compilation of Java sourcefile failed due to " + outputStream.toString());
		}
		String compiledClassName = StringUtil.getFilenameWithoutExtension(sourceFile.getName()) + ".class";
		File classFile = new File(sourceFile.getParentFile(), compiledClassName);
		classFile.deleteOnExit();
		return classFile;
	}
	
	/**
	 * Returns an implementation of CompiledExpression which represents
	 * the result of the expression compilation.
	 */
	protected CompiledExpression getCompiledExpression(File classFile)
		throws Exception
	{
		URL classUrl = new URL("file:" + File.separator +
				classFile.getParentFile().getPath()+File.separator);
		ClassLoader classloader = this.getClass().getClassLoader();
		URLClassLoader formulaClassLoader = new URLClassLoader(
				new URL[] {classUrl}, classloader);
		Class executeableFormula = Class.forName(StringUtil.getFilenameWithoutExtension(classFile.getName()), true, formulaClassLoader);
		Object instance = executeableFormula.newInstance();
		return (CompiledExpression)instance;
	}
	
	private final static String JAVAC_CLASS_NAME = "sun.tools.javac.Main";
	
}
