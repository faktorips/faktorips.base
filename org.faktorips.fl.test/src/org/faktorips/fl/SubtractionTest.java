/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
public class SubtractionTest extends JavaExprCompilerAbstractTest {
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
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("3.5 - WHOLENUMBER(2)", Decimal.valueOf("1.5"), Datatype.DECIMAL);
    }

    @Test
    public void testIntegerDecimal() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7.1) - 3.2", Decimal.valueOf("3.8"), Datatype.DECIMAL);
    }

    @Test
    public void testIntInt() throws Exception {
        execAndTestSuccessfull("10 - 3", Integer.valueOf(7), Datatype.INTEGER);
    }

    @Test
    public void testIntInteger() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("10 - WHOLENUMBER(3)", Integer.valueOf(7), Datatype.INTEGER);
    }

    @Test
    public void testIntegerInt() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(10) - 3", Integer.valueOf(7), Datatype.INTEGER);
    }

    @Test
    public void testIntegerInteger() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(10) - WHOLENUMBER(3)", Integer.valueOf(7), Datatype.INTEGER);
    }

    @Test
    public void testMoneyMoney() throws Exception {
        execAndTestSuccessfull("3.50EUR - 2.40EUR", Money.valueOf("1.10EUR"), Datatype.MONEY);
    }

}
