package org.faktorips.devtools.core.util;


import junit.framework.TestCase;

/**
 * 
 * @author Jan Ortmann
 */
public class QNameUtilTest extends TestCase {

    /*
     * Test method for 'org.faktorips.devtools.core.util.QNameUtil.getPackageName(String)'
     */
    public void testGetPackageName() {
        assertNull(QNameUtil.getPackageName(null));
        assertEquals("", QNameUtil.getPackageName("Test"));
        assertEquals("a", QNameUtil.getPackageName("a.Test"));
        assertEquals("a.b", QNameUtil.getPackageName("a.b.Test"));
    }

    /*
     * Test method for 'org.faktorips.devtools.core.util.QNameUtil.getUnqualifiedName(String)'
     */
    public void testGetUnqualifiedName() {
        assertNull(QNameUtil.getUnqualifiedName(null));
        assertEquals("Test", QNameUtil.getUnqualifiedName("Test"));
        assertEquals("Test", QNameUtil.getUnqualifiedName("a.Test"));
        assertEquals("Test", QNameUtil.getUnqualifiedName("a.b.Test"));
        assertEquals("", QNameUtil.getUnqualifiedName("a.b."));
    }

    /*
     * Test method for 'org.faktorips.devtools.core.util.QNameUtil.concat(String, String)'
     */
    public void testConcat() {
        assertEquals("a.b", QNameUtil.concat("a", "b"));
        assertEquals("b", QNameUtil.concat(null, "b"));
        assertEquals("b", QNameUtil.concat("", "b"));
        assertEquals("a", QNameUtil.concat("a", null));
        assertEquals("a", QNameUtil.concat("a", ""));
        assertNull(QNameUtil.concat(null, null));
        assertEquals("", QNameUtil.concat("", ""));
    }

}
