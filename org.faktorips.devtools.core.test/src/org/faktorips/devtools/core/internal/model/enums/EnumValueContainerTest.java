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

package org.faktorips.devtools.core.internal.model.enums;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.enums.IEnumValue;

public class EnumValueContainerTest extends AbstractIpsEnumPluginTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testGetEnumValues() {
        assertEquals(2, genderEnumContent.getEnumValues().size());
    }

    public void testNewEnumValue() throws CoreException {
        IEnumValue newEnumValue = genderEnumContent.newEnumValue();
        assertEquals(newEnumValue, genderEnumContent.getEnumValues().get(2));
        assertEquals(2, newEnumValue.getEnumAttributeValuesCount());

        genderEnumContent.setEnumType("foo");
        assertNull(genderEnumContent.newEnumValue());
    }

    public void testGetEnumValuesCount() {
        assertEquals(2, genderEnumContent.getEnumValuesCount());
        assertEquals(0, genderEnumType.getEnumValuesCount());
    }

    public void testMoveEnumValueUp() throws CoreException {
        try {
            genderEnumContent.moveEnumValue(null, true);
            fail();
        } catch (NullPointerException e) {
        }

        IEnumValue newEnumValue = genderEnumContent.newEnumValue();

        assertEquals(genderEnumValueMale, genderEnumContent.getEnumValues().get(0));
        assertEquals(genderEnumValueFemale, genderEnumContent.getEnumValues().get(1));
        assertEquals(newEnumValue, genderEnumContent.getEnumValues().get(2));

        int newIndex;
        newIndex = genderEnumContent.moveEnumValue(newEnumValue, true);
        assertEquals(1, newIndex);
        assertEquals(genderEnumValueMale, genderEnumContent.getEnumValues().get(0));
        assertEquals(newEnumValue, genderEnumContent.getEnumValues().get(1));
        assertEquals(genderEnumValueFemale, genderEnumContent.getEnumValues().get(2));

        newIndex = genderEnumContent.moveEnumValue(newEnumValue, true);
        assertEquals(0, newIndex);
        assertEquals(newEnumValue, genderEnumContent.getEnumValues().get(0));
        assertEquals(genderEnumValueMale, genderEnumContent.getEnumValues().get(1));
        assertEquals(genderEnumValueFemale, genderEnumContent.getEnumValues().get(2));

        // Nothing must change if the enum value is the first one already
        newIndex = genderEnumContent.moveEnumValue(newEnumValue, true);
        assertEquals(0, newIndex);
        assertEquals(newEnumValue, genderEnumContent.getEnumValues().get(0));
        assertEquals(genderEnumValueMale, genderEnumContent.getEnumValues().get(1));
        assertEquals(genderEnumValueFemale, genderEnumContent.getEnumValues().get(2));
    }

    public void testMoveEnumValueDown() throws CoreException {
        try {
            genderEnumContent.moveEnumValue(null, false);
            fail();
        } catch (NullPointerException e) {
        }

        IEnumValue newEnumValue = genderEnumContent.newEnumValue();

        assertEquals(genderEnumValueMale, genderEnumContent.getEnumValues().get(0));
        assertEquals(genderEnumValueFemale, genderEnumContent.getEnumValues().get(1));
        assertEquals(newEnumValue, genderEnumContent.getEnumValues().get(2));

        int newIndex;
        newIndex = genderEnumContent.moveEnumValue(genderEnumValueMale, false);
        assertEquals(1, newIndex);
        assertEquals(genderEnumValueFemale, genderEnumContent.getEnumValues().get(0));
        assertEquals(genderEnumValueMale, genderEnumContent.getEnumValues().get(1));
        assertEquals(newEnumValue, genderEnumContent.getEnumValues().get(2));

        newIndex = genderEnumContent.moveEnumValue(genderEnumValueMale, false);
        assertEquals(2, newIndex);
        assertEquals(genderEnumValueFemale, genderEnumContent.getEnumValues().get(0));
        assertEquals(newEnumValue, genderEnumContent.getEnumValues().get(1));
        assertEquals(genderEnumValueMale, genderEnumContent.getEnumValues().get(2));

        // Nothing must change if the enum value is the last one already
        newIndex = genderEnumContent.moveEnumValue(genderEnumValueMale, false);
        assertEquals(2, newIndex);
        assertEquals(genderEnumValueFemale, genderEnumContent.getEnumValues().get(0));
        assertEquals(newEnumValue, genderEnumContent.getEnumValues().get(1));
        assertEquals(genderEnumValueMale, genderEnumContent.getEnumValues().get(2));
    }

    public void testGetIndexOfEnumValue() throws CoreException {
        assertEquals(0, genderEnumContent.getIndexOfEnumValue(genderEnumValueMale));
        assertEquals(1, genderEnumContent.getIndexOfEnumValue(genderEnumValueFemale));

        genderEnumContent.moveEnumValue(genderEnumValueFemale, true);
        assertEquals(1, genderEnumContent.getIndexOfEnumValue(genderEnumValueMale));
        assertEquals(0, genderEnumContent.getIndexOfEnumValue(genderEnumValueFemale));
    }

    public void testClear() {
        genderEnumContent.clear();
        assertEquals(0, genderEnumContent.getEnumValuesCount());
    }

}
