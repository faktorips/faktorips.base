package org.faktorips.fl;

import junit.framework.TestCase;

import org.faktorips.datatype.Datatype;


/**
 *
 */
public abstract class AbstractCompilerTest extends TestCase {
    
    protected ExprCompiler compiler;
    private ExprEvaluator processor;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        compiler = new ExprCompiler();        
        processor = new ExprEvaluator(compiler);
    }
    
    /**
     * Compiles the given expression and tests if the compilation was successfull
     * and if the datatype is the expected one. After that the expression is executed
     * and it is tested if it returns the expected value.
     */
    protected CompilationResult execAndTestSuccessfull(
            String expression,
            Object expectedValue,
            Datatype expectedDatatype) throws Exception {
        
        CompilationResult result = compiler.compile(expression);
        if (result.failed()) {
            System.out.println(result);
        }
        assertTrue(result.successfull());
        assertEquals(expectedDatatype, result.getDatatype());
        
        Object value = processor.evaluate(expression);
        if (!value.equals(expectedValue)) {
            System.out.println(result);
        }
        assertEquals(expectedValue, value);
        return result;
    }

    /**
     * Compiles the given expression and tests if the compilation failed and 
     * the compilation result contains a message with the indicated message code.
     */
    protected CompilationResult execAndTestFail(String expression, String expectedMessageCode) throws Exception {
        CompilationResult result = compiler.compile(expression);
        assertTrue(result.failed());
        assertNotNull(result.getMessages().getMessageByCode(expectedMessageCode));
        return result;
    }
}
