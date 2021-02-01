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
import org.junit.Test;

/**
 *
 */
public class LessThanTest extends JavaExprCompilerAbstractTest {
    @Test
    public void testDecimalDecimal() throws Exception {
        execAndTestSuccessfull("3.45 < 7.45", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.45 < 7.45", Boolean.FALSE, Datatype.BOOLEAN);
    }

    @Test
    public void testDecimalInt() throws Exception {
        execAndTestSuccessfull("6.9 < 7", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.0 < 7", Boolean.FALSE, Datatype.BOOLEAN);
    }

    @Test
    public void testIntDecimal() throws Exception {
        execAndTestSuccessfull("6 < 6.5", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 < 7.0", Boolean.FALSE, Datatype.BOOLEAN);
    }

    @Test
    public void testDecimalInteger() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("6.9 < WHOLENUMBER(7.1)", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.0 < WHOLENUMBER(7.1)", Boolean.FALSE, Datatype.BOOLEAN);
    }

    @Test
    public void testIntegerDecimal() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7.1) < 7.1", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7.1) < 7.0", Boolean.FALSE, Datatype.BOOLEAN);
    }

    @Test
    public void testIntInt() throws Exception {
        execAndTestSuccessfull("7 < 8", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 < 7", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 < 6", Boolean.FALSE, Datatype.BOOLEAN);
    }

    @Test
    public void testIntInteger() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("7 < WHOLENUMBER(8)", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 < WHOLENUMBER(7)", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 < WHOLENUMBER(6)", Boolean.FALSE, Datatype.BOOLEAN);
    }

    @Test
    public void testIntegerInt() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7) < 8", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7) < 7", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7) < 6", Boolean.FALSE, Datatype.BOOLEAN);
    }

    @Test
    public void testIntegerInteger() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7) < WHOLENUMBER(8)", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7) < WHOLENUMBER(7)", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7) < WHOLENUMBER(6)", Boolean.FALSE, Datatype.BOOLEAN);
    }

    @Test
    public void testMoneyMoney() throws Exception {
        execAndTestSuccessfull("3.50EUR < 4.40EUR", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("3.50EUR < 3.50EUR", Boolean.FALSE, Datatype.BOOLEAN);
    }

}
