/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.fl;

import java.util.Locale;

import org.faktorips.datatype.Datatype;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.junit.Test;

/**
 *
 */
public class SubtractionTest extends CompilerAbstractTest {
    @Test
    public void testDecimalDecimal() throws Exception {
        execAndTestSuccessfull("7.55 - 3.4", Decimal.valueOf("4.15"), Datatype.DECIMAL);
    }

    @Test
    public void testDecimalInt() throws Exception {
        execAndTestSuccessfull("7.55 - 3", Decimal.valueOf("4.55"), Datatype.DECIMAL);
    }

    @Test
    public void testIntDecimal() throws Exception {
        execAndTestSuccessfull("7 - 2.5", Decimal.valueOf("4.5"), Datatype.DECIMAL);
    }

    @Test
    public void testDecimalInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("3.5 - WHOLENUMBER(2)", Decimal.valueOf("1.5"), Datatype.DECIMAL);
    }

    @Test
    public void testIntegerDecimal() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7.1) - 3.2", Decimal.valueOf("3.8"), Datatype.DECIMAL);
    }

    @Test
    public void testIntInt() throws Exception {
        execAndTestSuccessfull("10 - 3", new Integer(7), Datatype.INTEGER);
    }

    @Test
    public void testIntInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("10 - WHOLENUMBER(3)", new Integer(7), Datatype.INTEGER);
    }

    @Test
    public void testIntegerInt() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(10) - 3", new Integer(7), Datatype.INTEGER);
    }

    @Test
    public void testIntegerInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(10) - WHOLENUMBER(3)", new Integer(7), Datatype.INTEGER);
    }

    @Test
    public void testMoneyMoney() throws Exception {
        execAndTestSuccessfull("3.50EUR - 2.40EUR", Money.valueOf("1.10EUR"), Datatype.MONEY);
    }

}
