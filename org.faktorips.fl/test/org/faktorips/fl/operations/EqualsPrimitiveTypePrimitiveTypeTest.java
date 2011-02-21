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
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class EqualsPrimitiveTypePrimitiveTypeTest extends CompilerAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        compiler.setEnsureResultIsObject(false);
    }

    @Test
    public void testSuccessfull_int() throws Exception {
        compiler.setBinaryOperations(new BinaryOperation[] { new EqualsPrimtiveType(Datatype.PRIMITIVE_INT) });
        execAndTestSuccessfull("1=2", false);
        execAndTestSuccessfull("1=1", true);
    }

    @Test
    public void testSuccessfull_boolean() throws Exception {
        compiler.setBinaryOperations(new BinaryOperation[] { new EqualsPrimtiveType(Datatype.PRIMITIVE_BOOLEAN) });
        execAndTestSuccessfull("true=true", true);
        execAndTestSuccessfull("false=true", false);
    }
}
