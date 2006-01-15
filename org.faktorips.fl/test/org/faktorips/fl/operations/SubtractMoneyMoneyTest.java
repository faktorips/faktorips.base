package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.AbstractCompilerTest;
import org.faktorips.fl.BinaryOperation;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.values.Money;



/**
 *
 */
public class SubtractMoneyMoneyTest extends AbstractCompilerTest {
    
    protected void setUp() throws Exception {
        super.setUp();
        compiler.setBinaryOperations(new BinaryOperation[]{new SubtractMoneyMoney()});
    }
    
    public void testSuccessfull() throws Exception {
        execAndTestSuccessfull("10.12EUR - 8.10EUR", Money.valueOf("2.02EUR"), Datatype.MONEY);
        execAndTestSuccessfull("8.10EUR - 10.12EUR", Money.valueOf("-2.02EUR"), Datatype.MONEY);
    }
    
    public void testLhsError() throws Exception {
        execAndTestFail("a a - 8.10EUR", ExprCompiler.SYNTAX_ERROR);
    }
    
    public void testRhsError() throws Exception {
        execAndTestFail("8.10EUR - a a", ExprCompiler.SYNTAX_ERROR);
    }

}
