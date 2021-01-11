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

public class SqrtDecimalTest extends FunctionAbstractTest {

    @Test
    public void testCompile() throws Exception {
        registerFunction(new SqrtDecimal("SQRT", ""));
        execAndTestSuccessfull("SQRT(4.0)", Decimal.valueOf("2.0"), Datatype.DECIMAL);
        execAndTestSuccessfull("SQRT(25.0)", Decimal.valueOf("5.0"), Datatype.DECIMAL);
    }
}
