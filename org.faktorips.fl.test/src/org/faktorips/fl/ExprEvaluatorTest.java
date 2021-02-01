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

import static org.junit.Assert.assertEquals;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.values.Decimal;
import org.junit.Test;

/**
 *
 */
public class ExprEvaluatorTest {
    @Test
    public void testExecute() throws Exception {
        ExprEvaluator processor = new ExprEvaluator(new JavaExprCompiler());
        Object o = processor.evaluate("10.123");
        assertEquals(Decimal.valueOf("10.123"), o);
    }

    @Test
    public void testExecuteWithVariables() throws Exception {
        JavaExprCompiler compiler = new JavaExprCompiler();
        DefaultIdentifierResolver resolver = new DefaultIdentifierResolver();
        resolver.register("a", new JavaCodeFragment("Integer.valueOf(42)"), Datatype.INTEGER);
        compiler.setIdentifierResolver(resolver);
        ExprEvaluator processor = new ExprEvaluator(compiler);
        Object o = processor.evaluate("a * 2");

        assertEquals(Integer.valueOf(84), o);
    }

    @Test
    public void testExecuteWithEnum() throws Exception {
        System.setProperty("debug", "true");
        System.setProperty("trace", "true");

        JavaExprCompiler compiler = new JavaExprCompiler();
        compiler.setIdentifierResolver(new TestEnumIdentifierResolver());
        String expression = "TestEnum.MONTH";
        ExprEvaluator processor = new ExprEvaluator(compiler);
        Object value = processor.evaluate(expression);
        assertEquals(TestEnum.MONTH, value);
    }
}
