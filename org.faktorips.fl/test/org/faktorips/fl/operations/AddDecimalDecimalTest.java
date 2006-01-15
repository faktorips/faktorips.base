package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.AbstractCompilerTest;
import org.faktorips.fl.BinaryOperation;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.values.Decimal;



/**
 *
 */
public class AddDecimalDecimalTest extends AbstractCompilerTest {
    
    protected void setUp() throws Exception {
        super.setUp();
        compiler.setBinaryOperations(new BinaryOperation[]{new AddDecimalDecimal()});
    }
    
    public void testSuccessfull() throws Exception {
        execAndTestSuccessfull("3.5 + 7.45", Decimal.valueOf("10.95"), Datatype.DECIMAL);
    }

    public void testLhsError() throws Exception {
        execAndTestFail("a a + 8.1", ExprCompiler.SYNTAX_ERROR);
    }
    
    public void testRhsError() throws Exception {
        execAndTestFail("8.1 + a a", ExprCompiler.SYNTAX_ERROR);
    }
    
}
