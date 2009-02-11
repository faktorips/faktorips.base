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

package org.faktorips.devtools.core.internal.model.enumtype;

import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.enumtype.IEnumAttribute;
import org.faktorips.devtools.core.model.enumtype.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enumtype.IEnumContent;
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.model.enumtype.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.w3c.dom.Element;

public class EnumTypeTest extends AbstractIpsEnumPluginTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testGetSetSuperEnumType() {
        assertEquals("", genderEnumType.getSuperEnumType());
        genderEnumType.setSuperEnumType("OtherEnumTypeName");
        assertEquals("OtherEnumTypeName", genderEnumType.getSuperEnumType());

        try {
            genderEnumType.setSuperEnumType(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testGetSetAbstract() {
        assertFalse(genderEnumType.isAbstract());
        genderEnumType.setAbstract(true);
        assertTrue(genderEnumType.isAbstract());
    }

    public void testGetSetValuesPartOfModel() {
        assertFalse(genderEnumType.getValuesArePartOfModel());
        genderEnumType.setValuesArePartOfModel(true);
        assertTrue(genderEnumType.getValuesArePartOfModel());
    }

    public void testGetIpsObjectType() {
        assertEquals(IpsObjectType.ENUM_TYPE, genderEnumType.getIpsObjectType());
    }

    public void testGetEnumAttributes() {
        List<IEnumAttribute> attributes = genderEnumType.getEnumAttributes();
        assertEquals(2, attributes.size());
        assertEquals(GENDER_ENUM_ATTRIBUTE_ID_NAME, attributes.get(0).getName());
        assertEquals(GENDER_ENUM_ATTRIBUTE_NAME_NAME, attributes.get(1).getName());
    }

    public void testGetEnumAttributeById() {
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttribute(0));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttribute(1));
    }

