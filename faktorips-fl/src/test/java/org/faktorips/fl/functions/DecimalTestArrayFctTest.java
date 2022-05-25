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

import static org.junit.Assert.assertTrue;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.fl.CompilationResult;
import org.faktorips.values.Decimal;
import org.junit.Test;

/**
 *
 */
public class DecimalTestArrayFctTest extends FunctionAbstractTest {

    @Test
    public void test() throws Exception {
        DecimalTestArrayFct testFct = new DecimalTestArrayFct();
        testFct.setValues(new Decimal[] { Decimal.valueOf(10, 0), Decimal.valueOf(32, 0) });
        registerFunction(testFct);
        CompilationResult<JavaCodeFragment> result = getCompiler().compile("DECIMALTESTARRAY()");
        if (result.failed()) {
            System.out.println(result);
        }
        assertTrue(result.successfull());
    }

    @Test
    public void testNull() throws Exception {
        DecimalTestArrayFct testFct = new DecimalTestArrayFct();
        testFct.setValues(null);
        registerFunction(testFct);
        CompilationResult<JavaCodeFragment> result = getCompiler().compile("DECIMALTESTARRAY()");
        if (result.failed()) {
            System.out.println(result);
        }
        assertTrue(result.successfull());
    }

}
