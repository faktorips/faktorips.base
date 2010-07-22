/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.fl;

import junit.framework.TestCase;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.values.Decimal;

/**
 *
 */
public class ExprEvaluatorTest extends TestCase {

    public void testExecute() throws Exception {
        ExprEvaluator processor = new ExprEvaluator(new ExprCompiler());
        Object o = processor.evaluate("10.123");
        assertEquals(Decimal.valueOf("10.123"), o);
    }

    public void testExecuteWithVariables() throws Exception {
        ExprCompiler compiler = new ExprCompiler();
        DefaultIdentifierResolver resolver = new DefaultIdentifierResolver();
        resolver.register("a", new JavaCodeFragment("new Integer(42)"), Datatype.INTEGER);
        compiler.setIdentifierResolver(resolver);
        ExprEvaluator processor = new ExprEvaluator(compiler);
        Object o = processor.evaluate("a * 2");

        assertEquals(new Integer(84), o);
    }

    public void testExecuteWithEnum() throws Exception {
        System.setProperty("debug", "true");
        System.setProperty("trace", "true");

        ExprCompiler compiler = new ExprCompiler();
        compiler.setIdentifierResolver(new TestEnumIdentifierResolver());
        String expression = "TestEnum.MONTH";
        ExprEvaluator processor = new ExprEvaluator(compiler);
        Object value = processor.evaluate(expression);
        assertEquals(TestEnum.MONTH, value);
    }
}
