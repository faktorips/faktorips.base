/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
import org.junit.Test;

public class PowerIntTest extends FunctionAbstractTest {

    @Test
    public void testCompile() throws Exception {
        registerFunction(new PowerInt("POWER", ""));
        execAndTestSuccessfull("POWER(2; 3)", new Integer(8), Datatype.INTEGER);
        execAndTestSuccessfull("POWER(4; 2)", new Integer(16), Datatype.INTEGER);
        execAndTestSuccessfull("POWER(4; 0)", new Integer(1), Datatype.INTEGER);
        execAndTestSuccessfull("POWER(0; 2)", new Integer(0), Datatype.INTEGER);
    }

}
