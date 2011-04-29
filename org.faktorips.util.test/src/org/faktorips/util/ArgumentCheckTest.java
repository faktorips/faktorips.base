/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.util;

import static org.junit.Assert.fail;

import org.junit.Test;

public class ArgumentCheckTest {
	
    @Test
    public void testIsSubclassOf() {
        ArgumentCheck.isSubclassOf(String.class, String.class);
        ArgumentCheck.isSubclassOf(Double.class, Number.class);
        try {
            ArgumentCheck.isSubclassOf(String.class, Number.class);
            fail();
        } catch (IllegalArgumentException e) {
            // an exception is excepted to be thrown
        }
    }

    @Test
    public void testIsInstanceOf() {
        ArgumentCheck.isInstanceOf("123", String.class);
        ArgumentCheck.isInstanceOf(new Double(1234), Number.class);
        try {
            ArgumentCheck.isInstanceOf(this, String.class);
            fail();
        } catch (IllegalArgumentException e) {
            // an exception is excepted to be thrown
        }
    }

    @Test
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

    @Test
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
