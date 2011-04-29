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

/**
 *
 */
public class WholeNumberTest extends FunctionAbstractTest {
    @Test
    public void test() throws Exception {
        registerFunction(new WholeNumber("WHOLENUMBER", ""));
        execAndTestSuccessfull("WHOLENUMBER(3.24)", new Integer(3), Datatype.INTEGER);
        execAndTestSuccessfull("WHOLENUMBER(-3.24)", new Integer(-3), Datatype.INTEGER);
    }
}
