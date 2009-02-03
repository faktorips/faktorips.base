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
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.model.enumtype.IEnumValues;
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
        genderEnumType.setIsAbstract(true);
        assertTrue(genderEnumType.isAbstract());
    }

    public void testGetSetValuesPartOfModel() {
        assertFalse(genderEnumType.valuesArePartOfModel());
        genderEnumType.setValuesArePartOfModel(true);
        assertTrue(genderEnumType.valuesArePartOfModel());
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

    public void testGetEnumAttribute() {
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttribute(0));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttribute(1));
    }

    public void testGetNumberEnumAttributes() {
        assertEquals(2, genderEnumType.getNumberEnumAttributes());
    }

    public void testNewEnumAttribute() throws CoreException {
        IEnumAttribute description = genderEnumType.newEnumAttribute();
        description.setName("Description");
        description.setDatatype(STRING_DATATYPE_NAME);

        List<IEnumAttribute> attributes = genderEnumType.getEnumAttributes();
        assertEquals(3, attributes.size());
        assertEquals("Description", attributes.get(2).getName());

        List<IEnumAttributeValue> maleEnumAttributeValues = genderEnumMaleValue.getEnumAttributeValues();
        List<IEnumAttributeValue> femaleEnumAttributeValues = genderEnumFemaleValue.getEnumAttributeValues();
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
        IEnumAttributeValue valueId = genderEnumMaleValue.getEnumAttributeValue(0);
        IEnumAttributeValue valueName = genderEnumMaleValue.getEnumAttributeValue(1);
        IEnumAttributeValue valueNew = genderEnumMaleValue.getEnumAttributeValue(2);

        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(0));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(1));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueId, genderEnumMaleValue.getEnumAttributeValues().get(0));
        assertEquals(valueName, genderEnumMaleValue.getEnumAttributeValues().get(1));
        assertEquals(valueNew, genderEnumMaleValue.getEnumAttributeValues().get(2));

        genderEnumType.moveEnumAttributeUp(newEnumAttribute);
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(0));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(1));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueId, genderEnumMaleValue.getEnumAttributeValues().get(0));
        assertEquals(valueNew, genderEnumMaleValue.getEnumAttributeValues().get(1));
        assertEquals(valueName, genderEnumMaleValue.getEnumAttributeValues().get(2));

        genderEnumType.moveEnumAttributeUp(newEnumAttribute);
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(1));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueNew, genderEnumMaleValue.getEnumAttributeValues().get(0));
        assertEquals(valueId, genderEnumMaleValue.getEnumAttributeValues().get(1));
        assertEquals(valueName, genderEnumMaleValue.getEnumAttributeValues().get(2));

        // Nothing must change if the enum attribute is the first one already
        genderEnumType.moveEnumAttributeUp(newEnumAttribute);
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(1));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueNew, genderEnumMaleValue.getEnumAttributeValues().get(0));
        assertEquals(valueId, genderEnumMaleValue.getEnumAttributeValues().get(1));
        assertEquals(valueName, genderEnumMaleValue.getEnumAttributeValues().get(2));
    }

    public void testMoveEnumAttributeDown() throws CoreException {
        try {
            genderEnumType.moveEnumAttributeDown(null);
            fail();
        } catch (NullPointerException e) {
        }

        IEnumAttribute newEnumAttribute = genderEnumType.newEnumAttribute();
        IEnumAttributeValue valueId = genderEnumMaleValue.getEnumAttributeValue(0);
        IEnumAttributeValue valueName = genderEnumMaleValue.getEnumAttributeValue(1);
        IEnumAttributeValue valueNew = genderEnumMaleValue.getEnumAttributeValue(2);

        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(0));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(1));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueId, genderEnumMaleValue.getEnumAttributeValues().get(0));
        assertEquals(valueName, genderEnumMaleValue.getEnumAttributeValues().get(1));
        assertEquals(valueNew, genderEnumMaleValue.getEnumAttributeValues().get(2));

        genderEnumType.moveEnumAttributeDown(genderEnumAttributeId);
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(1));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueName, genderEnumMaleValue.getEnumAttributeValues().get(0));
        assertEquals(valueId, genderEnumMaleValue.getEnumAttributeValues().get(1));
        assertEquals(valueNew, genderEnumMaleValue.getEnumAttributeValues().get(2));

        genderEnumType.moveEnumAttributeDown(genderEnumAttributeId);
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(0));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(1));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueName, genderEnumMaleValue.getEnumAttributeValues().get(0));
        assertEquals(valueNew, genderEnumMaleValue.getEnumAttributeValues().get(1));
        assertEquals(valueId, genderEnumMaleValue.getEnumAttributeValues().get(2));

        // Nothing must change if the enum attribute is the last one already
        genderEnumType.moveEnumAttributeDown(genderEnumAttributeId);
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(0));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(1));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueName, genderEnumMaleValue.getEnumAttributeValues().get(0));
        assertEquals(valueNew, genderEnumMaleValue.getEnumAttributeValues().get(1));
        assertEquals(valueId, genderEnumMaleValue.getEnumAttributeValues().get(2));
    }

    public void testFindReferencingEnumValues() throws CoreException {
        newEnumValues(ipsProject, "NotReferencingEnumValues");

        List<IEnumValues> referencingEnumValues = genderEnumType.findReferencingEnumValues();
        assertEquals(1, referencingEnumValues.size());
        assertEquals(genderEnumValues, referencingEnumValues.get(0));
    }

    public void testXml() throws CoreException, ParserConfigurationException {
        IEnumType newEnumType = newEnumType(ipsProject, "NewEnumType");
        newEnumType.setIsAbstract(true);
        newEnumType.setValuesArePartOfModel(true);
        newEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        newEnumType.newEnumAttribute();

        Element xmlElement = newEnumType.toXml(createXmlDocument(IEnumType.XML_TAG));
        assertTrue(Boolean.parseBoolean(xmlElement.getAttribute(IEnumType.XML_ATTRIBUTE_ABSTRACT)));
        assertTrue(Boolean.parseBoolean(xmlElement.getAttribute(IEnumType.XML_ATTRIBUTE_VALUES_ARE_PART_OF_MODEL)));
        assertEquals(genderEnumType.getQualifiedName(), xmlElement.getAttribute(IEnumType.XML_ATTRIBUTE_SUPERTYPE));

        IEnumType loadedEnumType = newEnumType(ipsProject, "LoadedEnumType");
        loadedEnumType.initFromXml(xmlElement);
        assertTrue(loadedEnumType.isAbstract());
        assertTrue(loadedEnumType.valuesArePartOfModel());
        assertEquals(genderEnumType.getQualifiedName(), loadedEnumType.getSuperEnumType());
    }

    public void testDeleteEnumAttributeWithValues() throws CoreException {
        try {
            genderEnumType.deleteEnumAttributeWithValues(99);
            fail();
        } catch (NoSuchElementException e) {
        }

        genderEnumType.deleteEnumAttributeWithValues(0);
        List<IEnumAttribute> enumAttributes = genderEnumType.getEnumAttributes();
        assertEquals(1, enumAttributes.size());

        try {
            genderEnumType.deleteEnumAttributeWithValues(null);
        } catch (NullPointerException e) {
        }

        try {
            genderEnumType.deleteEnumAttributeWithValues(genderEnumAttributeId);
            fail();
        } catch (NoSuchElementException e) {
        }
    }
}
