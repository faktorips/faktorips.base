/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class DefaultEnumTypeTest {

    private static final String MALE_VALUE = "male"; //$NON-NLS-1$
    private static final String FEMALE_VALUE = "female"; //$NON-NLS-1$
    private static final String GENDER_ENUM_TYPE_NAME = "Gender"; //$NON-NLS-1$

    private DefaultEnumType gender;
    private DefaultEnumValue male;
    private DefaultEnumValue female;

    @Before
    public void setUp() {
        gender = new DefaultEnumType(GENDER_ENUM_TYPE_NAME, DefaultEnumValue.class);
        male = new DefaultEnumValue(gender, MALE_VALUE);
        female = new DefaultEnumValue(gender, FEMALE_VALUE);
    }

    @Test
    public void testGetValues() {
        assertEquals(2, gender.getValues().length);
        assertEquals(male, gender.getValues()[0]);
        assertEquals(female, gender.getValues()[1]);

        // Defensive copy test
        gender.getValues()[0] = null; // Modify resulting array
        assertEquals(male, gender.getValues()[0]);
    }

    @Test
    public void testGetValueIds() {
        assertEquals(2, gender.getValueIds().length);
        assertEquals("male", gender.getValueIds()[0]); //$NON-NLS-1$
        assertEquals("female", gender.getValueIds()[1]); //$NON-NLS-1$
    }

    @Test
    public void testContainsValue() {
        assertTrue(gender.containsValue(MALE_VALUE));
        assertTrue(gender.containsValue(FEMALE_VALUE));
        assertFalse(gender.containsValue("unknown")); //$NON-NLS-1$
    }

    @Test
    public void testGetEnumValue_int() {
        assertEquals(male, gender.getEnumValue(0));
    }

}
