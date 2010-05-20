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

package org.faktorips.util;

import junit.framework.TestCase;

public class ArgumentCheckTest extends TestCase {

    public void testIsSubclassOf() {
        ArgumentCheck.isSubclassOf(this.getClass(), TestCase.class);
        try {
            ArgumentCheck.isSubclassOf(String.class, TestCase.class);
            fail();
        } catch (IllegalArgumentException e) {
            // an exception is excepted to be thrown
        }
    }

    public void testIsInstanceOf() {
        ArgumentCheck.isInstanceOf(this, TestCase.class);
        try {
            ArgumentCheck.isInstanceOf(this, String.class);
            fail();
        } catch (IllegalArgumentException e) {
            // an exception is excepted to be thrown
        }
    }

    public void testIsNullArray() {
        String[] ids = new String[3];
        try {
            ArgumentCheck.notNull(ids);
            fail();
        } catch (RuntimeException e) {
            // an exception is excepted to be thrown
        }

        ids[0] = "";
        ids[1] = "";
        ids[2] = "";

        // expected to pass
        ArgumentCheck.notNull(ids);
    }

    public void testIsNullArrayContext() {
        String[] ids = new String[3];
        try {
            ArgumentCheck.notNull(ids, this);
            fail();
        } catch (RuntimeException e) {
            // an exception is excepted to be thrown
        }

        ids[0] = "";
        ids[1] = "";
        ids[2] = "";

        // expected to pass
        ArgumentCheck.notNull(ids, this);
    }

}
