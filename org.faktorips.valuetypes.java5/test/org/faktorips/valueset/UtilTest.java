/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.valueset;

import junit.framework.TestCase;

public class UtilTest extends TestCase {

    /*
     * Test method for 'org.faktorips.valueset.Util.equals(Object, Object)'
     */
    public void testEqualsObjectObject() {
        
        assertTrue(Util.equals(new Integer(1), new Integer(1)));
        assertTrue(Util.equals(null, null));
        assertFalse(Util.equals(null, new Integer(1)));
    }

}
