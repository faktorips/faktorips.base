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
import org.faktorips.values.Money;
import org.junit.Test;

public class MinMaxMoneyTest extends FunctionAbstractTest {
    @Test
    public void testCompile() throws Exception {
        registerFunction(new MinMaxMoney("MAX", "", true));
        execAndTestSuccessfull("MAX(3.0EUR; 4.0EUR)", Money.euro(4), Datatype.MONEY);
        execAndTestSuccessfull("MAX(4.0EUR; 3.0EUR)", Money.euro(4), Datatype.MONEY);

        registerFunction(new MinMaxMoney("MIN", "", false));
        execAndTestSuccessfull("MIN(3.0EUR; 4.0EUR)", Money.euro(3), Datatype.MONEY);
        execAndTestSuccessfull("MIN(4.0EUR; 3.0EUR)", Money.euro(3), Datatype.MONEY);
    }

}
