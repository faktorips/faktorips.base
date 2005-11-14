package org.faktorips.fl.functions;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.Decimal;

/**
 *
 */
public class AbsTest extends AbstractFunctionTest {
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testRoundUp() throws Exception {
        registerFunction(new Abs("ABS", ""));
        execAndTestSuccessfull("ABS(3.25)", Decimal.valueOf("3.25"), Datatype.DECIMAL);
        execAndTestSuccessfull("ABS(-3.25)", Decimal.valueOf("3.25"), Datatype.DECIMAL);
    }
    
}
