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

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class IsEmptyTest extends FunctionAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        compiler.setEnsureResultIsObject(false);
        registerFunction(new IsEmpty("ISEMPTY", ""));
    }

    @Test
    public void testDecimal() throws Exception {
        execAndTestSuccessfull("ISEMPTY(1.0)", false);
        registerFunction(new DecimalNullFct());
        execAndTestSuccessfull("ISEMPTY(DECIMALNULL())", true);
    }

    @Test
    public void testMoney() throws Exception {
        execAndTestSuccessfull("ISEMPTY(10EUR)", false);
        registerFunction(new MoneyNullFct());
        execAndTestSuccessfull("ISEMPTY(MONEYNULL())", true);
    }

    @Test
    public void testString() throws Exception {
        execAndTestSuccessfull("ISEMPTY(\"a\")", false);
    }

    @Test
    public void testInt() throws Exception {
        execAndTestSuccessfull("ISEMPTY(1)", false);
    }

    @Test
    public void testBoolean() throws Exception {
        registerFunction(new BooleanFct("TRUEOBJ", Boolean.TRUE));
        registerFunction(new BooleanFct("BOOLEANNULL", null));
        execAndTestSuccessfull("ISEMPTY(TRUEOBJ())", false);
        execAndTestSuccessfull("ISEMPTY(BOOLEANNULL())", true);
    }

    @Test
    public void testPrimitiveBoolean() throws Exception {
        execAndTestSuccessfull("ISEMPTY(true)", false);
    }

}
