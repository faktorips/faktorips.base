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

package org.faktorips.valueset;

import java.util.Set;

import junit.framework.TestCase;

import org.faktorips.values.Decimal;

public class DecimalRangeTest extends TestCase {

    public void testValueOf() {
        DecimalRange range = DecimalRange.valueOf("1.25", "5.67");
        Decimal lower = range.getLowerBound();
        Decimal upper = range.getUpperBound();
        assertEquals(Decimal.valueOf(125, 2), lower);
        assertEquals(Decimal.valueOf(567, 2), upper);
    }

    public void testConstructor() {
        DecimalRange range = new DecimalRange(Decimal.valueOf(125, 2), Decimal.valueOf(567, 2));
        Decimal lower = range.getLowerBound();
        Decimal upper = range.getUpperBound();
        assertEquals(Decimal.valueOf(125, 2), lower);
        assertEquals(Decimal.valueOf(567, 2), upper);
    }

    public void testConstructorWithStep() {
        DecimalRange.valueOf(Decimal.valueOf(new Integer(10)), Decimal.valueOf(new Integer(100)),
                Decimal.valueOf(10, 0));
        DecimalRange.valueOf(Decimal.valueOf(135, 2), Decimal.valueOf(108, 1), Decimal.valueOf(135, 2));

        try {
            // step doesn't fit to range
            DecimalRange.valueOf(Decimal.valueOf(new Integer(10)), Decimal.valueOf(new Integer(100)),
                    Decimal.valueOf(new Integer(12)));
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }

        try {
            DecimalRange.valueOf(Decimal.valueOf(new Integer(10)), Decimal.valueOf(new Integer(100)),
                    Decimal.valueOf(new Integer(0)));
            fail("Expect to fail since a step size of zero is not allowed.");
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }
    }

    public void testContains() {
        DecimalRange range = new DecimalRange(Decimal.valueOf(new Integer(10)), Decimal.valueOf(new Integer(100)));
        assertTrue(range.contains(Decimal.valueOf(new Integer(30))));
        assertFalse(range.contains(Decimal.valueOf(new Integer(120))));
        assertFalse(range.contains(Decimal.valueOf(new Integer(5))));

        range = DecimalRange.valueOf(Decimal.valueOf(new Integer(10)), Decimal.valueOf(new Integer(100)), Decimal.NULL);
        assertTrue(range.contains(Decimal.valueOf(new Integer(30))));
        assertFalse(range.contains(Decimal.valueOf(new Integer(120))));
        assertFalse(range.contains(Decimal.valueOf(new Integer(5))));

        range = DecimalRange.valueOf(Decimal.valueOf(new Integer(10)), Decimal.valueOf(new Integer(100)),
                Decimal.valueOf(new Integer(10)));

        assertTrue(range.contains(Decimal.valueOf(30, 0)));
        assertFalse(range.contains(Decimal.valueOf(35, 0)));
    }

    public void testGetValues() {
        DecimalRange range = new DecimalRange(Decimal.valueOf(new Integer(10)), Decimal.valueOf(new Integer(100)));
        try {
            range.getValues(false);
            fail();
        } catch (IllegalStateException e) {
            // Expected exception.
        }

        range = DecimalRange.valueOf(Decimal.valueOf(new Integer(10)), Decimal.valueOf(new Integer(100)), Decimal.NULL);
        try {
            range.getValues(false);
            fail();
        } catch (IllegalStateException e) {
            // Expected exception.
        }

        range = DecimalRange.valueOf(Decimal.valueOf(new Integer(10)), null, Decimal.valueOf(new Integer(10)));

        try {
            range.getValues(false);
            fail();
        } catch (IllegalStateException e) {
            // Expected exception.
        }

        range = DecimalRange.valueOf(Decimal.valueOf(new Integer(10)), Decimal.valueOf(new Integer(100)),
                Decimal.valueOf(new Integer(10)));

        Set<Decimal> values = range.getValues(false);
        assertEquals(10, values.size());

        assertTrue(values.contains(Decimal.valueOf(100, 0)));
        assertTrue(values.contains(Decimal.valueOf(70, 0)));
        assertTrue(values.contains(Decimal.valueOf(10, 0)));

        range = DecimalRange.valueOf(Decimal.valueOf(new Integer(10)), Decimal.valueOf(new Integer(100)),
                Decimal.valueOf(new Integer(10)), true);
        values = range.getValues(false);
        assertEquals(11, values.size());

        assertTrue(values.contains(Decimal.valueOf(100, 0)));
        assertTrue(values.contains(Decimal.valueOf(70, 0)));
        assertTrue(values.contains(Decimal.valueOf(10, 0)));
        assertTrue(values.contains(Decimal.NULL));
    }

    public void testSerializable() throws Exception {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(new Integer(10)), Decimal.valueOf(new Integer(100)),
                Decimal.valueOf(new Integer(10)), true);
        TestUtil.testSerializable(range);
    }

}
