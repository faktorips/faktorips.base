package org.faktorips.fl;

import java.util.Locale;

import org.faktorips.datatype.*;
import org.faktorips.fl.ExcelFunctionsResolver;


/**
 *
 */
public class AdditionTest extends AbstractCompilerTest {
    
    public void testDecimalDecimal() throws Exception {
        execAndTestSuccessfull("3.5 + 7.45", Decimal.valueOf("10.95"), Datatype.DECIMAL);
    }

    public void testDecimalInt() throws Exception {
        execAndTestSuccessfull("3.5 + 7", Decimal.valueOf("10.5"), Datatype.DECIMAL);
    }
    
    public void testIntDecimal() throws Exception {
        execAndTestSuccessfull("7 + 3.5", Decimal.valueOf("10.5"), Datatype.DECIMAL);
    }
    
    public void testDecimalInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("3.5 + WHOLENUMBER(7.1)", Decimal.valueOf("10.5"), Datatype.DECIMAL);
    }
    
    public void testIntegerDecimal() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7.1) + 3.5", Decimal.valueOf("10.5"), Datatype.DECIMAL);
    }
    
    public void testIntInt() throws Exception {
        execAndTestSuccessfull("7 + 3", new Integer(10), Datatype.INTEGER);
    }
    
    public void testIntInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("7 + WHOLENUMBER(3)", new Integer(10), Datatype.INTEGER);
    }

    public void testIntegerInt() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(3) + 7", new Integer(10), Datatype.INTEGER);
    }
    
    public void testIntegerInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7) + WHOLENUMBER(3)", new Integer(10), Datatype.INTEGER);
    }
    
    public void testMoneyMoney() throws Exception {
        execAndTestSuccessfull("3.50EUR + 2.40EUR", Money.valueOf("5.90EUR"), Datatype.MONEY);
    }
    
    public void testStringString() throws Exception {
        execAndTestSuccessfull("\"Hello \" + \"world!\"", "Hello world!", Datatype.STRING);
    }
    
    
}
