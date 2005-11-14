package org.faktorips.fl.functions;

import java.math.BigDecimal;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.Decimal;

/**
 *
 */
public class RoundTest extends AbstractFunctionTest {
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    
    public void testRoundUp() throws Exception {
        registerFunction(new Round("ROUNDUP", "", BigDecimal.ROUND_UP));
        execAndTestSuccessfull("ROUNDUP(3.25; 1)", Decimal.valueOf("3.3"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUNDUP(3.21; 1)", Decimal.valueOf("3.3"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUNDUP(-3.21; 1)", Decimal.valueOf("-3.3"), Datatype.DECIMAL);
    }
    
    public void testRoundDown() throws Exception {
        registerFunction(new Round("ROUNDDOWN", "", BigDecimal.ROUND_DOWN));
        execAndTestSuccessfull("ROUNDDOWN(3.25; 1)", Decimal.valueOf("3.2"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUNDDOWN(3.21; 1)", Decimal.valueOf("3.2"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUNDDOWN(-3.21; 1)", Decimal.valueOf("-3.2"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUNDDOWN(-3.29; 1)", Decimal.valueOf("-3.2"), Datatype.DECIMAL);
    }
    
    public void testRoundHalfUp() throws Exception {
        registerFunction(new Round("ROUND", "", BigDecimal.ROUND_HALF_UP));
        execAndTestSuccessfull("ROUND(3.25; 1)", Decimal.valueOf("3.3"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUND(3.249; 1)", Decimal.valueOf("3.2"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUND(-3.21; 1)", Decimal.valueOf("-3.2"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUND(-3.29; 1)", Decimal.valueOf("-3.3"), Datatype.DECIMAL);
    }
}
