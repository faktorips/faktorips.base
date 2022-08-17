/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.util.ArgumentCheck;

import bsh.Interpreter;

/**
 * Evaluates a given expression and returns it's result.
 * <p>
 * Technically this is done by first compiling the expression to {@link JavaCodeFragment Java source
 * code} using the {@link JavaExprCompiler}. After that a java
 * <a href="https://beanshell.github.io/">bean-shell</a>} interpreter interprets the expression and
 * the result is returned by the evaluate method of this class.
 * 
 * @author Jan Ortmann
 */
public class ExprEvaluator {

    // The compiler used to compile the formula into standard Java source code
    private JavaExprCompiler compiler;

    private ClassLoader classLoader;

    /**
     * Constructs a new processor for the given compiler.
     */
    public ExprEvaluator(JavaExprCompiler compiler) {
        ArgumentCheck.notNull(compiler);
        this.compiler = compiler;
    }

    /**
     * Constructs a new processor for the given compiler and class loader.
     */
    public ExprEvaluator(JavaExprCompiler compiler, ClassLoader classLoader) {
        this(compiler);
        this.classLoader = classLoader;
    }

    /**
     * Evaluates and returns the result of the given expression.
     */
    public Object evaluate(String expression) throws Exception {
        // compiles the expression to Java source code
        JavaCodeFragment fragment = compileExpressionToJava(expression);
        Interpreter i = new Interpreter();
        if (classLoader != null) {
            i.setClassLoader(classLoader);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(fragment.getImportDeclaration().toString());
        sb.append(System.lineSeparator());
        sb.append(fragment.getSourcecode());
        return i.eval(sb.toString());
    }

    /**
     * Evaluates and returns the result of the given expression. If the expression contains any
     * variables the variables can be specified with the {@code variables} parameter and the values
     * of the variables can be specified by means of the {@code variableValues} parameter.
     */
    public Object evaluate(String expression, String[] variables, Object[] variableValues) throws Exception {

        ArgumentCheck.length(variableValues, variables.length,
                "the variableValues parameter and the variables parameter need to have the same amount of values.");
        Interpreter interpreter = new Interpreter();
        if (classLoader != null) {
            interpreter.setClassLoader(classLoader);
        }
        for (int r = 0; r < variables.length; r++) {
            interpreter.set(variables[r], variableValues[r]);
        }
        JavaCodeFragment fragment = compileExpressionToJava(expression);

        StringBuilder sb = new StringBuilder();
        sb.append(fragment.getImportDeclaration().toString());
        sb.append(System.lineSeparator());
        sb.append(fragment.getSourcecode());
        return interpreter.eval(sb.toString());
    }

    /**
     * Evaluates and returns the result of the given {@link JavaCodeFragment java code fragment}.
     */
    public Object evaluate(JavaCodeFragment javaCodeFragment) throws Exception {
        Interpreter interpreter = new Interpreter();
        if (classLoader != null) {
            interpreter.setClassLoader(classLoader);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(javaCodeFragment);

        // execute the expression.
        return interpreter.eval(sb.toString());
    }

    /**
     * Compiles expression to {@link JavaCodeFragment Java}.
     */
    private JavaCodeFragment compileExpressionToJava(String expression) throws Exception {
        CompilationResult<JavaCodeFragment> result = compiler.compile(expression);
        if (result.failed()) {
            throw new Exception(result.getMessages().toString());
        }
        return result.getCodeFragment();
    }

}
