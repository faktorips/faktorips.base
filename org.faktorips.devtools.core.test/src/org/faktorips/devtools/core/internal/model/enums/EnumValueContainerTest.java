/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsEnumPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.value.ValueFactory;
import org.junit.Ignore;
import org.junit.Test;

public class EnumValueContainerTest extends AbstractIpsEnumPluginTest {

    @Test
    public void testGetEnumValues() {
        assertEquals(2, genderEnumContent.getEnumValues().size());
    }

    @Test
    public void testNewEnumValue() throws CoreException {
        IEnumAttribute integerAttribute = genderEnumType.newEnumAttribute();
        integerAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        integerAttribute.setName("integerAttribute");

        for (IEnumValue currentEnumValue : genderEnumContent.getEnumValues()) {
            IEnumAttributeValue value = currentEnumValue.newEnumAttributeValue();
            value.setValue(null);
        }

        contentsChangeCounter.reset();
        IEnumValue newEnumValue = genderEnumContent.newEnumValue();
        assertEquals(1, contentsChangeCounter.getCounts());
        assertEquals(newEnumValue, genderEnumContent.getEnumValues().get(2));
        assertEquals(3, newEnumValue.getEnumAttributeValuesCount());
        assertNotNull(newEnumValue.getEnumAttributeValues().get(2).getValue());

        genderEnumContent.setEnumType("foo");
        assertNull(genderEnumContent.newEnumValue());
    }

    @Test
    public void testGetEnumValuesCount() {
        assertEquals(2, genderEnumContent.getEnumValuesCount());
        assertEquals(0, genderEnumType.getEnumValuesCount());
    }

    @Test
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
        contentsChangeCounter.reset();
        newIndizes = genderEnumContent.moveEnumValues(moveList, true);
        assertEquals(1, contentsChangeCounter.getCounts());
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

    @Test
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

