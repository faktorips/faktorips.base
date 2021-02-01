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

public class MinMaxDecimalTest extends FunctionAbstractTest {
    @Test
    public void testCompile() throws Exception {
        registerFunction(new MinMaxDecimal("MAX", "", true));
        execAndTestSuccessfull("MAX(3.0; 4.0)", Decimal.valueOf("4.0"), Datatype.DECIMAL);
        execAndTestSuccessfull("MAX(4.0; 3.0)", Decimal.valueOf("4.0"), Datatype.DECIMAL);
        execAndTestSuccessfull("MAX(4; 3)", Decimal.valueOf("4"), Datatype.DECIMAL);

        registerFunction(new MinMaxDecimal("MIN", "", false));
        execAndTestSuccessfull("MIN(3.0; 4.0)", Decimal.valueOf("3.0"), Datatype.DECIMAL);
        execAndTestSuccessfull("MIN(4.0; 3.0)", Decimal.valueOf("3.0"), Datatype.DECIMAL);
        execAndTestSuccessfull("MIN(4; 3)", Decimal.valueOf("3"), Datatype.DECIMAL);
    }

}
