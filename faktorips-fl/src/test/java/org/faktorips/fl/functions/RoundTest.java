/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
        registerFunction(new Round("ROUNDUP", "", RoundingMode.UP));
        execAndTestSuccessfull("ROUNDUP(3.25; 1)", Decimal.valueOf("3.3"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUNDUP(3.21; 1)", Decimal.valueOf("3.3"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUNDUP(-3.21; 1)", Decimal.valueOf("-3.3"), Datatype.DECIMAL);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testRoundUp_RoundingModeInt() throws Exception {
        registerFunction(new Round("ROUNDUP", "", BigDecimal.ROUND_UP));
        execAndTestSuccessfull("ROUNDUP(3.25; 1)", Decimal.valueOf("3.3"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUNDUP(3.21; 1)", Decimal.valueOf("3.3"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUNDUP(-3.21; 1)", Decimal.valueOf("-3.3"), Datatype.DECIMAL);
    }

    @Test
    public void testRoundDown() throws Exception {
        registerFunction(new Round("ROUNDDOWN", "", RoundingMode.DOWN));
        execAndTestSuccessfull("ROUNDDOWN(3.25; 1)", Decimal.valueOf("3.2"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUNDDOWN(3.21; 1)", Decimal.valueOf("3.2"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUNDDOWN(-3.21; 1)", Decimal.valueOf("-3.2"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUNDDOWN(-3.29; 1)", Decimal.valueOf("-3.2"), Datatype.DECIMAL);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testRoundDown_RoundingModeInt() throws Exception {
        registerFunction(new Round("ROUNDDOWN", "", BigDecimal.ROUND_DOWN));
        execAndTestSuccessfull("ROUNDDOWN(3.25; 1)", Decimal.valueOf("3.2"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUNDDOWN(3.21; 1)", Decimal.valueOf("3.2"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUNDDOWN(-3.21; 1)", Decimal.valueOf("-3.2"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUNDDOWN(-3.29; 1)", Decimal.valueOf("-3.2"), Datatype.DECIMAL);
    }

    @Test
    public void testRoundHalfUp() throws Exception {
        registerFunction(new Round("ROUND", "", RoundingMode.HALF_UP));
        execAndTestSuccessfull("ROUND(3.25; 1)", Decimal.valueOf("3.3"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUND(3.249; 1)", Decimal.valueOf("3.2"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUND(-3.21; 1)", Decimal.valueOf("-3.2"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUND(-3.29; 1)", Decimal.valueOf("-3.3"), Datatype.DECIMAL);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testRoundHalfUp_RoundingModeInt() throws Exception {
        registerFunction(new Round("ROUND", "", BigDecimal.ROUND_HALF_UP));
        execAndTestSuccessfull("ROUND(3.25; 1)", Decimal.valueOf("3.3"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUND(3.249; 1)", Decimal.valueOf("3.2"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUND(-3.21; 1)", Decimal.valueOf("-3.2"), Datatype.DECIMAL);
        execAndTestSuccessfull("ROUND(-3.29; 1)", Decimal.valueOf("-3.3"), Datatype.DECIMAL);
    }
}
