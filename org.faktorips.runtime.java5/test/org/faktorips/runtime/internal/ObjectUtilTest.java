/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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
