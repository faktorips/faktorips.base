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

package org.faktorips.fl;

import org.faktorips.datatype.Datatype;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;

/**
 *
 */
public class ParanthesisTest extends CompilerAbstractTest {

    public void test() throws Exception {
        execAndTestSuccessfull("3.0 + 2.0 * 5.0", Decimal.valueOf("13.00"), Datatype.DECIMAL);
        execAndTestSuccessfull("(3.0 + 2.0) * 5.0", Decimal.valueOf("25.00"), Datatype.DECIMAL);
        execAndTestSuccessfull("5.0 * (3.0 + 2.0)", Decimal.valueOf("25.00"), Datatype.DECIMAL);
        execAndTestSuccessfull("(3 + 2) * 5", new Integer(25), Datatype.INTEGER);
        execAndTestSuccessfull("(3EUR + 2EUR) * 5", Money.valueOf("25EUR"), Datatype.MONEY);
        execAndTestSuccessfull("(\"3\" + \"2\") + \"5\"", "325", Datatype.STRING);
    }

}
