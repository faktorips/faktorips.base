/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.fl.functions;

import org.faktorips.datatype.Datatype;

public class OrTest extends FunctionAbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        registerFunction(new Or("OR", ""));
        compiler.setEnsureResultIsObject(false);
    }

    public void testCompile() throws Exception {
        // test if multiple arguments work
        execAndTestSuccessfull("OR(true)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(true; true)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(true; true; true)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(true; true; false)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(false; false; false)", Boolean.FALSE, Datatype.PRIMITIVE_BOOLEAN);

        // test if the result of an expression as argument for the OR-function works
        execAndTestSuccessfull("OR(1=1)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(1!=1)", Boolean.FALSE, Datatype.PRIMITIVE_BOOLEAN);
    }
}
