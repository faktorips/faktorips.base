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

package org.faktorips.fl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.junit.Test;

/**
 * Contains all tests for the unary minus (+) operator that as defined by the default unary
 * operations.
 */
public class MinusTest extends JavaExprCompilerAbstractTest {
    @Test
    public void testDecimal() throws Exception {
        execAndTestSuccessfull("- 10.23", Decimal.valueOf(-1023, 2), Datatype.DECIMAL);
    }

    @Test
    public void testInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("- WHOLENUMBER(42.2)", new Integer(-42), Datatype.INTEGER);
    }

    @Test
    public void testInt() throws Exception {
        compiler.setEnsureResultIsObject(false);
        CompilationResult<JavaCodeFragment> result = compiler.compile("- 42");
        assertTrue(result.successfull());
        assertEquals(Datatype.PRIMITIVE_INT, result.getDatatype());
        assertEquals("-42", result.getCodeFragment().getSourcecode());
    }

    @Test
    public void testMoney() throws Exception {
        execAndTestSuccessfull("- 10.12EUR", Money.valueOf("-10.12EUR"), Datatype.MONEY);
    }

}
