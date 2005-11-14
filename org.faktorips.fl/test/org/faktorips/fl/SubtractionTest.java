package org.faktorips.fl;

import java.util.Locale;

import org.faktorips.datatype.*;
import org.faktorips.fl.ExcelFunctionsResolver;


/**
 *
 */
public class SubtractionTest extends AbstractCompilerTest {
    
    public void testDecimalDecimal() throws Exception {
        execAndTestSuccessfull("7.55 - 3.4", Decimal.valueOf("4.15"), Datatype.DECIMAL);
    }

    public void testDecimalInt() throws Exception {
        execAndTestSuccessfull("7.55 - 3", Decimal.valueOf("4.55"), Datatype.DECIMAL);
    }
    
    public void testIntDecimal() throws Exception {
        execAndTestSuccessfull("7 - 2.5", Decimal.valueOf("4.5"), Datatype.DECIMAL);
    }
    
    public void testDecimalInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("3.5 - WHOLENUMBER(2)", Decimal.valueOf("1.5"), Datatype.DECIMAL);
    }
    
    public void testIntegerDecimal() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7.1) - 3.2", Decimal.valueOf("3.8"), Datatype.DECIMAL);
    }
    
    public void testIntInt() throws Exception {
        execAndTestSuccessfull("10 - 3", new Integer(7), Datatype.INTEGER);
    }
    
    public void testIntInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("10 - WHOLENUMBER(3)", new Integer(7), Datatype.INTEGER);
    }
    
    public void testIntegerInt() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(10) - 3", new Integer(7), Datatype.INTEGER);
    }
    
    public void testIntegerInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(10) - WHOLENUMBER(3)", new Integer(7), Datatype.INTEGER);
    }
    
    public void testMoneyMoney() throws Exception {
        execAndTestSuccessfull("3.50EUR - 2.40EUR", Money.valueOf("1.10EUR"), Datatype.MONEY);
    }
    
}
