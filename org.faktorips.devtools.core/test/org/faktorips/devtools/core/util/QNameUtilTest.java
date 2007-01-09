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
        assertEquals("", QNameUtil.getPackageName("Test")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("a", QNameUtil.getPackageName("a.Test")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("a.b", QNameUtil.getPackageName("a.b.Test")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /*
     * Test method for 'org.faktorips.devtools.core.util.QNameUtil.getUnqualifiedName(String)'
     */
    public void testGetUnqualifiedName() {
        assertNull(QNameUtil.getUnqualifiedName(null));
        assertEquals("Test", QNameUtil.getUnqualifiedName("Test")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("Test", QNameUtil.getUnqualifiedName("a.Test")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("Test", QNameUtil.getUnqualifiedName("a.b.Test")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("", QNameUtil.getUnqualifiedName("a.b.")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /*
     * Test method for 'org.faktorips.devtools.core.util.QNameUtil.concat(String, String)'
     */
    public void testConcat() {
        assertEquals("a.b", QNameUtil.concat("a", "b")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("b", QNameUtil.concat(null, "b")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("b", QNameUtil.concat("", "b")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("a", QNameUtil.concat("a", null)); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("a", QNameUtil.concat("a", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertNull(QNameUtil.concat(null, null));
        assertEquals("", QNameUtil.concat("", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
