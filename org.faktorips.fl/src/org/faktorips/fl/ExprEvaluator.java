/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.fl;

import bsh.Interpreter;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.util.ArgumentCheck;

/**
 * The <code>ExprEvaluator</code> evaluates a given expression and returns it's result.
 * <p>
 * Technically this is done by first compiling the expression to Java sourcecode using the
 * {@link ExprCompiler}. After that a java bean shell {@link "http://www.beanshell.org"} interpreter
 * interpretes the expression and the result is return by the evaluate method of this class.
 * 
 * @author Jan Ortmann
 */
public class ExprEvaluator {

    // The compiler used to compile the formula into standard Java sourcecode
    private ExprCompiler compiler;

    private ClassLoader classLoader;

    /**
     * Constructs a new processor for the given compiler.
     */
    public ExprEvaluator(ExprCompiler compiler) {
        ArgumentCheck.notNull(compiler);
        this.compiler = compiler;
    }

    /**
     * Constructs a new processor for the given compiler and class loader.
     */
    public ExprEvaluator(ExprCompiler compiler, ClassLoader classLoader) {
        this(compiler);
        this.classLoader = classLoader;
    }

    /**
     * Evaluates and returns the result of the given expression.
     */
    public Object evaluate(String expression) throws Exception {
        // compiles the expression to Java sourcecode
        JavaCodeFragment fragment = compileExpressionToJava(expression);
        Interpreter i = new Interpreter(); // Construct an interpreter
        if (classLoader != null) {
            i.setClassLoader(classLoader);
        }

        StringBuffer sb = new StringBuffer();
        sb.append(fragment.getImportDeclaration().toString());
        sb.append(System.getProperty("line.separator")); //$NON-NLS-1$
        sb.append(fragment.getSourcecode());
        return i.eval(sb.toString());
    }

    /**
     * Evaluates and returns the result of the given expression. If the expression contains any
     * variables the variables can be specified with the variables parameter and the values of the
     * variables can be specified by means of the variableValues parameter.
     */
    public Object evaluate(String expression, String[] variables, Object[] variableValues) throws Exception {

        ArgumentCheck.length(variableValues, variables.length,
                "the variableValues parameter and the variables parameter need to have the same amount of values.");
        Interpreter i = new Interpreter(); // Construct an interpreter
        if (classLoader != null) {
            i.setClassLoader(classLoader);
        }
        for (int r = 0; r < variables.length; r++) {
            i.set(variables[r], variableValues[r]);
        }
        JavaCodeFragment fragment = compileExpressionToJava(expression);

        StringBuffer sb = new StringBuffer();
        sb.append(fragment.getImportDeclaration().toString());
        sb.append(System.getProperty("line.separator")); //$NON-NLS-1$
        sb.append(fragment.getSourcecode());
        return i.eval(sb.toString());
    }

    /**
     * Evaluates and return the result of the given java code fragment
     */
    public Object evaluate(JavaCodeFragment javaCodeFragment) throws Exception {
        Interpreter i = new Interpreter(); // Construct an interpreter
        if (classLoader != null) {
            i.setClassLoader(classLoader);
        }

        StringBuffer sb = new StringBuffer();
        sb.append(javaCodeFragment);

        // execute the expression.
        return i.eval(sb.toString());
    }

    /**
     * Compiles expression to Java and returns the CompilationResult.
     */
    private JavaCodeFragment compileExpressionToJava(String expression) throws Exception {
        CompilationResult result = compiler.compile(expression);
        if (result.failed()) {
            throw new Exception(result.getMessages().toString());
        }
        return result.getCodeFragment();
    }

}
