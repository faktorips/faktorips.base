/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.JavaExprCompilerAbstractTest;
import org.junit.Test;

public class NotEqualsObjectDatatypeTest extends JavaExprCompilerAbstractTest {
    @Test
    public void testDecimal() throws Exception {
        compiler.setBinaryOperations(toArray(new NotEqualsObjectDatatype(Datatype.DECIMAL, Datatype.DECIMAL)));
        compiler.setEnsureResultIsObject(false);
        execAndTestSuccessfull("1!=2", true);
        execAndTestSuccessfull("1.0!=1.0", false);
    }

    @Test
    public void testMoney() throws Exception {
        compiler.setBinaryOperations(toArray(new NotEqualsObjectDatatype(Datatype.MONEY, Datatype.MONEY)));
        compiler.setEnsureResultIsObject(false);
        execAndTestSuccessfull("1EUR!=2EUR", true);
        execAndTestSuccessfull("1.23EUR!=1.23EUR", false);
    }

    @Test
    public void testString() throws Exception {
        compiler.setBinaryOperations(toArray(new NotEqualsObjectDatatype(Datatype.STRING, Datatype.STRING)));
        compiler.setEnsureResultIsObject(false);
        execAndTestSuccessfull("\"abc\"!=\"cde\"", true);
        execAndTestSuccessfull("\"abc\"!=\"abc\"", false);
    }

}
