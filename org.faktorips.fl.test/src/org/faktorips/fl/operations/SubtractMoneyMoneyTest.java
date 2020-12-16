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
import org.faktorips.values.Money;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class SubtractMoneyMoneyTest extends JavaExprCompilerAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        getCompiler().setBinaryOperations(toArray(new SubtractMoneyMoney()));
    }

    @Test
    public void testSuccessfull() throws Exception {
        execAndTestSuccessfull("10.12EUR - 8.10EUR", Money.valueOf("2.02EUR"), Datatype.MONEY);
        execAndTestSuccessfull("8.10EUR - 10.12EUR", Money.valueOf("-2.02EUR"), Datatype.MONEY);
    }

    @Test
    public void testLhsError() throws Exception {
        execAndTestFail("a a - 8.10EUR", ExprCompiler.SYNTAX_ERROR);
    }

    @Test
    public void testRhsError() throws Exception {
        execAndTestFail("8.10EUR - a a", ExprCompiler.SYNTAX_ERROR);
    }

}
