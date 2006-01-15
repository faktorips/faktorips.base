package org.faktorips.fl;

import junit.framework.TestCase;

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

}
