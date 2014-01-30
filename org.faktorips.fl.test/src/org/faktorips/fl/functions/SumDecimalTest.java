/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions;

import org.faktorips.datatype.Datatype;
import org.faktorips.values.Decimal;
import org.junit.Before;
import org.junit.Test;

/**
 */
public class SumDecimalTest extends FunctionAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void test() throws Exception {
        registerFunction(new SumDecimal("SUM", ""));
        DecimalTestArrayFct testFct = new DecimalTestArrayFct();
        testFct.setValues(new Decimal[] { Decimal.valueOf(10, 0), Decimal.valueOf(32, 0) });
        registerFunction(testFct);
        execAndTestSuccessfull("SUM(DECIMALTESTARRAY())", Decimal.valueOf("42"), Datatype.DECIMAL);
    }

}
