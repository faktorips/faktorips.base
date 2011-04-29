/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
import org.junit.Test;

public class MinMaxIntTest extends FunctionAbstractTest {
    @Test
    public void testCompile() throws Exception {
        registerFunction(new MinMaxInt("MAX", "", true));

        execAndTestSuccessfull("MAX(3; 4)", new Integer(4), Datatype.INTEGER);
        execAndTestSuccessfull("MAX(4; 3)", new Integer(4), Datatype.INTEGER);
        execAndTestSuccessfull("MAX(a; 3)", new Integer(4), new String[] { "a" }, new Datatype[] { Datatype.INTEGER },
                new Object[] { new Integer(4) }, Datatype.INTEGER);
        execAndTestSuccessfull("MAX(4; a)", new Integer(4), new String[] { "a" }, new Datatype[] { Datatype.INTEGER },
                new Object[] { new Integer(3) }, Datatype.INTEGER);

        registerFunction(new MinMaxInt("MIN", "", false));
        execAndTestSuccessfull("MIN(3; 4)", new Integer(3), Datatype.INTEGER);
        execAndTestSuccessfull("MIN(4; 3)", new Integer(3), Datatype.INTEGER);
        execAndTestSuccessfull("MIN(a; 3)", new Integer(3), new String[] { "a" }, new Datatype[] { Datatype.INTEGER },
                new Object[] { new Integer(4) }, Datatype.INTEGER);
        execAndTestSuccessfull("MIN(4; a)", new Integer(3), new String[] { "a" }, new Datatype[] { Datatype.INTEGER },
                new Object[] { new Integer(3) }, Datatype.INTEGER);

    }
}
