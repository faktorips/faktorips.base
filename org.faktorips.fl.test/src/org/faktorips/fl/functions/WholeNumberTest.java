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

/**
 *
 */
public class WholeNumberTest extends FunctionAbstractTest {
    @Test
    public void test() throws Exception {
        registerFunction(new WholeNumber("WHOLENUMBER", ""));
        execAndTestSuccessfull("WHOLENUMBER(3.24)", Integer.valueOf(3), Datatype.INTEGER);
        execAndTestSuccessfull("WHOLENUMBER(-3.24)", Integer.valueOf(-3), Datatype.INTEGER);
    }
}
