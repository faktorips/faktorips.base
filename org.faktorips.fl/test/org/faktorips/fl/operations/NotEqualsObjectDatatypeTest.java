/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.BinaryOperation;
import org.faktorips.fl.CompilerAbstractTest;

public class NotEqualsObjectDatatypeTest extends CompilerAbstractTest {

    public void testDecimal() throws Exception {
        compiler.setBinaryOperations(new BinaryOperation[] { new NotEqualsObjectDatatype(Datatype.DECIMAL,
                Datatype.DECIMAL) });
        compiler.setEnsureResultIsObject(false);
        execAndTestSuccessfull("1!=2", true);
        execAndTestSuccessfull("1.0!=1.0", false);
    }

    public void testMoney() throws Exception {
        compiler.setBinaryOperations(new BinaryOperation[] { new NotEqualsObjectDatatype(Datatype.MONEY, Datatype.MONEY) });
        compiler.setEnsureResultIsObject(false);
        execAndTestSuccessfull("1EUR!=2EUR", true);
        execAndTestSuccessfull("1.23EUR!=1.23EUR", false);
    }

    public void testString() throws Exception {
        compiler.setBinaryOperations(new BinaryOperation[] { new NotEqualsObjectDatatype(Datatype.STRING,
                Datatype.STRING) });
        compiler.setEnsureResultIsObject(false);
        execAndTestSuccessfull("\"abc\"!=\"cde\"", true);
        execAndTestSuccessfull("\"abc\"!=\"abc\"", false);
    }

}
