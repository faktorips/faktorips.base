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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
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
        IEnumAttribute integerAttribute = genderEnumType.newEnumAttribute();
        integerAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        integerAttribute.setName("integerAttribute");

        for (IEnumValue currentEnumValue : genderEnumContent.getEnumValues()) {
            IEnumAttributeValue value = currentEnumValue.newEnumAttributeValue();
            value.setValue(null);
        }

        IEnumValue newEnumValue = genderEnumContent.newEnumValue();
        assertEquals(newEnumValue, genderEnumContent.getEnumValues().get(2));
        assertEquals(3, newEnumValue.getEnumAttributeValuesCount());
        assertNull(newEnumValue.getEnumAttributeValues().get(2).getValue());

        genderEnumContent.setEnumType("foo");
        assertNull(genderEnumContent.newEnumValue());
    }

    public void testGetEnumValuesCount() {
        assertEquals(2, genderEnumContent.getEnumValuesCount());
        assertEquals(0, genderEnumType.getEnumValuesCount());
    }

    public void testMoveEnumValuesUp() throws CoreException {
        try {
            genderEnumContent.moveEnumValues(null, true);
            fail();
        } catch (NullPointerException e) {
        }

        IEnumValue newEnumValue = genderEnumContent.newEnumValue();

        assertEquals(genderEnumValueMale, genderEnumContent.getEnumValues().get(0));
        assertEquals(genderEnumValueFemale, genderEnumContent.getEnumValues().get(1));
        assertEquals(newEnumValue, genderEnumContent.getEnumValues().get(2));

        List<IEnumValue> moveList = new ArrayList<IEnumValue>(1);
        moveList.add(newEnumValue);
        int[] newIndizes;
        newIndizes = genderEnumContent.moveEnumValues(moveList, true);
        assertEquals(1, newIndizes[0]);
        assertEquals(genderEnumValueMale, genderEnumContent.getEnumValues().get(0));
        assertEquals(newEnumValue, genderEnumContent.getEnumValues().get(1));
        assertEquals(genderEnumValueFemale, genderEnumContent.getEnumValues().get(2));

        newIndizes = genderEnumContent.moveEnumValues(moveList, true);
        assertEquals(0, newIndizes[0]);
        assertEquals(newEnumValue, genderEnumContent.getEnumValues().get(0));
        assertEquals(genderEnumValueMale, genderEnumContent.getEnumValues().get(1));
        assertEquals(genderEnumValueFemale, genderEnumContent.getEnumValues().get(2));

        // Nothing must change if the enum value is the first one already
        newIndizes = genderEnumContent.moveEnumValues(moveList, true);
        assertEquals(0, newIndizes[0]);
        assertEquals(newEnumValue, genderEnumContent.getEnumValues().get(0));
        assertEquals(genderEnumValueMale, genderEnumContent.getEnumValues().get(1));
        assertEquals(genderEnumValueFemale, genderEnumContent.getEnumValues().get(2));
    }

    public void testMoveEnumValuesDown() throws CoreException {
        try {
            genderEnumContent.moveEnumValues(null, false);
            fail();
        } catch (NullPointerException e) {
        }

        IEnumValue newEnumValue = genderEnumContent.newEnumValue();

        assertEquals(genderEnumValueMale, genderEnumContent.getEnumValues().get(0));
        assertEquals(genderEnumValueFemale, genderEnumContent.getEnumValues().get(1));
        assertEquals(newEnumValue, genderEnumContent.getEnumValues().get(2));

        List<IEnumValue> moveList = new ArrayList<IEnumValue>(1);
        moveList.add(genderEnumValueMale);
        int[] newIndizes;
        newIndizes = genderEnumContent.moveEnumValues(moveList, false);
        assertEquals(1, newIndizes[0]);
        assertEquals(genderEnumValueFemale, genderEnumContent.getEnumValues().get(0));
        assertEquals(genderEnumValueMale, genderEnumContent.getEnumValues().get(1));
        assertEquals(newEnumValue, genderEnumContent.getEnumValues().get(2));

        newIndizes = genderEnumContent.moveEnumValues(moveList, false);
        assertEquals(2, newIndizes[0]);
        assertEquals(genderEnumValueFemale, genderEnumContent.getEnumValues().get(0));
        assertEquals(newEnumValue, genderEnumContent.getEnumValues().get(1));
        assertEquals(genderEnumValueMale, genderEnumContent.getEnumValues().get(2));

        // Nothing must change if the enum value is the last one already
        newIndizes = genderEnumContent.moveEnumValues(moveList, false);
        assertEquals(2, newIndizes[0]);
        assertEquals(genderEnumValueFemale, genderEnumContent.getEnumValues().get(0));
        assertEquals(newEnumValue, genderEnumContent.getEnumValues().get(1));
        assertEquals(genderEnumValueMale, genderEnumContent.getEnumValues().get(2));
    }

    public void testGetIndexOfEnumValue() throws CoreException {
        assertEquals(0, genderEnumContent.getIndexOfEnumValue(genderEnumValueMale));
        assertEquals(1, genderEnumContent.getIndexOfEnumValue(genderEnumValueFemale));

        List<IEnumValue> moveList = new ArrayList<IEnumValue>(1);
        moveList.add(genderEnumValueFemale);
        genderEnumContent.moveEnumValues(moveList, true);
        assertEquals(1, genderEnumContent.getIndexOfEnumValue(genderEnumValueMale));
        assertEquals(0, genderEnumContent.getIndexOfEnumValue(genderEnumValueFemale));
    }

    public void testClear() {
        genderEnumContent.clear();
        assertEquals(0, genderEnumContent.getEnumValuesCount());
    }

    public void testUniqueIdentifierValidation() throws CoreException {
        genderEnumType.setContainingValues(true);
        IEnumValue testValue1 = genderEnumType.newEnumValue();
        testValue1.setEnumAttributeValue(0, "id1");
        testValue1.setEnumAttributeValue(1, "name1");
        IEnumValue testValue2 = genderEnumType.newEnumValue();
        testValue2.setEnumAttributeValue(0, "id2");
        testValue2.setEnumAttributeValue(1, "name2");
        assertEquals(0, genderEnumType.validate(ipsProject).getNoOfMessages());

        // Test working after new unique attribute addition.
        IEnumAttribute newUnique = genderEnumType.newEnumAttribute();
        newUnique.setDatatype(Datatype.STRING.getQualifiedName());
        newUnique.setName("newUnique");
        newUnique.setUnique(true);
        testValue1.setEnumAttributeValue(2, "newUniqueValue");
        testValue2.setEnumAttributeValue(2, "newUniqueValue");
        getIpsModel().clearValidationCache();
        assertEquals(2, genderEnumType.validate(ipsProject).getNoOfMessages());

        // Test working for enum value deletion.
        testValue2.delete();
        getIpsModel().clearValidationCache();
        assertEquals(0, genderEnumType.validate(ipsProject).getNoOfMessages());

        // Test working for enum value addition.
        testValue2 = genderEnumType.newEnumValue();
        testValue2.setEnumAttributeValue(0, "id1");
        testValue2.setEnumAttributeValue(1, "name1");
        testValue2.setEnumAttributeValue(2, "newUniqueValue");
        getIpsModel().clearValidationCache();
        assertEquals(6, genderEnumType.validate(ipsProject).getNoOfMessages());

        // Test working for toggling unique property.
        newUnique.setUnique(false);
        getIpsModel().clearValidationCache();
        assertEquals(4, genderEnumType.validate(ipsProject).getNoOfMessages());
        newUnique.setUnique(true);
        getIpsModel().clearValidationCache();
        assertEquals(6, genderEnumType.validate(ipsProject).getNoOfMessages());
        testValue2.setEnumAttributeValue(0, "id2");
        testValue2.setEnumAttributeValue(1, "name2");
        testValue2.setEnumAttributeValue(2, "otherUniqueValue");

        // Test working for enum attribute movement.
        genderEnumType.moveEnumAttribute(newUnique, true);
        testValue2.setEnumAttributeValue(2, "name1");
        getIpsModel().clearValidationCache();
        assertEquals(2, genderEnumType.validate(ipsProject).getNoOfMessages());

        // Test working for enum attribute deletion.
        genderEnumType.deleteEnumAttributeWithValues(genderEnumAttributeName);
        newUnique.setUsedAsNameInFaktorIpsUi(true);
        testValue2.setEnumAttributeValue(1, "newUniqueValue");
        getIpsModel().clearValidationCache();
        assertEquals(2, genderEnumType.validate(ipsProject).getNoOfMessages());
    }

    public void testDeleteEnumValues() {
        assertFalse(genderEnumContent.deleteEnumValues(null));
        assertTrue(genderEnumContent.deleteEnumValues(genderEnumContent.getEnumValues()));
        assertEquals(0, genderEnumContent.getEnumValuesCount());

        try {
            genderEnumContent.deleteEnumValues(paymentMode.getEnumValues());
            fail();
        } catch (NoSuchElementException e) {
        }
    }

    public void _testUniqueIdentifierValidationPerformance() throws CoreException {
        IEnumType hugeEnumType = newEnumType(ipsProject, "HugeEnumType");
        hugeEnumType.setContainingValues(true);
        IEnumAttribute idAttribute = hugeEnumType.newEnumAttribute();
        idAttribute.setName("id");
        idAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        idAttribute.setIdentifier(true);
        idAttribute.setUnique(true);
        IEnumAttribute nameAttribute = hugeEnumType.newEnumAttribute();
        nameAttribute.setName("name");
        nameAttribute.setUsedAsNameInFaktorIpsUi(true);
        nameAttribute.setLiteralName(true);
        nameAttribute.setUnique(true);

        // Create 15 further enum attributes, each 2nd one a unique.
        for (int i = 1; i <= 15; i++) {
            IEnumAttribute enumAttribute = hugeEnumType.newEnumAttribute();
            enumAttribute.setName("testAttribute" + i);
            enumAttribute.setDatatype(Datatype.STRING.getQualifiedName());
            enumAttribute.setUnique(i % 2 == 0);
        }

        // Create 7500 enum values all having the same values.
        System.out.println("Creating enum values ...");
        for (int i = 1; i <= 7500; i++) {
            IEnumValue enumValue = hugeEnumType.newEnumValue();
            for (int j = 0; j < 17; j++) {
                enumValue.setEnumAttributeValue(j, "value" + j);
            }
            System.out.println("Enum value # " + i + " created.");
        }

        // Perform first validation.
        System.out.println("First validation starts, timing ...");
        long millisBefore = System.currentTimeMillis();
        hugeEnumType.validate(ipsProject);
        long millisDifference = System.currentTimeMillis() - millisBefore;
        System.out.println("First validation took " + millisDifference / 1000 + " seconds.");

        // Perform second validation.
        getIpsModel().clearValidationCache();
        System.out.println("Second validation starts, timing ...");
        millisBefore = System.currentTimeMillis();
        hugeEnumType.validate(ipsProject);
        millisDifference = System.currentTimeMillis() - millisBefore;
        System.out.println("Second validation took " + millisDifference / 1000 + " seconds.");
    }

}
