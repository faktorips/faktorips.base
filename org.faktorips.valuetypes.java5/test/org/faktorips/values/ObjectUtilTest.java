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

package org.faktorips.values;

import junit.framework.TestCase;

/**
 * 
 * @author Jan Ortmann
 */
public class ObjectUtilTest extends TestCase {

    /*
     * Test method for 'org.faktorips.values.ObjectUtil.isNull(Object)'
     */
    public void testIsNull() {
        assertTrue(ObjectUtil.isNull(null));
        assertTrue(ObjectUtil.isNull(Money.NULL));
        assertFalse(ObjectUtil.isNull(Money.euro(42, 0)));
    }

}
