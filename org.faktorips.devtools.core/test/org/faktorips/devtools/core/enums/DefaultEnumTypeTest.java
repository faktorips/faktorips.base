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

package org.faktorips.devtools.core.enums;

import junit.framework.TestCase;

/**
 *
 */
public class DefaultEnumTypeTest extends TestCase {

    private DefaultEnumType gender;
    private DefaultEnumValue male;
    private DefaultEnumValue female;

    @Override
    protected void setUp() {
        gender = new DefaultEnumType("Gender", DefaultEnumValue.class);
        male = new DefaultEnumValue(gender, "male");
        female = new DefaultEnumValue(gender, "female");
    }

    public void testAddValue() {
    }

    public void testGetValues() {
        assertEquals(2, gender.getValues().length);
        assertEquals(male, gender.getValues()[0]);
        assertEquals(female, gender.getValues()[1]);

        // defensive copy test
        gender.getValues()[0] = null; // modifiy resulting array
        assertEquals(male, gender.getValues()[0]);
    }

    public void testGetValueIds() {
        assertEquals(2, gender.getValueIds().length);
        assertEquals("male", gender.getValueIds()[0]);
        assertEquals("female", gender.getValueIds()[1]);
    }

    public void testContainsValue() {
        assertTrue(gender.containsValue("male"));
        assertTrue(gender.containsValue("female"));
        assertFalse(gender.containsValue("unknown"));
    }

    public void testGetEnumValue() {
    }

    public void testContains() {
    }

    public void testContainsNull() {
    }

    public void testGetNumOfValues() {
    }

    public void testCompareTo() {
    }

    public void testGetEnumValue_int() {
        assertEquals(male, gender.getEnumValue(0));
    }

}
