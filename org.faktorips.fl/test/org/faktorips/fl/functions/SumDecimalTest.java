package org.faktorips.fl.functions;

import org.faktorips.datatype.Datatype;
import org.faktorips.values.Decimal;

/**
 */
public class SumDecimalTest extends FunctionAbstractTest {
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void test() throws Exception {
        registerFunction(new SumDecimal("SUM", ""));
        DecimalTestArrayFct testFct = new DecimalTestArrayFct();
        testFct.setValues(new Decimal[]{Decimal.valueOf(10, 0), Decimal.valueOf(32, 0)});
        registerFunction(testFct);
        execAndTestSuccessfull("SUM(DECIMALTESTARRAY())", Decimal.valueOf("42"), Datatype.DECIMAL);
    }
    
}
