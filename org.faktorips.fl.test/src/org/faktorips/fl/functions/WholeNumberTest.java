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

/**
 *
 */
public class WholeNumberTest extends FunctionAbstractTest {
    @Test
    public void test() throws Exception {
        registerFunction(new WholeNumber("WHOLENUMBER", ""));
        execAndTestSuccessfull("WHOLENUMBER(3.24)", new Integer(3), Datatype.INTEGER);
        execAndTestSuccessfull("WHOLENUMBER(-3.24)", new Integer(-3), Datatype.INTEGER);
    }
}
