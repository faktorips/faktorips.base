/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.valueset;

import java.math.BigDecimal;
import java.util.Set;

import junit.framework.TestCase;

public class BigDecimalRangeTest extends TestCase {

    public void testValueOf() {
        BigDecimalRange range = BigDecimalRange.valueOf("1.25", "5.67");
        BigDecimal lower = range.getLowerBound();
        BigDecimal upper = range.getUpperBound();
        assertEquals(BigDecimal.valueOf(125, 2), lower);
        assertEquals(BigDecimal.valueOf(567, 2), upper);
    }

    public void testConstructor() {
        BigDecimalRange range = new BigDecimalRange(BigDecimal.valueOf(125, 2), BigDecimal.valueOf(567, 2));
        BigDecimal lower = range.getLowerBound();
        BigDecimal upper = range.getUpperBound();
        assertEquals(BigDecimal.valueOf(125, 2), lower);
        assertEquals(BigDecimal.valueOf(567, 2), upper);
    }

    public void testConstructorWithStep() {

        BigDecimalRange.valueOf(BigDecimal.valueOf(new Integer(10)), BigDecimal.valueOf(new Integer(100)), BigDecimal
                .valueOf(10, 0));
        BigDecimalRange.valueOf(BigDecimal.valueOf(135, 2), BigDecimal.valueOf(108, 1), BigDecimal.valueOf(135, 2));

        try {
            // step doesn't fit to range
            BigDecimalRange.valueOf(BigDecimal.valueOf(new Integer(10)), BigDecimal.valueOf(new Integer(100)),
                    BigDecimal.valueOf(new Integer(12)));
            fail();
        } catch (IllegalArgumentException e) {
            // ok exception expected
        }

        try {
            BigDecimalRange.valueOf(BigDecimal.valueOf(new Integer(10)), BigDecimal.valueOf(new Integer(100)),
                    BigDecimal.valueOf(new Integer(0)));
            fail("Expect to fail since a step size of zero is not allowed.");
        } catch (IllegalArgumentException e) {
            // ok exception expected
        }
    }

    public void testContains() {

        BigDecimalRange range = new BigDecimalRange(BigDecimal.valueOf(new Integer(10)), BigDecimal
                .valueOf(new Integer(100)));
        assertTrue(range.contains(BigDecimal.valueOf(new Integer(30))));
        assertFalse(range.contains(BigDecimal.valueOf(new Integer(120))));
        assertFalse(range.contains(BigDecimal.valueOf(new Integer(5))));

        range = BigDecimalRange
                .valueOf(BigDecimal.valueOf(new Integer(10)), BigDecimal.valueOf(new Integer(100)), null); // ?
        assertTrue(range.contains(BigDecimal.valueOf(new Integer(30))));
        assertFalse(range.contains(BigDecimal.valueOf(new Integer(120))));
        assertFalse(range.contains(BigDecimal.valueOf(new Integer(5))));

        range = BigDecimalRange.valueOf(BigDecimal.valueOf(new Integer(10)), BigDecimal.valueOf(new Integer(100)),
                BigDecimal.valueOf(new Integer(10)));

        assertTrue(range.contains(BigDecimal.valueOf(30, 0)));
        assertFalse(range.contains(BigDecimal.valueOf(35, 0)));

    }

    public void testGetValues() {

        BigDecimalRange range = new BigDecimalRange(BigDecimal.valueOf(new Integer(10)), BigDecimal
                .valueOf(new Integer(100)));
        try {
            range.getValues(false);
            fail();
        } catch (IllegalStateException e) {
            // ok exception expected
        }

        range = BigDecimalRange
                .valueOf(BigDecimal.valueOf(new Integer(10)), BigDecimal.valueOf(new Integer(100)), null);
        try {
            range.getValues(false);
            fail();
        } catch (IllegalStateException e) {
            // ok exception expected
        }

        range = BigDecimalRange.valueOf(BigDecimal.valueOf(new Integer(10)), null, BigDecimal.valueOf(new Integer(10)));

        try {
            range.getValues(false);
            fail();
        } catch (IllegalStateException e) {
            // ok exception expected
        }

        range = BigDecimalRange.valueOf(BigDecimal.valueOf(new Integer(10)), BigDecimal.valueOf(new Integer(100)),
                BigDecimal.valueOf(new Integer(10)));

        Set<BigDecimal> values = range.getValues(false);
        assertEquals(10, values.size());

        assertTrue(values.contains(BigDecimal.valueOf(100, 0)));
        assertTrue(values.contains(BigDecimal.valueOf(70, 0)));
        assertTrue(values.contains(BigDecimal.valueOf(10, 0)));

        range = BigDecimalRange.valueOf(BigDecimal.valueOf(new Integer(10)), BigDecimal.valueOf(new Integer(100)),
                BigDecimal.valueOf(new Integer(10)), true);
        values = range.getValues(false);
        assertEquals(11, values.size());

        assertTrue(values.contains(BigDecimal.valueOf(100, 0)));
        assertTrue(values.contains(BigDecimal.valueOf(70, 0)));
        assertTrue(values.contains(BigDecimal.valueOf(10, 0)));
        assertTrue(values.contains(null));

    }

    public void testSerializable() throws Exception {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(new Integer(10)), BigDecimal
                .valueOf(new Integer(100)), BigDecimal.valueOf(new Integer(10)), true);
        TestUtil.testSerializable(range);
    }
}
