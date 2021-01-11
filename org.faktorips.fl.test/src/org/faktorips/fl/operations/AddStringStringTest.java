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
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.JavaExprCompilerAbstractTest;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 */
public class AddStringStringTest extends JavaExprCompilerAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        getCompiler().setBinaryOperations(toArray(new AddStringString()));
    }

    @Test
    public void test() throws Exception {
        execAndTestSuccessfull("\"a\" + \"b\"", "ab", Datatype.STRING);
    }

    @Test
    public void testLhsError() throws Exception {
        execAndTestFail("a a + \"b\"", ExprCompiler.SYNTAX_ERROR);
    }

    @Test
    public void testRhsError() throws Exception {
        execAndTestFail("\"b\" + a a", ExprCompiler.SYNTAX_ERROR);
    }

}
