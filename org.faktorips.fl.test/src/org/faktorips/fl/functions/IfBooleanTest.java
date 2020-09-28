/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions;

import org.faktorips.datatype.Datatype;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class IfBooleanTest extends FunctionAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        registerFunction(new IfBoolean("IF", ""));
    }

    @Test
    public void testNull() throws Exception {
        registerFunction(new BooleanFct("NULLOBJ", null));
        execAndTestSuccessfull("IF(NULLOBJ(); 2.1; 3.2)", Decimal.valueOf("3.2"), Datatype.DECIMAL);
    }

    @Test
    public void testBooleanCondition() throws Exception {
        registerFunction(new BooleanFct("TRUEOBJ", Boolean.TRUE));
        registerFunction(new BooleanFct("FALSEOBJ", Boolean.FALSE));
        execAndTestSuccessfull("IF(1=1; 2.1; 3.2)", Decimal.valueOf("2.1"), Datatype.DECIMAL);
        execAndTestSuccessfull("IF(TRUEOBJ(); 2; 3)", Integer.valueOf(2), Datatype.INTEGER);
        execAndTestSuccessfull("IF(FALSEOBJ(); 2; 3)", Integer.valueOf(3), Datatype.INTEGER);
    }

    @Test
    public void testDifferentDatatypes() throws Exception {
        execAndTestFail("IF(1=2; 10EUR; 1)", If.ERROR_MESSAGE_CODE);
    }

    @Test
    public void testDecimal() throws Exception {
        execAndTestSuccessfull("IF(1=1; 2.1; 3.2)", Decimal.valueOf("2.1"), Datatype.DECIMAL);
        execAndTestSuccessfull("IF(1=2; 2.1; 3.2)", Decimal.valueOf("3.2"), Datatype.DECIMAL);
    }

    @Test
    public void testDecimalInt() throws Exception {
        execAndTestSuccessfull("IF(1=1; 2.1; 3)", Decimal.valueOf("2.1"), Datatype.DECIMAL);
        execAndTestSuccessfull("IF(1=2; 2.1; 3)", Decimal.valueOf("3"), Datatype.DECIMAL);
    }

    @Test
    public void testIntDecimal() throws Exception {
        execAndTestSuccessfull("IF(1=1; 2; 3.1)", Decimal.valueOf("2"), Datatype.DECIMAL);
        execAndTestSuccessfull("IF(1=2; 2; 3.1)", Decimal.valueOf("3.1"), Datatype.DECIMAL);
    }

    @Test
    public void testInt() throws Exception {
        execAndTestSuccessfull("IF(1=1; 2; 3)", Integer.valueOf(2), Datatype.INTEGER);
        execAndTestSuccessfull("IF(1=2; 2; 3)", Integer.valueOf(3), Datatype.INTEGER);
    }

    @Test
    public void testInteger() throws Exception {
        execAndTestSuccessfull("IF(1=1; 2; 3)", Integer.valueOf(2), Datatype.INTEGER);
        execAndTestSuccessfull("IF(1=2; 2; 3)", Integer.valueOf(3), Datatype.INTEGER);
    }

    @Test
    public void testMoney() throws Exception {
        execAndTestSuccessfull("IF(1=1; 10EUR; 20EUR)", Money.euro(10, 0), Datatype.MONEY);
        execAndTestSuccessfull("IF(1=2; 10EUR; 20EUR)", Money.euro(20, 0), Datatype.MONEY);
    }

    @Test
    public void testString() throws Exception {
        execAndTestSuccessfull("IF(1=1; \"a\"; \"b\")", "a", Datatype.STRING);
        execAndTestSuccessfull("IF(1=2; \"a\"; \"b\")", "b", Datatype.STRING);
    }

    @Test
    public void testCombinations() throws Exception {
        execAndTestSuccessfull("IF(1=1; 2; 3) + 1", Integer.valueOf(3), Datatype.INTEGER);
        execAndTestSuccessfull("IF(1=1; 2.1 + 1; 3) + 2", Decimal.valueOf("5.1"), Datatype.DECIMAL);
        execAndTestSuccessfull("IF(1=1; 2; 3) + 10 + IF(1=2; 2; 3) ", Integer.valueOf(15), Datatype.INTEGER);
        execAndTestSuccessfull("IF(1=1; IF(1=2; 2; 30); 3) + 1", Integer.valueOf(31), Datatype.INTEGER);
    }
}
