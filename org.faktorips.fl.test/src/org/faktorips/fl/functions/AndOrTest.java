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

public class AndOrTest extends FunctionAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        registerFunction(new And("AND", ""));
        registerFunction(new Or("OR", ""));
        getCompiler().setEnsureResultIsObject(false);
    }

    @Test
    public void testCompile() throws Exception {
        execAndTestSuccessfull("AND(true; OR(true; false))", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("AND(true; OR(false; true))", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("AND(false; OR(true; false))", Boolean.FALSE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("AND(false; OR(false; true))", Boolean.FALSE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(true; AND(true; false))", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(true; AND(false; true))", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(false; AND(true; false))", Boolean.FALSE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(false; AND(true; true))", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
    }
}
