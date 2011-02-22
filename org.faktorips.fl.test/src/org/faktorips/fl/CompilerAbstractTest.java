/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.Message;
import org.junit.Before;

/**
 *
 */
public abstract class CompilerAbstractTest {

    protected ExprCompiler compiler;
    private ExprEvaluator processor;

    @Before
    public void setUp() throws Exception {
        compiler = new ExprCompiler();
        Locale.setDefault(Locale.ENGLISH);
        compiler.setLocale(Locale.ENGLISH);
        processor = new ExprEvaluator(compiler);
    }

    /**
     * Compiles the given expression and tests if the compilation was successfull and if the
     * datatype is the expected one. After that the expression is executed and it is tested if it
     * returns the expected value.
     */
    protected CompilationResult execAndTestSuccessfull(String expression,
            Object expectedValue,
            Datatype expectedDatatype) throws Exception {

        CompilationResult result = compiler.compile(expression);
        if (result.failed()) {
            System.out.println(result);
        }
        assertTrue(result.successfull());
        assertEquals(expectedDatatype, result.getDatatype());

        Object value = processor.evaluate(expression);
        if (!ObjectUtils.equals(value, expectedValue)) {
            System.out.println(result);
        }
        assertEquals(expectedValue, value);
        return result;
    }

    protected CompilationResult execAndTestSuccessfull(String expression,
            Object expectedValue,
            String[] parameterNames,
            Datatype[] parameterTypes,
            Object[] parameterValues,
            Datatype expectedDatatype) throws Exception {

        final Map<String, Datatype> parameterMap = new HashMap<String, Datatype>();
        for (int i = 0; i < parameterNames.length; i++) {
            parameterMap.put(parameterNames[i], parameterTypes[i]);
        }
        IdentifierResolver resolver = new IdentifierResolver() {
            public CompilationResult compile(String identifier, ExprCompiler exprCompiler, Locale locale) {
                Object paramDatatype = parameterMap.get(identifier);
                if (paramDatatype != null) {
                    return new CompilationResultImpl(identifier, (Datatype)paramDatatype);
                }
                return new CompilationResultImpl(new Message("",
                        "The parameter " + identifier + " cannot be resolved.", Message.ERROR));
            }
        };
        compiler.setIdentifierResolver(resolver);
        CompilationResult result = compiler.compile(expression);
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
    protected CompilationResult execAndTestSuccessfull(String expression, boolean expectedValue) throws Exception {

        CompilationResult result = compiler.compile(expression);
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
    protected CompilationResult execAndTestFail(String expression, String expectedMessageCode) throws Exception {
        CompilationResult result = compiler.compile(expression);
        assertTrue(result.failed());
        assertNotNull(result.getMessages().getMessageByCode(expectedMessageCode));
        return result;
    }
}
