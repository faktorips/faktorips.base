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
public class MultiplicationTest extends JavaExprCompilerAbstractTest {

    @Test
    public void testDecimalDecimal() throws Exception {
        execAndTestSuccessfull("3.5 * 7.2", Decimal.valueOf("25.20"), Datatype.DECIMAL);
    }

    @Test
    public void testDecimalInt() throws Exception {
        execAndTestSuccessfull("3.5 * 7", Decimal.valueOf("24.5"), Datatype.DECIMAL);
    }

    @Test
    public void testIntDecimal() throws Exception {
        execAndTestSuccessfull("7 * 3.5", Decimal.valueOf("24.5"), Datatype.DECIMAL);
    }

    @Test
    public void testDecimalInteger() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("3.5 * WHOLENUMBER(7.1)", Decimal.valueOf("24.5"), Datatype.DECIMAL);
    }

    @Test
    public void testIntegerDecimal() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7.1) * 3.5", Decimal.valueOf("24.5"), Datatype.DECIMAL);
    }

    @Test
    public void testIntInt() throws Exception {
        execAndTestSuccessfull("7 * 3", Integer.valueOf(21), Datatype.INTEGER);
        execAndTestSuccessfull("7*3", Integer.valueOf(21), Datatype.INTEGER);
    }

    @Test
    public void testIntInteger() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("7 * WHOLENUMBER(3)", Integer.valueOf(21), Datatype.INTEGER);
    }

    @Test
    public void testIntegerInt() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(3) * 7", Integer.valueOf(21), Datatype.INTEGER);
    }

    @Test
    public void testIntegerInteger() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(3) * WHOLENUMBER(7)", Integer.valueOf(21), Datatype.INTEGER);
    }

    @Test
    public void testMoneyDecimal() throws Exception {
        execAndTestSuccessfull("3.50EUR * 7", Money.valueOf("24.50EUR"), Datatype.MONEY);
    }

    @Test
    public void testDecimalMoney() throws Exception {
        execAndTestSuccessfull("7 * 3.50EUR", Money.valueOf("24.50EUR"), Datatype.MONEY);
    }
}
