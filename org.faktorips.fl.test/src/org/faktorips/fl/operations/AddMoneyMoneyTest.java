/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
public class AddMoneyMoneyTest extends JavaExprCompilerAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        compiler.setBinaryOperations(toArray(new AddMoneyMoney()));
    }

    @Test
    public void test() throws Exception {
        execAndTestSuccessfull("3.50EUR + 7.45EUR", Money.valueOf("10.95EUR"), Datatype.MONEY);
    }

    @Test
    public void testLhsError() throws Exception {
        execAndTestFail("a a + 8.30EUR", ExprCompiler.SYNTAX_ERROR);
    }

    @Test
    public void testRhsError() throws Exception {
        execAndTestFail("8.10EUR + a a", ExprCompiler.SYNTAX_ERROR);
    }

}
