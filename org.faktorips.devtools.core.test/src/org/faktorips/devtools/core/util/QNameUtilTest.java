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

package org.faktorips.devtools.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class QNameUtilTest {

    /*
     * Test method for 'org.faktorips.devtools.core.util.QNameUtil.getPackageName(String)'
     */@Test
    public void testGetPackageName() {
        assertNull(QNameUtil.getPackageName(null));
        assertEquals("", QNameUtil.getPackageName("Test")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("a", QNameUtil.getPackageName("a.Test")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("a.b", QNameUtil.getPackageName("a.b.Test")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /*
     * Test method for 'org.faktorips.devtools.core.util.QNameUtil.getUnqualifiedName(String)'
     */@Test
    public void testGetUnqualifiedName() {
        assertNull(QNameUtil.getUnqualifiedName(null));
        assertEquals("Test", QNameUtil.getUnqualifiedName("Test")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("Test", QNameUtil.getUnqualifiedName("a.Test")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("Test", QNameUtil.getUnqualifiedName("a.b.Test")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("", QNameUtil.getUnqualifiedName("a.b.")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /*
     * Test method for 'org.faktorips.devtools.core.util.QNameUtil.concat(String, String)'
     */@Test
    public void testConcat() {
        assertEquals("a.b", QNameUtil.concat("a", "b")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("b", QNameUtil.concat(null, "b")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("b", QNameUtil.concat("", "b")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("a", QNameUtil.concat("a", null)); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("a", QNameUtil.concat("a", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertNull(QNameUtil.concat(null, null));
        assertEquals("", QNameUtil.concat("", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Test
    public void testGetSegments() {
        assertEquals(0, QNameUtil.getSegments(null).length);
        assertEquals(0, QNameUtil.getSegments("").length); //$NON-NLS-1$

        String[] segments = QNameUtil.getSegments(" "); //$NON-NLS-1$
        assertEquals(1, segments.length);
        assertEquals(" ", segments[0]); //$NON-NLS-1$

        segments = QNameUtil.getSegments("a"); //$NON-NLS-1$
        assertEquals(1, segments.length);
        assertEquals("a", segments[0]); //$NON-NLS-1$

        segments = QNameUtil.getSegments("a."); //$NON-NLS-1$
        assertEquals(1, segments.length);
        assertEquals("a", segments[0]); //$NON-NLS-1$

        segments = QNameUtil.getSegments("a.b.c.d"); //$NON-NLS-1$
        assertEquals(4, segments.length);
        assertEquals("a", segments[0]); //$NON-NLS-1$
        assertEquals("b", segments[1]); //$NON-NLS-1$
        assertEquals("c", segments[2]); //$NON-NLS-1$
        assertEquals("d", segments[3]); //$NON-NLS-1$

    }

    @Test
    public void testGetSegmentCount() {

        assertEquals(0, QNameUtil.getSegmentCount(null));

        assertEquals(0, QNameUtil.getSegmentCount("")); //$NON-NLS-1$
        assertEquals(1, QNameUtil.getSegmentCount("  ")); //$NON-NLS-1$
        assertEquals(1, QNameUtil.getSegmentCount("a")); //$NON-NLS-1$
        assertEquals(4, QNameUtil.getSegmentCount("a.b.c. d")); //$NON-NLS-1$
        assertEquals(4, QNameUtil.getSegmentCount("a.b.c.d.")); //$NON-NLS-1$
        assertEquals(5, QNameUtil.getSegmentCount("a.b.c.d. ")); //$NON-NLS-1$
    }

    @Test
    public void testGetSubSegment() {

        assertEquals("", QNameUtil.getSubSegments("", 2)); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("a", QNameUtil.getSubSegments("a.b.c.d", 1)); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("a.b", QNameUtil.getSubSegments("a.b.c.d", 2)); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("a.b.c", QNameUtil.getSubSegments("a.b.c.d", 3)); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("a.b.c.d", QNameUtil.getSubSegments("a.b.c.d", 5)); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("a.b.c", QNameUtil.getSubSegments("a.b.c.", 4)); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
