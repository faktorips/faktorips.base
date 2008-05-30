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

package org.faktorips.runtime.internal;

import junit.framework.TestCase;

/**
 * 
 * @author Jan Ortmann
 */
public class ObjectUtilTest extends TestCase {

    public void testEqualsObject() {
        assertTrue(ObjectUtil.equals(null, null));

        Object o1 = new Object();
        assertTrue(ObjectUtil.equals(o1, o1));
        assertFalse(ObjectUtil.equals(null, o1));
        assertFalse(ObjectUtil.equals(o1, null));
        
        Object o2 = new Object();
        assertFalse(ObjectUtil.equals(o1, o2));
        
    }

}