    public void testGetEnumAttributeByName() {
        try {
            genderEnumType.getEnumAttribute(null);
            fail();
        } catch (NullPointerException e) {
        }

        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttribute(GENDER_ENUM_ATTRIBUTE_ID_NAME));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttribute(GENDER_ENUM_ATTRIBUTE_NAME_NAME));
    }

    public void testGetNumberEnumAttributes() {
        assertEquals(2, genderEnumType.getNumberEnumAttributes());
    }

    public void testGetChildren() throws CoreException {
        assertEquals(2, genderEnumType.getChildren().length);
    }

    public void testNewEnumAttribute() throws CoreException {
        IEnumAttribute description = genderEnumType.newEnumAttribute();
        description.setName("Description");
        description.setDatatype(STRING_DATATYPE_NAME);

        List<IEnumAttribute> attributes = genderEnumType.getEnumAttributes();
        assertEquals(3, attributes.size());
        assertEquals("Description", attributes.get(2).getName());

        List<IEnumAttributeValue> maleEnumAttributeValues = genderEnumValueMale.getEnumAttributeValues();
        List<IEnumAttributeValue> femaleEnumAttributeValues = genderEnumValueFemale.getEnumAttributeValues();
        assertEquals(3, maleEnumAttributeValues.size());
        assertEquals(description, maleEnumAttributeValues.get(2).getEnumAttribute());
        assertEquals(3, femaleEnumAttributeValues.size());
        assertEquals(description, femaleEnumAttributeValues.get(2).getEnumAttribute());
    }

    public void testFindEnumType() throws CoreException {
        assertEquals(genderEnumType, genderEnumType.findEnumType());
    }

    public void testMoveEnumAttributeUp() throws CoreException {
        try {
            genderEnumType.moveEnumAttributeUp(null);
            fail();
        } catch (NullPointerException e) {
        }

        IEnumAttribute newEnumAttribute = genderEnumType.newEnumAttribute();
        IEnumAttributeValue valueId = genderEnumValueMale.getEnumAttributeValue(0);
        IEnumAttributeValue valueName = genderEnumValueMale.getEnumAttributeValue(1);
        IEnumAttributeValue valueNew = genderEnumValueMale.getEnumAttributeValue(2);

        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(0));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(1));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(1));
        assertEquals(valueNew, genderEnumValueMale.getEnumAttributeValues().get(2));

        int newIndex;
        newIndex = genderEnumType.moveEnumAttributeUp(newEnumAttribute);
        assertEquals(1, newIndex);
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(0));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(1));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueNew, genderEnumValueMale.getEnumAttributeValues().get(1));
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(2));

        newIndex = genderEnumType.moveEnumAttributeUp(newEnumAttribute);
        assertEquals(0, newIndex);
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(1));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueNew, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(1));
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(2));

        // Nothing must change if the enum attribute is the first one already
        newIndex = genderEnumType.moveEnumAttributeUp(newEnumAttribute);
        assertEquals(0, newIndex);
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(1));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueNew, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(1));
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(2));
    }

    public void testMoveEnumAttributeUpValuesPartOfModel() throws CoreException {
        IEnumAttributeValue valueId = genderEnumValueMale.getEnumAttributeValue(0);
        IEnumAttributeValue valueName = genderEnumValueMale.getEnumAttributeValue(1);

        genderEnumType.setValuesArePartOfModel(true);
        genderEnumType.moveEnumAttributeUp(genderEnumAttributeName);

        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(1));

        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(1));
    }

    public void testMoveEnumAttributeDown() throws CoreException {
        try {
            genderEnumType.moveEnumAttributeDown(null);
            fail();
        } catch (NullPointerException e) {
        }

        IEnumAttribute newEnumAttribute = genderEnumType.newEnumAttribute();
        IEnumAttributeValue valueId = genderEnumValueMale.getEnumAttributeValue(0);
        IEnumAttributeValue valueName = genderEnumValueMale.getEnumAttributeValue(1);
        IEnumAttributeValue valueNew = genderEnumValueMale.getEnumAttributeValue(2);

        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(0));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(1));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(1));
        assertEquals(valueNew, genderEnumValueMale.getEnumAttributeValues().get(2));

        int newIndex;
        newIndex = genderEnumType.moveEnumAttributeDown(genderEnumAttributeId);
        assertEquals(1, newIndex);
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(1));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(1));
        assertEquals(valueNew, genderEnumValueMale.getEnumAttributeValues().get(2));

        newIndex = genderEnumType.moveEnumAttributeDown(genderEnumAttributeId);
        assertEquals(2, newIndex);
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(0));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(1));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueNew, genderEnumValueMale.getEnumAttributeValues().get(1));
        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(2));

        // Nothing must change if the enum attribute is the last one already
        newIndex = genderEnumType.moveEnumAttributeDown(genderEnumAttributeId);
        assertEquals(2, newIndex);
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(0));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(1));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueNew, genderEnumValueMale.getEnumAttributeValues().get(1));
        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(2));
    }

    public void testMoveEnumAttributeDownValuesPartOfModel() throws CoreException {
        IEnumAttributeValue valueId = genderEnumValueMale.getEnumAttributeValue(0);
        IEnumAttributeValue valueName = genderEnumValueMale.getEnumAttributeValue(1);

        genderEnumType.setValuesArePartOfModel(true);
        genderEnumType.moveEnumAttributeDown(genderEnumAttributeId);

        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(1));

        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(1));
    }

    public void testFindReferencingEnumContents() throws CoreException {
        newEnumContent(ipsProject, "NotReferencingEnumValues");

        List<IEnumContent> referencingEnumContents = genderEnumType.findReferencingEnumContents();
        assertEquals(1, referencingEnumContents.size());
        assertEquals(genderEnumContent, referencingEnumContents.get(0));
    }

    public void testXml() throws CoreException, ParserConfigurationException {
        IEnumType newEnumType = newEnumType(ipsProject, "NewEnumType");
        newEnumType.setAbstract(true);
        newEnumType.setValuesArePartOfModel(true);
        newEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        newEnumType.newEnumAttribute();

        Element xmlElement = newEnumType.toXml(createXmlDocument(IEnumType.XML_TAG));
        assertTrue(Boolean.parseBoolean(xmlElement.getAttribute(IEnumType.PROPERTY_ABSTRACT)));
        assertTrue(Boolean.parseBoolean(xmlElement.getAttribute(IEnumType.PROPERTY_VALUES_ARE_PART_OF_MODEL)));
        assertEquals(genderEnumType.getQualifiedName(), xmlElement.getAttribute(IEnumType.PROPERTY_SUPERTYPE));

        IEnumType loadedEnumType = newEnumType(ipsProject, "LoadedEnumType");
        loadedEnumType.initFromXml(xmlElement);
        assertTrue(loadedEnumType.isAbstract());
        assertTrue(loadedEnumType.getValuesArePartOfModel());
        assertEquals(genderEnumType.getQualifiedName(), loadedEnumType.getSuperEnumType());
    }

    public void testDeleteEnumAttributeWithValues() throws CoreException {
        try {
            genderEnumType.deleteEnumAttributeWithValues(99);
            fail();
        } catch (NoSuchElementException e) {
        }

        IEnumValue modelValue = genderEnumType.newEnumValue();

        genderEnumType.deleteEnumAttributeWithValues(0);
        List<IEnumAttribute> enumAttributes = genderEnumType.getEnumAttributes();
        assertEquals(1, enumAttributes.size());
        assertEquals(genderEnumAttributeName, enumAttributes.get(0));
        List<IEnumAttributeValue> enumAttributeValues = genderEnumValueMale.getEnumAttributeValues();
        assertEquals(1, enumAttributeValues.size());
        assertEquals(GENDER_ENUM_LITERAL_MALE_NAME, enumAttributeValues.get(0).getValue());
        assertEquals(1, modelValue.getEnumAttributeValues().size());

        try {
            genderEnumType.deleteEnumAttributeWithValues(null);
        } catch (NullPointerException e) {
        }

        try {
            genderEnumType.deleteEnumAttributeWithValues(genderEnumAttributeId);
            fail();
        } catch (NoSuchElementException e) {
        }

        genderEnumType.deleteEnumAttributeWithValues(genderEnumAttributeName);
        assertEquals(0, genderEnumType.getEnumAttributes().size());
        assertEquals(0, genderEnumValueMale.getEnumAttributeValues().size());
        assertEquals(0, modelValue.getEnumAttributeValues().size());
    }

    public void testGetJavaClassName() {
        try {
            genderEnumType.getJavaClassName();
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testHasNullObject() {
        assertFalse(genderEnumType.hasNullObject());
    }

    public void testIsPrimitive() {
        assertFalse(genderEnumType.isPrimitive());
    }

    public void testIsValueDatatype() {
        assertFalse(genderEnumType.isValueDatatype());
    }

    public void testIsVoid() {
        assertFalse(genderEnumType.isVoid());
    }

    @SuppressWarnings("unchecked")
    public void testCompareTo() {
        assertEquals(0, genderEnumType.compareTo(new Object()));
    }

    public void testEnumAttributeExists() {
        try {
            genderEnumType.enumAttributeExists(null);
            fail();
        } catch (NullPointerException e) {
        }

        assertTrue(genderEnumType.enumAttributeExists(GENDER_ENUM_ATTRIBUTE_ID_NAME));
        assertFalse(genderEnumType.enumAttributeExists("FooBar"));
    }

    public void testValidateThis() throws CoreException {
        assertTrue(genderEnumType.isValid());

        getIpsModel().clearValidationCache();
        genderEnumType.setSuperEnumType("FooBar");
        assertEquals(1, genderEnumType.validate(ipsProject).getNoOfMessages());
    }

    public void testFindSuperEnumType() throws CoreException {
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        assertEquals(genderEnumType, subEnumType.findSuperEnumType());
    }

}
