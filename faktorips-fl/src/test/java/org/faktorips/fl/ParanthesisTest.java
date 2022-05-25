/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import org.faktorips.datatype.Datatype;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.junit.Test;

/**
 *
 */
public class ParanthesisTest extends JavaExprCompilerAbstractTest {
    @Test
    public void test() throws Exception {
        execAndTestSuccessfull("3.0 + 2.0 * 5.0", Decimal.valueOf("13.00"), Datatype.DECIMAL);
        execAndTestSuccessfull("(3.0 + 2.0) * 5.0", Decimal.valueOf("25.00"), Datatype.DECIMAL);
        execAndTestSuccessfull("5.0 * (3.0 + 2.0)", Decimal.valueOf("25.00"), Datatype.DECIMAL);
        execAndTestSuccessfull("(3 + 2) * 5", Integer.valueOf(25), Datatype.INTEGER);
        execAndTestSuccessfull("(3EUR + 2EUR) * 5", Money.valueOf("25EUR"), Datatype.MONEY);
        execAndTestSuccessfull("(\"3\" + \"2\") + \"5\"", "325", Datatype.STRING);
    }

}
