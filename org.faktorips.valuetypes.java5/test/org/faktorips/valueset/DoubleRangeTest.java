/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import junit.framework.TestCase;

public class DoubleRangeTest extends TestCase {

    public DoubleRangeTest(String name) {
        super(name);
    }

    public void testConstructor() {
        DoubleRange range = new DoubleRange(5.0, 10.0);
        assertEquals(range.getLowerBound().doubleValue(), 5.0, 0.0);
        assertEquals(range.getUpperBound().doubleValue(), 10.0, 0.0);
        assertFalse(range.containsNull());
    }

    public void testConstructor2() {
        DoubleRange range = new DoubleRange(5.0, 10.0, true);
        assertEquals(range.getLowerBound().doubleValue(), 5.0, 0.0);
        assertEquals(range.getUpperBound().doubleValue(), 10.0, 0.0);
        assertTrue(range.containsNull());
    }

    public void testSerializable() throws Exception {
        TestUtil.testSerializable(new DoubleRange(5.0, 10.0));
    }

}
