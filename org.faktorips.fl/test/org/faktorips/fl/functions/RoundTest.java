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

import java.math.BigDecimal;

import org.faktorips.datatype.Datatype;
import org.faktorips.values.Decimal;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class RoundTest extends FunctionAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testRoundUp() throws Exception {
        registerFunction(new Round("ROUNDUP", "", BigDecimal.ROUND_UP));
        execAndTestSuccessfull("ROUNDUP(3.25; 1)", Decimal.valueOf("3.3"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUNDUP(3.21; 1)", Decimal.valueOf("3.3"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUNDUP(-3.21; 1)", Decimal.valueOf("-3.3"), Datatype.DECIMAL);
    }

    @Test
    public void testRoundDown() throws Exception {
        registerFunction(new Round("ROUNDDOWN", "", BigDecimal.ROUND_DOWN));
        execAndTestSuccessfull("ROUNDDOWN(3.25; 1)", Decimal.valueOf("3.2"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUNDDOWN(3.21; 1)", Decimal.valueOf("3.2"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUNDDOWN(-3.21; 1)", Decimal.valueOf("-3.2"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUNDDOWN(-3.29; 1)", Decimal.valueOf("-3.2"), Datatype.DECIMAL);
    }

    @Test
    public void testRoundHalfUp() throws Exception {
        registerFunction(new Round("ROUND", "", BigDecimal.ROUND_HALF_UP));
        execAndTestSuccessfull("ROUND(3.25; 1)", Decimal.valueOf("3.3"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUND(3.249; 1)", Decimal.valueOf("3.2"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUND(-3.21; 1)", Decimal.valueOf("-3.2"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUND(-3.29; 1)", Decimal.valueOf("-3.3"), Datatype.DECIMAL);
    }
}
