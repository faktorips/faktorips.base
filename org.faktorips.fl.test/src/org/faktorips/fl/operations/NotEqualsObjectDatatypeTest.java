/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
        getCompiler().setBinaryOperations(toArray(new NotEqualsObjectDatatype(Datatype.DECIMAL, Datatype.DECIMAL)));
        getCompiler().setEnsureResultIsObject(false);
        execAndTestSuccessfull("1!=2", true);
        execAndTestSuccessfull("1.0!=1.0", false);
    }

    @Test
    public void testMoney() throws Exception {
        getCompiler().setBinaryOperations(toArray(new NotEqualsObjectDatatype(Datatype.MONEY, Datatype.MONEY)));
        getCompiler().setEnsureResultIsObject(false);
        execAndTestSuccessfull("1EUR!=2EUR", true);
        execAndTestSuccessfull("1.23EUR!=1.23EUR", false);
    }

    @Test
    public void testString() throws Exception {
        getCompiler().setBinaryOperations(toArray(new NotEqualsObjectDatatype(Datatype.STRING, Datatype.STRING)));
        getCompiler().setEnsureResultIsObject(false);
        execAndTestSuccessfull("\"abc\"!=\"cde\"", true);
        execAndTestSuccessfull("\"abc\"!=\"abc\"", false);
    }

}
