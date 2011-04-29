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
import org.faktorips.values.Money;
import org.junit.Test;

public class MinMaxMoneyTest extends FunctionAbstractTest {
    @Test
    public void testCompile() throws Exception {
        registerFunction(new MinMaxMoney("MAX", "", true));
        execAndTestSuccessfull("MAX(3.0EUR; 4.0EUR)", Money.euro(4), Datatype.MONEY);
        execAndTestSuccessfull("MAX(4.0EUR; 3.0EUR)", Money.euro(4), Datatype.MONEY);

        registerFunction(new MinMaxMoney("MIN", "", false));
        execAndTestSuccessfull("MIN(3.0EUR; 4.0EUR)", Money.euro(3), Datatype.MONEY);
        execAndTestSuccessfull("MIN(4.0EUR; 3.0EUR)", Money.euro(3), Datatype.MONEY);
    }

}
