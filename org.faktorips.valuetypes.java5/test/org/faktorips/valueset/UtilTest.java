/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

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
