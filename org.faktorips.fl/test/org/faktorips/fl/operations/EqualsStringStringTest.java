package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.AbstractCompilerTest;
import org.faktorips.fl.BinaryOperation;
import org.faktorips.fl.ExprCompiler;



/**
 *
 */
public class EqualsStringStringTest extends AbstractCompilerTest {
    
    protected void setUp() throws Exception {
        super.setUp();
        compiler.setBinaryOperations(new BinaryOperation[]{new EqualsStringString()});
    }
    
    public void testSuccessfull() throws Exception {
        execAndTestSuccessfull("\"abc\"=\"abc\"", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("\"abc\"=\"xyz\"", Boolean.FALSE, Datatype.BOOLEAN);
    }

    public void testLhsError() throws Exception {
        execAndTestFail("\"a = \"a", ExprCompiler.SYNTAX_ERROR);
    }
    
    public void testRhsError() throws Exception {
        execAndTestFail("\"a\" = \"a", ExprCompiler.SYNTAX_ERROR);
    }
    
}
