/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.JavaExprCompilerAbstractTest;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class EqualsPrimitiveTypePrimitiveTypeTest extends JavaExprCompilerAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        getCompiler().setEnsureResultIsObject(false);
    }

    @Test
    public void testSuccessfull_int() throws Exception {
        getCompiler().setBinaryOperations(toArray(new EqualsPrimtiveType(Datatype.PRIMITIVE_INT)));
        execAndTestSuccessfull("1=2", false);
        execAndTestSuccessfull("1=1", true);
    }

    @Test
    public void testSuccessfull_boolean() throws Exception {
        getCompiler().setBinaryOperations(toArray(new EqualsPrimtiveType(Datatype.PRIMITIVE_BOOLEAN)));
        execAndTestSuccessfull("true=true", true);
        execAndTestSuccessfull("false=true", false);
    }
}
