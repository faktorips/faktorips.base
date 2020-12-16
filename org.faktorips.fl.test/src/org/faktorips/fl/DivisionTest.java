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
public class DivisionTest extends JavaExprCompilerAbstractTest {
    @Test
    public void testDecimalDecimal() throws Exception {
        execAndTestSuccessfull("10.8 / 4.2", Decimal.valueOf("2.5714285714"), Datatype.DECIMAL);
    }

    @Test
    public void testDecimalInt() throws Exception {
        execAndTestSuccessfull("10.8 / 4", Decimal.valueOf("2.7000000000"), Datatype.DECIMAL);
    }

    @Test
    public void testIntDecimal() throws Exception {
        execAndTestSuccessfull("10 / 2.5", Decimal.valueOf("4.0000000000"), Datatype.DECIMAL);
    }

    @Test
    public void testDecimalInteger() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("10.8 / WHOLENUMBER(4.1)", Decimal.valueOf("2.7000000000"), Datatype.DECIMAL);
    }

    @Test
    public void testIntegerDecimal() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(10.2) / 2.5", Decimal.valueOf("4.0000000000"), Datatype.DECIMAL);
    }

    @Test
    public void testIntInt() throws Exception {
        execAndTestSuccessfull("10 / 4", Decimal.valueOf("2.5000000000"), Datatype.DECIMAL);
    }

    @Test
    public void testIntInteger() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("10 / WHOLENUMBER(4)", Decimal.valueOf("2.5000000000"), Datatype.DECIMAL);
    }

    @Test
    public void testIntegerInt() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(10) / WHOLENUMBER(4)", Decimal.valueOf("2.5000000000"), Datatype.DECIMAL);
    }

    @Test
    public void testIntegerInteger() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(10) / WHOLENUMBER(4)", Decimal.valueOf("2.5000000000"), Datatype.DECIMAL);
    }

    @Test
    public void testMoneyDecimal() throws Exception {
        execAndTestSuccessfull("10.80EUR / 4.2", Money.valueOf("2.57EUR"), Datatype.MONEY);
    }

    @Test
    public void testMoneyInt() throws Exception {
        execAndTestSuccessfull("10.00EUR / 4", Money.valueOf("2.50EUR"), Datatype.MONEY);
    }

    @Test
    public void testMoneyInteger() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("10.00EUR / WHOLENUMBER(4)", Money.valueOf("2.50EUR"), Datatype.MONEY);
    }

}
