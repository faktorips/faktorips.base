/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.Message;
import org.junit.Before;

/**
 *
 */
public abstract class JavaExprCompilerAbstractTest {

    protected JavaExprCompiler compiler;
    private ExprEvaluator processor;

    @Before
    public void setUp() throws Exception {
        compiler = new JavaExprCompiler();
        Locale.setDefault(Locale.ENGLISH);
        compiler.setLocale(Locale.ENGLISH);
        processor = new ExprEvaluator(compiler);
    }

    /**
     * Compiles the given expression and tests if the compilation was successfull and if the
     * datatype is the expected one. After that the expression is executed and it is tested if it
     * returns the expected value.
     */
    protected CompilationResult<JavaCodeFragment> execAndTestSuccessfull(String expression,
            Object expectedValue,
            Datatype expectedDatatype) throws Exception {

        CompilationResult<JavaCodeFragment> result = compiler.compile(expression);
        if (result.failed()) {
            System.out.println(result);
        }
        assertTrue(result.getMessages().toString(), result.successfull());
        assertEquals(expectedDatatype, result.getDatatype());

        Object value = processor.evaluate(expression);
        if (!ObjectUtils.equals(value, expectedValue)) {
            System.out.println(result);
        }
        assertEquals(expectedValue, value);
        return result;
    }

    protected CompilationResult<JavaCodeFragment> execAndTestSuccessfull(String expression,
            Object expectedValue,
            String[] parameterNames,
            Datatype[] parameterTypes,
            Object[] parameterValues,
            Datatype expectedDatatype) throws Exception {

        final Map<String, Datatype> parameterMap = new HashMap<String, Datatype>();
        for (int i = 0; i < parameterNames.length; i++) {
            parameterMap.put(parameterNames[i], parameterTypes[i]);
        }
        IdentifierResolver<JavaCodeFragment> resolver = new IdentifierResolver<JavaCodeFragment>() {
            public CompilationResult<JavaCodeFragment> compile(String identifier,
                    ExprCompiler<JavaCodeFragment> exprCompiler,
                    Locale locale) {
                Object paramDatatype = parameterMap.get(identifier);
                if (paramDatatype != null) {
                    return new CompilationResultImpl(identifier, (Datatype)paramDatatype);
                }
                return new CompilationResultImpl(new Message("",
                        "The parameter " + identifier + " cannot be resolved.", Message.ERROR));
            }
        };
        compiler.setIdentifierResolver(resolver);
        CompilationResult<JavaCodeFragment> result = compiler.compile(expression);
        if (result.failed()) {
            System.out.println(result);
        }
        assertTrue(result.successfull());
        assertEquals(expectedDatatype, result.getDatatype());

        Object value = processor.evaluate(expression, parameterNames, parameterValues);
        if (!ObjectUtils.equals(value, expectedValue)) {
            System.out.println(result);
        }
        assertEquals(expectedValue, value);
        return result;
    }

    /**
     * Compiles the given expression and tests if the compilation was successfull and if the
     * datatype is the expected one. After that the expression is executed and it is tested if it
     * returns the expected value.
     */
    protected CompilationResult<JavaCodeFragment> execAndTestSuccessfull(String expression, boolean expectedValue)
            throws Exception {

        CompilationResult<JavaCodeFragment> result = compiler.compile(expression);
        if (result.failed()) {
            System.out.println(result);
        }
        assertTrue(result.successfull());
        assertEquals(Datatype.PRIMITIVE_BOOLEAN, result.getDatatype());

        Object value = processor.evaluate(expression);
        if (!(value instanceof Boolean)) {
            System.out.println();
            assertTrue(result + " ist keine Boolean!", value instanceof Boolean);
        }
        assertEquals(expectedValue, ((Boolean)value).booleanValue());
        return result;
    }

    /**
     * Compiles the given expression and tests if the compilation failed and the compilation result
     * contains a message with the indicated message code.
     */
    protected CompilationResult<JavaCodeFragment> execAndTestFail(String expression, String expectedMessageCode)
            throws Exception {
        CompilationResult<JavaCodeFragment> result = compiler.compile(expression);
        assertTrue(result.failed());
        assertNotNull(result.getMessages().getMessageByCode(expectedMessageCode));
        return result;
    }

    protected BinaryOperation<JavaCodeFragment>[] toArray(BinaryOperation<JavaCodeFragment> binaryOperation) {
        @SuppressWarnings("unchecked")
        BinaryOperation<JavaCodeFragment>[] binaryOperations = new BinaryOperation[] { binaryOperation };
        return binaryOperations;
    }
}
