/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.BinaryOperation;
import org.faktorips.fl.CompilerAbstractTest;



/**
 *
 */
public class EqualsPrimitiveTypePrimitiveTypeTest extends CompilerAbstractTest {
    
    protected void setUp() throws Exception {
        super.setUp();
        compiler.setEnsureResultIsObject(false);
    }
    
    public void testSuccessfull_int() throws Exception {
        compiler.setBinaryOperations(new BinaryOperation[]{new EqualsPrimtiveType(Datatype.PRIMITIVE_INT)});
        execAndTestSuccessfull("1=2", false); 
        execAndTestSuccessfull("1=1", true); 
    }

    public void testSuccessfull_boolean() throws Exception {
        compiler.setBinaryOperations(new BinaryOperation[]{new EqualsPrimtiveType(Datatype.PRIMITIVE_BOOLEAN)});
        execAndTestSuccessfull("true=true", true); 
        execAndTestSuccessfull("false=true", false); 
    }
}
