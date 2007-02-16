/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.BinaryOperation;
import org.faktorips.fl.CompilerAbstractTest;

public class NotEqualsObjectDatatypeTest extends CompilerAbstractTest {

    public void testDecimal() throws Exception{
        compiler.setBinaryOperations(new BinaryOperation[]{new NotEqualsObjectDatatype(Datatype.DECIMAL, Datatype.DECIMAL)});
        compiler.setEnsureResultIsObject(false);
        execAndTestSuccessfull("1!=2", true);
        execAndTestSuccessfull("1.0!=1.0", false); 
    }
    
    public void testMoney() throws Exception{
        compiler.setBinaryOperations(new BinaryOperation[]{new NotEqualsObjectDatatype(Datatype.MONEY, Datatype.MONEY)});
        compiler.setEnsureResultIsObject(false);
        execAndTestSuccessfull("1EUR!=2EUR", true);
        execAndTestSuccessfull("1.23EUR!=1.23EUR", false); 
    }

    public void testString() throws Exception{
        compiler.setBinaryOperations(new BinaryOperation[]{new NotEqualsObjectDatatype(Datatype.STRING, Datatype.STRING)});
        compiler.setEnsureResultIsObject(false);
        execAndTestSuccessfull("\"abc\"!=\"cde\"", true);
        execAndTestSuccessfull("\"abc\"!=\"abc\"", false); 
    }

}
