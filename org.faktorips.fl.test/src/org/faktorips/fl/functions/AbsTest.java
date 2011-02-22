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

package org.faktorips.fl.functions;

import org.faktorips.datatype.Datatype;
import org.faktorips.values.Decimal;
import org.junit.Test;

/**
 *
 */
public class AbsTest extends FunctionAbstractTest {

    @Test
    public void testRoundUp() throws Exception {
        registerFunction(new Abs("ABS", ""));
        execAndTestSuccessfull("ABS(3.25)", Decimal.valueOf("3.25"), Datatype.DECIMAL);
        execAndTestSuccessfull("ABS(-3.25)", Decimal.valueOf("3.25"), Datatype.DECIMAL);
    }

}
