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

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.enumtype.IEnumAttribute;
import org.faktorips.devtools.core.model.enumtype.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enumtype.IEnumContent;
import org.faktorips.devtools.core.model.enumtype.IEnumValue;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EnumAttributeValueTest extends AbstractIpsEnumPluginTest {

    private IEnumAttributeValue maleIdAttributeValue;
    private IEnumAttributeValue maleNameAttributeValue;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        maleIdAttributeValue = genderEnumValueMale.getEnumAttributeValue(0);
        maleNameAttributeValue = genderEnumValueMale.getEnumAttributeValue(1);
    }

    public void testFindEnumAttribute() throws CoreException {
        assertEquals(genderEnumAttributeId, maleIdAttributeValue.findEnumAttribute());
        assertEquals(genderEnumAttributeName, maleNameAttributeValue.findEnumAttribute());

        genderEnumContent.setEnumType("");
        assertNull(maleIdAttributeValue.findEnumAttribute());
    }

    public void testGetSetValue() {
        maleIdAttributeValue.setValue("otherValue");
        assertEquals("otherValue", maleIdAttributeValue.getValue());
    }

    public void testXml() throws ParserConfigurationException, CoreException {
        Element xmlElement = genderEnumContent.toXml(createXmlDocument(IEnumContent.XML_TAG));
        // Get first enum attribute value of the first enum value
        Node firstEnumAttributeValue = xmlElement.getChildNodes().item(1).getChildNodes().item(1);
        assertEquals(GENDER_ENUM_LITERAL_MALE_ID, firstEnumAttributeValue.getTextContent());
        assertEquals(1 + 2, xmlElement.getChildNodes().getLength());

        IEnumContent loadedEnumContent = newEnumContent(ipsProject, "LoadedEnumValues");
        loadedEnumContent.initFromXml(xmlElement);
        assertEquals(GENDER_ENUM_LITERAL_MALE_ID, loadedEnumContent.getEnumValues().get(0).getEnumAttributeValue(0)
                .getValue());
        assertEquals(2, loadedEnumContent.getEnumValues().size());
    }

    public void testValidateThis() throws CoreException {
        IEnumAttribute stringAttribute = genderEnumType.newEnumAttribute();
        stringAttribute.setDatatype(STRING_DATATYPE_NAME);
        stringAttribute.setName("StringAttribute");

        IEnumAttribute integerAttribute = genderEnumType.newEnumAttribute();
        integerAttribute.setDatatype(INTEGER_DATATYPE_NAME);
        integerAttribute.setName("IntegerAttribute");

        IEnumAttribute booleanAttribute = genderEnumType.newEnumAttribute();
        booleanAttribute.setDatatype(BOOLEAN_DATATYPE_NAME);
        booleanAttribute.setName("BooleanAttribute");

        genderEnumType.setValuesArePartOfModel(true);
        IEnumValue newEnumValue = genderEnumType.newEnumValue();

        IEnumAttributeValue stringNewAttributeValue = newEnumValue.getEnumAttributeValues().get(2);
        IEnumAttributeValue integerNewAttributeValue = newEnumValue.getEnumAttributeValues().get(3);
        IEnumAttributeValue booleanNewAttributeValue = newEnumValue.getEnumAttributeValues().get(4);

        stringNewAttributeValue.setValue("String");
        integerNewAttributeValue.setValue("4");
        booleanNewAttributeValue.setValue("false");

        assertTrue(stringNewAttributeValue.isValid());
        assertTrue(integerNewAttributeValue.isValid());
        assertTrue(booleanNewAttributeValue.isValid());

        IIpsModel ipsModel = getIpsModel();

        ipsModel.clearValidationCache();
        integerNewAttributeValue.setValue("fooBar");
        assertEquals(1, integerNewAttributeValue.validate(ipsProject).getNoOfMessages());
        integerNewAttributeValue.setValue("4");

        ipsModel.clearValidationCache();
        booleanNewAttributeValue.setValue("fooBar");
        assertEquals(1, booleanNewAttributeValue.validate(ipsProject).getNoOfMessages());
        booleanNewAttributeValue.setValue("false");
    }

    public void testGetImage() {
        assertNull(genderEnumValueMale.getEnumAttributeValues().get(0).getImage());
    }

}
