package org.faktorips.fl.functions;

import org.faktorips.datatype.Decimal;
import org.faktorips.fl.CompilationResult;

/**
 *
 */
public class DecimalTestArrayFctTest extends AbstractFunctionTest {
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void test() throws Exception {
        DecimalTestArrayFct testFct = new DecimalTestArrayFct();
        testFct.setValues(new Decimal[]{Decimal.valueOf(10, 0), Decimal.valueOf(32, 0)});
        registerFunction(testFct);
        CompilationResult result = compiler.compile("DECIMALTESTARRAY()");
        if (result.failed()) {
            System.out.println(result);
        }
        assertTrue(result.successfull());
    }
    
    public void testNull() throws Exception {
        DecimalTestArrayFct testFct = new DecimalTestArrayFct();
        testFct.setValues(null);
        registerFunction(testFct);
        CompilationResult result = compiler.compile("DECIMALTESTARRAY()");
        if (result.failed()) {
            System.out.println(result);
        }
        assertTrue(result.successfull());
    }
    
    
}
