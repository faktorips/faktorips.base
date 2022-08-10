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
import org.faktorips.values.Decimal;
import org.junit.Test;

/**
 *
 */
public class AbsTest extends FunctionAbstractTest {

    @Test
    public void testRoundUp() throws Exception {
        registerFunction(new Abs("ABS", ""));
        execAndTestSuccessfull("ABS(3.25)", Decimal.valueOf("3.25"), Datatype.DECIMAL);
        execAndTestSuccessfull("ABS(-3.25)", Decimal.valueOf("3.25"), Datatype.DECIMAL);
    }

}
