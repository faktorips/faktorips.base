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

package org.faktorips.fl.functions;

import org.faktorips.datatype.Datatype;
import org.junit.Test;

public class PowerIntTest extends FunctionAbstractTest {

    @Test
    public void testCompile() throws Exception {
        registerFunction(new PowerInt("POWER", ""));
        execAndTestSuccessfull("POWER(2; 3)", new Integer(8), Datatype.INTEGER);
        execAndTestSuccessfull("POWER(4; 2)", new Integer(16), Datatype.INTEGER);
        execAndTestSuccessfull("POWER(4; 0)", new Integer(1), Datatype.INTEGER);
        execAndTestSuccessfull("POWER(0; 2)", new Integer(0), Datatype.INTEGER);
    }

}
