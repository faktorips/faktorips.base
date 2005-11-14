package org.faktorips.fl;

import junit.framework.TestCase;

import org.faktorips.datatype.Decimal;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.ExprEvaluator;


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
