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

public class MinMaxIntTest extends FunctionAbstractTest {
    @Test
    public void testCompile() throws Exception {
        registerFunction(new MinMaxInt("MAX", "", true));

        execAndTestSuccessfull("MAX(3; 4)", Integer.valueOf(4), Datatype.INTEGER);
        execAndTestSuccessfull("MAX(4; 3)", Integer.valueOf(4), Datatype.INTEGER);
        execAndTestSuccessfull("MAX(a; 3)", Integer.valueOf(4), new String[] { "a" },
                new Datatype[] { Datatype.INTEGER },
                new Object[] { Integer.valueOf(4) }, Datatype.INTEGER);
        execAndTestSuccessfull("MAX(4; a)", Integer.valueOf(4), new String[] { "a" },
                new Datatype[] { Datatype.INTEGER },
                new Object[] { Integer.valueOf(3) }, Datatype.INTEGER);

        registerFunction(new MinMaxInt("MIN", "", false));
        execAndTestSuccessfull("MIN(3; 4)", Integer.valueOf(3), Datatype.INTEGER);
        execAndTestSuccessfull("MIN(4; 3)", Integer.valueOf(3), Datatype.INTEGER);
        execAndTestSuccessfull("MIN(a; 3)", Integer.valueOf(3), new String[] { "a" },
                new Datatype[] { Datatype.INTEGER },
                new Object[] { Integer.valueOf(4) }, Datatype.INTEGER);
        execAndTestSuccessfull("MIN(4; a)", Integer.valueOf(3), new String[] { "a" },
                new Datatype[] { Datatype.INTEGER },
                new Object[] { Integer.valueOf(3) }, Datatype.INTEGER);

    }
}
