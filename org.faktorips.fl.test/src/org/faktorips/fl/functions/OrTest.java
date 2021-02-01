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

public class OrTest extends FunctionAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        registerFunction(new Or("OR", ""));
        getCompiler().setEnsureResultIsObject(false);
    }

    @Test
    public void testCompile() throws Exception {
        // test if multiple arguments work
        execAndTestSuccessfull("OR(true)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(true; true)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(true; true; true)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(true; true; false)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(false; false; false)", Boolean.FALSE, Datatype.PRIMITIVE_BOOLEAN);

        // test if the result of an expression as argument for the OR-function works
        execAndTestSuccessfull("OR(1=1)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(1!=1)", Boolean.FALSE, Datatype.PRIMITIVE_BOOLEAN);
    }
}
