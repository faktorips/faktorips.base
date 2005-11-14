package org.faktorips.fl;

import java.util.Locale;

import org.faktorips.datatype.*;
import org.faktorips.fl.ExcelFunctionsResolver;


/**
 *
 */
public class MultiplicationTest extends AbstractCompilerTest {
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testDecimalDecimal() throws Exception {
        execAndTestSuccessfull("3.5 * 7.2", Decimal.valueOf("25.20"), Datatype.DECIMAL);
    }

    public void testDecimalInt() throws Exception {
        execAndTestSuccessfull("3.5 * 7", Decimal.valueOf("24.5"), Datatype.DECIMAL);
    }
    
    public void testIntDecimal() throws Exception {
        execAndTestSuccessfull("7 * 3.5", Decimal.valueOf("24.5"), Datatype.DECIMAL);
    }
    
    public void testDecimalInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("3.5 * WHOLENUMBER(7.1)", Decimal.valueOf("24.5"), Datatype.DECIMAL);
    }
    
    public void testIntegerDecimal() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7.1) * 3.5", Decimal.valueOf("24.5"), Datatype.DECIMAL);
    }
    
    public void testIntInt() throws Exception {
        execAndTestSuccessfull("7 * 3", new Integer(21), Datatype.INTEGER);
    }
    
    public void testIntInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("7 * WHOLENUMBER(3)", new Integer(21), Datatype.INTEGER);
    }
    
    public void testIntegerInt() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(3) * 7", new Integer(21), Datatype.INTEGER);
    }
    
    public void testIntegerInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(3) * WHOLENUMBER(7)", new Integer(21), Datatype.INTEGER);
    }
    
    public void testMoneyDecimal() throws Exception {
        execAndTestSuccessfull("3.50EUR * 7", Money.valueOf("24.50EUR"), Datatype.MONEY);
    }
    
    public void testDecimalMoney() throws Exception {
        execAndTestSuccessfull("7 * 3.50EUR", Money.valueOf("24.50EUR"), Datatype.MONEY);
    }
}
