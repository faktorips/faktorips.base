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
import org.faktorips.values.Decimal;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class AddDecimalDecimalTest extends JavaExprCompilerAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        getCompiler().setBinaryOperations(toArray(new AddDecimalDecimal()));
    }

    @Test
    public void testSuccessfull() throws Exception {
        execAndTestSuccessfull("3.5 + 7.45", Decimal.valueOf("10.95"), Datatype.DECIMAL);
    }

    @Test
    public void testLhsError() throws Exception {
        execAndTestFail("a a + 8.1", ExprCompiler.SYNTAX_ERROR);
    }

    @Test
    public void testRhsError() throws Exception {
        execAndTestFail("8.1 + a a", ExprCompiler.SYNTAX_ERROR);
    }

}
