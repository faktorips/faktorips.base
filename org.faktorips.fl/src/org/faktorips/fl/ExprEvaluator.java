package org.faktorips.fl;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.util.ArgumentCheck;

import bsh.Interpreter;


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
	

	
	/**
	 * Constructs a new processor for the given compiler. 
	 */
	public ExprEvaluator(ExprCompiler compiler) {

		ArgumentCheck.notNull(compiler);
		compiler_ = compiler;

	}

	public Object evaluate(String expression) throws Exception
	{
	    // compiles the expression to Java sourcecode 
	    JavaCodeFragment fragment = compileExpressionToJava(expression);
	    Interpreter i = new Interpreter();  // Construct an interpreter
	    
	    StringBuffer sb = new StringBuffer();
	    sb.append(fragment.getImportDeclaration().toString());
	    sb.append(System.getProperty("line.separator"));
	    sb.append(fragment.getSourcecode());
	    
	    
		// execute the expression.
		return i.eval(sb.toString());
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
	
	
	
}
