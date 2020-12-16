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
public class GreaterDecimalDecimalTest extends JavaExprCompilerAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        getCompiler().setBinaryOperations(toArray(new GreaterThanDecimalDecimal()));
    }

    @Test
    public void testSuccessfull() throws Exception {
        execAndTestSuccessfull("3.5 > 3.4", Boolean.TRUE, Datatype.BOOLEAN);
    }

    @Test
    public void testLhsError() throws Exception {
        execAndTestFail("a a > 3.5", ExprCompiler.SYNTAX_ERROR);
    }

    @Test
    public void testRhsError() throws Exception {
        execAndTestFail("3 > a a", ExprCompiler.SYNTAX_ERROR);
    }

}
