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

package org.faktorips.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ObjectPropertyTest {

    /**
     * Test method for 'org.faktorips.util.message.ObjectProperty.hashCode()'
     */
	@Test
    public void testHashCode() {
        ObjectProperty op1 = new ObjectProperty(new Integer(1), "toString");
        ObjectProperty op2 = new ObjectProperty(new Integer(1), "toString");
        assertEquals(op1.hashCode(), op2.hashCode());

        ObjectProperty op3 = new ObjectProperty(new Integer(2), "toString");
        assertFalse(op1.hashCode() == op3.hashCode());

    }

    /**
     * Test method for 'org.faktorips.util.message.ObjectProperty.equals(Object)'
     */
	@Test
    public void testEqualsObject() {
        ObjectProperty op1 = new ObjectProperty(new Integer(1), "toString");
        ObjectProperty op2 = new ObjectProperty(new Integer(1), "toString");
        assertEquals(op1, op2);

        ObjectProperty op3 = new ObjectProperty(new Integer(2), "toString");
        assertTrue(!op1.equals(op3));

    }

}
