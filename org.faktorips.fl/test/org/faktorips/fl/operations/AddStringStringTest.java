package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.AbstractCompilerTest;
import org.faktorips.fl.BinaryOperation;
import org.faktorips.fl.ExprCompiler;



/**
 * 
 */
public class AddStringStringTest extends AbstractCompilerTest {
    
    protected void setUp() throws Exception {
        super.setUp();
        compiler.setBinaryOperations(new BinaryOperation[]{new AddStringString()});
    }
    
    public void test() throws Exception {
        execAndTestSuccessfull("\"a\" + \"b\"", "ab", Datatype.STRING);
    }

    public void testLhsError() throws Exception {
        execAndTestFail("a a + \"b\"", ExprCompiler.SYNTAX_ERROR);
    }
    
    public void testRhsError() throws Exception {
        execAndTestFail("\"b\" + a a", ExprCompiler.SYNTAX_ERROR);
    }
    
}
