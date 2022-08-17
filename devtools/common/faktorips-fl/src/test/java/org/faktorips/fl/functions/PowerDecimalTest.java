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

public class PowerDecimalTest extends FunctionAbstractTest {

    @Test
    public void testCompile() throws Exception {
        registerFunction(new PowerDecimal("POWER", ""));
        execAndTestSuccessfull("POWER(2.0; 3.0)", Decimal.valueOf("8.0"), Datatype.DECIMAL);
        execAndTestSuccessfull("POWER(4.0; 2.0)", Decimal.valueOf("16.0"), Datatype.DECIMAL);
    }
}
