/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.fl.functions;

import org.faktorips.datatype.Datatype;
import org.junit.Before;
import org.junit.Test;

public class AndOrTest extends FunctionAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        registerFunction(new And("AND", ""));
        registerFunction(new Or("OR", ""));
        compiler.setEnsureResultIsObject(false);
    }

    @Test
    public void testCompile() throws Exception {
        execAndTestSuccessfull("AND(true; OR(true; false))", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("AND(true; OR(false; true))", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("AND(false; OR(true; false))", Boolean.FALSE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("AND(false; OR(false; true))", Boolean.FALSE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(true; AND(true; false))", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(true; AND(false; true))", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(false; AND(true; false))", Boolean.FALSE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(false; AND(true; true))", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
    }
}
