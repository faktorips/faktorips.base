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
import org.junit.Test;

public class PowerIntTest extends FunctionAbstractTest {

    @Test
    public void testCompile() throws Exception {
        registerFunction(new PowerInt("POWER", ""));
        execAndTestSuccessfull("POWER(2; 3)", Integer.valueOf(8), Datatype.INTEGER);
        execAndTestSuccessfull("POWER(4; 2)", Integer.valueOf(16), Datatype.INTEGER);
        execAndTestSuccessfull("POWER(4; 0)", Integer.valueOf(1), Datatype.INTEGER);
        execAndTestSuccessfull("POWER(0; 2)", Integer.valueOf(0), Datatype.INTEGER);
    }

}
