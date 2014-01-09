/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("3.5 * WHOLENUMBER(7.1)", Decimal.valueOf("24.5"), Datatype.DECIMAL);
    }

    @Test
    public void testIntegerDecimal() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7.1) * 3.5", Decimal.valueOf("24.5"), Datatype.DECIMAL);
    }

    @Test
    public void testIntInt() throws Exception {
        execAndTestSuccessfull("7 * 3", new Integer(21), Datatype.INTEGER);
        execAndTestSuccessfull("7*3", new Integer(21), Datatype.INTEGER);
    }

    @Test
    public void testIntInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("7 * WHOLENUMBER(3)", new Integer(21), Datatype.INTEGER);
    }

    @Test
    public void testIntegerInt() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(3) * 7", new Integer(21), Datatype.INTEGER);
    }

    @Test
    public void testIntegerInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(3) * WHOLENUMBER(7)", new Integer(21), Datatype.INTEGER);
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
