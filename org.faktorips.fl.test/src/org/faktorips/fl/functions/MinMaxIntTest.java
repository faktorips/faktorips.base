/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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

        execAndTestSuccessfull("MAX(3; 4)", new Integer(4), Datatype.INTEGER);
        execAndTestSuccessfull("MAX(4; 3)", new Integer(4), Datatype.INTEGER);
        execAndTestSuccessfull("MAX(a; 3)", new Integer(4), new String[] { "a" }, new Datatype[] { Datatype.INTEGER },
                new Object[] { new Integer(4) }, Datatype.INTEGER);
        execAndTestSuccessfull("MAX(4; a)", new Integer(4), new String[] { "a" }, new Datatype[] { Datatype.INTEGER },
                new Object[] { new Integer(3) }, Datatype.INTEGER);

        registerFunction(new MinMaxInt("MIN", "", false));
        execAndTestSuccessfull("MIN(3; 4)", new Integer(3), Datatype.INTEGER);
        execAndTestSuccessfull("MIN(4; 3)", new Integer(3), Datatype.INTEGER);
        execAndTestSuccessfull("MIN(a; 3)", new Integer(3), new String[] { "a" }, new Datatype[] { Datatype.INTEGER },
                new Object[] { new Integer(4) }, Datatype.INTEGER);
        execAndTestSuccessfull("MIN(4; a)", new Integer(3), new String[] { "a" }, new Datatype[] { Datatype.INTEGER },
                new Object[] { new Integer(3) }, Datatype.INTEGER);

    }
}
