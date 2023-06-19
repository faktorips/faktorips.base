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
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class IsEmptyTest extends FunctionAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        getCompiler().setEnsureResultIsObject(false);
        registerFunction(new IsEmpty("ISEMPTY", ""));
    }

    @Test
    public void testDecimal() throws Exception {
        execAndTestSuccessfull("ISEMPTY(1.0)", false);
        registerFunction(new DecimalNullFct());
        execAndTestSuccessfull("ISEMPTY(DECIMALNULL())", true);
    }

    @Test
    public void testMoney() throws Exception {
        execAndTestSuccessfull("ISEMPTY(10EUR)", false);
        registerFunction(new MoneyNullFct());
        execAndTestSuccessfull("ISEMPTY(MONEYNULL())", true);
    }

    @Test
    public void testString() throws Exception {
        execAndTestSuccessfull("ISEMPTY(\"a\")", false);
    }

    @Test
    public void testInt() throws Exception {
        execAndTestSuccessfull("ISEMPTY(1)", false);
    }

    @Test
    public void testBoolean() throws Exception {
        registerFunction(new BooleanFct("TRUEOBJ", Boolean.TRUE));
        registerFunction(new BooleanFct("BOOLEANNULL", null));
        execAndTestSuccessfull("ISEMPTY(TRUEOBJ())", false);
        execAndTestSuccessfull("ISEMPTY(BOOLEANNULL())", true);
    }

    @Test
    public void testPrimitiveBoolean() throws Exception {
        execAndTestSuccessfull("ISEMPTY(true)", false);
    }

    @Test
    public void testParameter() throws Exception {
        execAndTestSuccessfull("ISEMPTY(param)", true, new String[] { "param" }, new Datatype[] { Datatype.STRING },
                new Object[] { null }, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("ISEMPTY(param)", false, new String[] { "param" }, new Datatype[] { Datatype.STRING },
                new Object[] { "Foo" }, Datatype.PRIMITIVE_BOOLEAN);
    }

}
