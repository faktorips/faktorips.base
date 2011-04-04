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

package org.faktorips.devtools.core.internal.model.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
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
        assertNull(newEnumValue.getEnumAttributeValues().get(2).getValue());

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
        testValue1.setEnumAttributeValue(newUnique, "newUniqueValue");
        testValue2.setEnumAttributeValue(newUnique, "newUniqueValue");
        getIpsModel().clearValidationCache();
        assertEquals(2, paymentMode.validate(ipsProject).size());

        // Test working for EnumValue deletion.
        testValue2.delete();
        getIpsModel().clearValidationCache();
        assertEquals(0, paymentMode.validate(ipsProject).size());

        // Test working for EnumValue addition.
        testValue2 = paymentMode.newEnumValue();
        testValue2.setEnumAttributeValue(0, "MONTHLY");
        testValue2.setEnumAttributeValue(1, "P1");
        testValue2.setEnumAttributeValue(2, "monthly");
        testValue2.setEnumAttributeValue(3, "newUniqueValue");
        getIpsModel().clearValidationCache();
        assertEquals(8, paymentMode.validate(ipsProject).size());

        // Test working for toggling unique property.
        newUnique.setUnique(false);
        getIpsModel().clearValidationCache();
        assertEquals(6, paymentMode.validate(ipsProject).size());
        newUnique.setUnique(true);
        getIpsModel().clearValidationCache();
        assertEquals(8, paymentMode.validate(ipsProject).size());
        testValue2.setEnumAttributeValue(0, "ANNUALLY");
        testValue2.setEnumAttributeValue(1, "P2");
        testValue2.setEnumAttributeValue(2, "annually");
        testValue2.setEnumAttributeValue(3, "otherUniqueValue");

        // Test working for EnumAttribute movement.
        paymentMode.moveEnumAttribute(newUnique, true);
        testValue2.setEnumAttributeValue(3, "monthly");
        getIpsModel().clearValidationCache();
        assertEquals(2, paymentMode.validate(ipsProject).size());

        // Test working for EnumAttribute deletion.
        paymentMode.deleteEnumAttributeWithValues(newUnique);
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

    public void _testUniqueIdentifierValidationPerformance() throws CoreException {
        IEnumType hugeEnumType = newEnumType(ipsProject, "HugeEnumType");
        hugeEnumType.setContainingValues(true);
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

    @Test
    public void testGetEnumValue() throws CoreException {
        assertNull(paymentMode.getEnumValue(null));

        assertNotNull(paymentMode.getEnumValue("P1"));
        assertNotNull(paymentMode.getEnumValue("P2"));
        assertNull(paymentMode.getEnumValue("P3"));

        IEnumValue newEnumValue = paymentMode.newEnumValue();
        newEnumValue.setEnumAttributeValue(0, "NEW");
        newEnumValue.setEnumAttributeValue(1, "P4");
        newEnumValue.setEnumAttributeValue(2, "new");
        assertNotNull(paymentMode.getEnumValue("P4"));
    }

}
