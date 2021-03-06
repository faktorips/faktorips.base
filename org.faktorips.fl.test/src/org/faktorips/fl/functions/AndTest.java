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

public class AndTest extends FunctionAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        registerFunction(new And("AND", ""));
        getCompiler().setEnsureResultIsObject(false);
    }

    @Test
    public void testCompile() throws Exception {
        // test if multiple arguments work
        execAndTestSuccessfull("AND(true)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("AND(true; true)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("AND(true; true; true)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("AND(true; true; false)", Boolean.FALSE, Datatype.PRIMITIVE_BOOLEAN);

        // test if the result of an expression as argument for the OR-function works
        execAndTestSuccessfull("AND(1=1)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("AND(1!=1)", Boolean.FALSE, Datatype.PRIMITIVE_BOOLEAN);
    }
}
