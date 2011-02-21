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

package org.faktorips.devtools.core.enums;

import junit.framework.TestCase;

public class DefaultEnumTypeTest extends TestCase {

    private static final String MALE_VALUE = "male"; //$NON-NLS-1$
    private static final String FEMALE_VALUE = "female"; //$NON-NLS-1$
    private static final String GENDER_ENUM_TYPE_NAME = "Gender"; //$NON-NLS-1$

    private DefaultEnumType gender;
    private DefaultEnumValue male;
    private DefaultEnumValue female;

    @Override
    protected void setUp() {
        gender = new DefaultEnumType(GENDER_ENUM_TYPE_NAME, DefaultEnumValue.class);
        male = new DefaultEnumValue(gender, MALE_VALUE);
        female = new DefaultEnumValue(gender, FEMALE_VALUE);
    }

    public void testGetValues() {
        assertEquals(2, gender.getValues().length);
        assertEquals(male, gender.getValues()[0]);
        assertEquals(female, gender.getValues()[1]);

        // Defensive copy test
        gender.getValues()[0] = null; // Modify resulting array
        assertEquals(male, gender.getValues()[0]);
    }

    public void testGetValueIds() {
        assertEquals(2, gender.getValueIds().length);
        assertEquals("male", gender.getValueIds()[0]); //$NON-NLS-1$
        assertEquals("female", gender.getValueIds()[1]); //$NON-NLS-1$
    }

    public void testContainsValue() {
        assertTrue(gender.containsValue(MALE_VALUE));
        assertTrue(gender.containsValue(FEMALE_VALUE));
        assertFalse(gender.containsValue("unknown")); //$NON-NLS-1$
    }

    public void testGetEnumValue_int() {
        assertEquals(male, gender.getEnumValue(0));
    }

}