        // Nothing must change if the EnumValue is the last one already.
        newIndizes = genderEnumContent.moveEnumValues(moveList, false);
        assertEquals(2, newIndizes[0]);
        assertEquals(genderEnumValueFemale, genderEnumContent.getEnumValues().get(0));
        assertEquals(newEnumValue, genderEnumContent.getEnumValues().get(1));
        assertEquals(genderEnumValueMale, genderEnumContent.getEnumValues().get(2));
    }

    @Test
    public void testGetIndexOfEnumValue() throws CoreException {
        assertEquals(0, genderEnumContent.getIndexOfEnumValue(genderEnumValueMale));
        assertEquals(1, genderEnumContent.getIndexOfEnumValue(genderEnumValueFemale));

        List<IEnumValue> moveList = new ArrayList<IEnumValue>(1);
        moveList.add(genderEnumValueFemale);
        genderEnumContent.moveEnumValues(moveList, true);
        assertEquals(1, genderEnumContent.getIndexOfEnumValue(genderEnumValueMale));
        assertEquals(0, genderEnumContent.getIndexOfEnumValue(genderEnumValueFemale));

        assertEquals(-1, genderEnumContent.getIndexOfEnumValue(paymentMode.getEnumValues().get(0)));
    }

    @Test
    public void testClear() {
        genderEnumContent.clear();
        assertEquals(0, genderEnumContent.getEnumValuesCount());
    }

    @Test
    public void testUniqueIdentifierValidation() throws CoreException {
        assertEquals(0, paymentMode.validate(ipsProject).size());
        IEnumValue testValue1 = paymentMode.getEnumValues().get(0);
        IEnumValue testValue2 = paymentMode.getEnumValues().get(1);

        // Test working after new unique attribute addition.
        IEnumAttribute newUnique = paymentMode.newEnumAttribute();
        newUnique.setDatatype(Datatype.STRING.getQualifiedName());
        newUnique.setName("newUnique");
        newUnique.setUnique(true);
        testValue1.setEnumAttributeValue(newUnique, ValueFactory.createStringValue("newUniqueValue"));
        testValue2.setEnumAttributeValue(newUnique, ValueFactory.createStringValue("newUniqueValue"));
        getIpsModel().clearValidationCache();
        assertEquals(2, paymentMode.validate(ipsProject).size());

        // Test working for EnumValue deletion.
        testValue2.delete();
        getIpsModel().clearValidationCache();
        assertEquals(0, paymentMode.validate(ipsProject).size());

        // Test working for EnumValue addition.
        testValue2 = paymentMode.newEnumValue();
        testValue2.setEnumAttributeValue(0, ValueFactory.createStringValue("MONTHLY"));
        testValue2.setEnumAttributeValue(1, ValueFactory.createStringValue("P1"));
        testValue2.setEnumAttributeValue(2, ValueFactory.createStringValue("monthly"));
        testValue2.setEnumAttributeValue(3, ValueFactory.createStringValue("newUniqueValue"));
        getIpsModel().clearValidationCache();
        assertEquals(8, paymentMode.validate(ipsProject).size());

        // Test working for toggling unique property.
        newUnique.setUnique(false);
        getIpsModel().clearValidationCache();
        assertEquals(6, paymentMode.validate(ipsProject).size());
        newUnique.setUnique(true);
        getIpsModel().clearValidationCache();
        assertEquals(8, paymentMode.validate(ipsProject).size());
        testValue2.setEnumAttributeValue(0, ValueFactory.createStringValue("ANNUALLY"));
        testValue2.setEnumAttributeValue(1, ValueFactory.createStringValue("P2"));
        testValue2.setEnumAttributeValue(2, ValueFactory.createStringValue("annually"));
        testValue2.setEnumAttributeValue(3, ValueFactory.createStringValue("otherUniqueValue"));

        // Test working for EnumAttribute movement.
        paymentMode.moveEnumAttribute(newUnique, true);
        testValue2.setEnumAttributeValue(3, ValueFactory.createStringValue("monthly"));
        getIpsModel().clearValidationCache();
        assertEquals(2, paymentMode.validate(ipsProject).size());

        // Test working for EnumAttribute deletion.
        newUnique.delete();
        getIpsModel().clearValidationCache();
        assertEquals(2, paymentMode.validate(ipsProject).size());
    }

    @Test
    public void testDeleteEnumValues() {
        assertFalse(genderEnumContent.deleteEnumValues(paymentMode.getEnumValues()));
        assertFalse(genderEnumContent.deleteEnumValues(null));
        assertTrue(genderEnumContent.deleteEnumValues(genderEnumContent.getEnumValues()));
        assertEquals(0, genderEnumContent.getEnumValuesCount());
    }

    @Test
    @Ignore("Only performance messure, no real test")
    public void testUniqueIdentifierValidationPerformance() throws CoreException {
        IEnumType hugeEnumType = newEnumType(ipsProject, "HugeEnumType");
        hugeEnumType.setExtensible(false);
        IEnumLiteralNameAttribute literalNameAttribute = hugeEnumType.newEnumLiteralNameAttribute();
        literalNameAttribute.setDefaultValueProviderAttribute("id");
        IEnumAttribute idAttribute = hugeEnumType.newEnumAttribute();
        idAttribute.setName("id");
        idAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        idAttribute.setIdentifier(true);
        idAttribute.setUnique(true);
        IEnumAttribute nameAttribute = hugeEnumType.newEnumAttribute();
        nameAttribute.setName("name");
        nameAttribute.setUsedAsNameInFaktorIpsUi(true);
        nameAttribute.setUnique(true);

        // Create 15 further EnumAttributes, each 2nd one a unique.
        for (int i = 1; i <= 15; i++) {
            IEnumAttribute enumAttribute = hugeEnumType.newEnumAttribute();
            enumAttribute.setName("testAttribute" + i);
            enumAttribute.setDatatype(Datatype.STRING.getQualifiedName());
            enumAttribute.setUnique(i % 2 == 0);
        }

        // Create 7500 EnumValues all having the same values.
        System.out.println("Creating enum values ...");
        for (int i = 1; i <= 7500; i++) {
            IEnumValue enumValue = hugeEnumType.newEnumValue();
            for (int j = 0; j < 18; j++) {
                enumValue.setEnumAttributeValue(j, ValueFactory.createStringValue("value" + j));
            }
            System.out.println("Enum value # " + i + " created.");
        }

        // Perform first validation.
        System.out.println("First validation starts, timing ...");
        long millisBefore = System.currentTimeMillis();
        hugeEnumType.validate(ipsProject);
        long millisDifference = System.currentTimeMillis() - millisBefore;
        System.out.println("First validation took " + millisDifference / 1000f + " seconds.");

        // Perform second validation.
        getIpsModel().clearValidationCache();
        System.out.println("Second validation starts, timing ...");
        millisBefore = System.currentTimeMillis();
        hugeEnumType.validate(ipsProject);
        millisDifference = System.currentTimeMillis() - millisBefore;
        System.out.println("Second validation took " + millisDifference / 1000f + " seconds.");
    }

    @Test
    public void testFindEnumValue() throws CoreException {
        assertNull(paymentMode.findEnumValue(null, ipsProject));

        assertNotNull(paymentMode.findEnumValue("P1", ipsProject));
        assertNotNull(paymentMode.findEnumValue("P2", ipsProject));
        assertNull(paymentMode.findEnumValue("P3", ipsProject));

        IEnumValue newEnumValue = paymentMode.newEnumValue();
        newEnumValue.setEnumAttributeValue(0, ValueFactory.createStringValue("NEW"));
        newEnumValue.setEnumAttributeValue(1, ValueFactory.createStringValue("P4"));
        newEnumValue.setEnumAttributeValue(2, ValueFactory.createStringValue("new"));
        assertNotNull(paymentMode.findEnumValue("P4", ipsProject));
    }

    @Test
    public void testFindEnumValue_idChanged() throws CoreException {
        // verify cache is initialized
        assertNotNull(genderEnumContent.findEnumValue(GENDER_ENUM_LITERAL_MALE_ID, ipsProject));

        genderEnumContent.findEnumValue(GENDER_ENUM_LITERAL_MALE_ID, ipsProject)
                .getEnumAttributeValue(genderEnumAttributeId).setValue(ValueFactory.createStringValue("x"));

        assertNull(genderEnumContent.findEnumValue(GENDER_ENUM_LITERAL_MALE_ID, ipsProject));
    }

    @Test
    public void testFindEnumValue_ForContentInType() throws CoreException {
        IEnumValue newEnumValue = genderEnumType.newEnumValue();
        newEnumValue.setEnumAttributeValue(1, ValueFactory.createStringValue("test1"));
        assertNotNull(genderEnumContent.findEnumValue("test1", ipsProject));
    }

    @Test
    public void testFindEnumValueIdentifierUsedTwice() throws CoreException {
        IEnumValue enumValue1 = paymentMode.getEnumValues().get(0);
        IEnumValue enumValue2 = paymentMode.getEnumValues().get(1);
        enumValue1.getEnumAttributeValues().get(1).setValue(ValueFactory.createStringValue("Identifier"));
        enumValue2.getEnumAttributeValues().get(1).setValue(ValueFactory.createStringValue("Identifier"));

        assertEquals(enumValue2, paymentMode.findEnumValue("Identifier", ipsProject));

        paymentMode.deleteEnumValues(Arrays.asList(enumValue2));
        assertEquals(enumValue1, paymentMode.findEnumValue("Identifier", ipsProject));
    }

    @Test
    public void testFindEnumValueIdentifierChanged() throws CoreException {
        IEnumValue enumValue = paymentMode.getEnumValues().get(0);

        assertEquals(enumValue, paymentMode.findEnumValue("P1", ipsProject));

        enumValue.getEnumAttributeValues().get(1).setValue(ValueFactory.createStringValue("ChangedIdentifier"));
        assertEquals(enumValue, paymentMode.findEnumValue("ChangedIdentifier", ipsProject));
    }

    @Test
    public void testFindEnumValueIdentifierAttributeChanged() throws CoreException {
        IEnumAttribute id = paymentMode.getEnumAttribute("id");
        id.setIdentifier(false);

        IEnumAttribute name = paymentMode.getEnumAttribute("name");
        name.setIdentifier(true);

        assertEquals(paymentMode.getEnumValues().get(0), paymentMode.findEnumValue("monthly", ipsProject));
    }

}
