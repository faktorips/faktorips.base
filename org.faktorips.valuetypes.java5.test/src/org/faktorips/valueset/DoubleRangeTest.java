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

package org.faktorips.valueset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DoubleRangeTest {

    @Test
    public void testConstructor() {
        DoubleRange range = new DoubleRange(5.0, 10.0);
        assertEquals(range.getLowerBound().doubleValue(), 5.0, 0.0);
        assertEquals(range.getUpperBound().doubleValue(), 10.0, 0.0);
        assertFalse(range.containsNull());
    }

    @Test
    public void testConstructor2() {
        DoubleRange range = new DoubleRange(5.0, 10.0, true);
        assertEquals(range.getLowerBound().doubleValue(), 5.0, 0.0);
        assertEquals(range.getUpperBound().doubleValue(), 10.0, 0.0);
        assertTrue(range.containsNull());
    }

    @Test
    public void testSerializable() throws Exception {
        TestUtil.testSerializable(new DoubleRange(5.0, 10.0));
    }

}
