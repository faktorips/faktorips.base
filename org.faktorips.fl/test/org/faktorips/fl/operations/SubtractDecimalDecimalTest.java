package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.AbstractCompilerTest;
import org.faktorips.fl.BinaryOperation;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.values.Decimal;



/**
 *
 */
public class SubtractDecimalDecimalTest extends AbstractCompilerTest {
    
    protected void setUp() throws Exception {
        super.setUp();
        compiler.setBinaryOperations(new BinaryOperation[]{new SubtractDecimalDecimal()});
    }
    
    public void testSuccessfull() throws Exception {
        execAndTestSuccessfull("10.123 - 8.1", Decimal.valueOf("2.023"), Datatype.DECIMAL);
        execAndTestSuccessfull("8.1 - 10.123", Decimal.valueOf("-2.023"), Datatype.DECIMAL);
    }
    
    public void testLhsError() throws Exception {
        execAndTestFail("a a - 8.1", ExprCompiler.SYNTAX_ERROR);
    }
    
    public void testRhsError() throws Exception {
        execAndTestFail("8.1 - a a", ExprCompiler.SYNTAX_ERROR);
    }

}
