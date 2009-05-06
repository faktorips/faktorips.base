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

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.ipsobject.DescriptionHelper;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

public class EnumAttributeValueTest extends AbstractIpsEnumPluginTest {

    private IEnumAttributeValue maleIdAttributeValue;
    private IEnumAttributeValue maleNameAttributeValue;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        maleIdAttributeValue = genderEnumValueMale.getEnumAttributeValues().get(0);
        maleNameAttributeValue = genderEnumValueMale.getEnumAttributeValues().get(1);
    }

    public void testFindEnumAttribute() throws CoreException {
        try {
            maleIdAttributeValue.findEnumAttribute(null);
            fail();
        } catch (NullPointerException e) {
        }

        assertEquals(genderEnumAttributeId, maleIdAttributeValue.findEnumAttribute(ipsProject));
        assertEquals(genderEnumAttributeName, maleNameAttributeValue.findEnumAttribute(ipsProject));

        genderEnumContent.setEnumType("");
        assertNull(maleIdAttributeValue.findEnumAttribute(ipsProject));
        genderEnumContent.setEnumType(genderEnumType.getQualifiedName());

        genderEnumType.deleteEnumAttributeWithValues(genderEnumAttributeId);
        assertNull(maleIdAttributeValue.findEnumAttribute(ipsProject));
    }

    public void testGetSetValue() {
        maleIdAttributeValue.setValue("otherValue");
        assertEquals("otherValue", maleIdAttributeValue.getValue());
    }

    public void testXml() throws ParserConfigurationException, CoreException {
        Element xmlElement = genderEnumContent.toXml(createXmlDocument(IEnumContent.XML_TAG));
        // Get first enum attribute value of the first enum value
        Element firstEnumValue = XmlUtil.getFirstElement(xmlElement, IEnumValue.XML_TAG);
        Element descriptionElement = XmlUtil.getFirstElement(firstEnumValue, DescriptionHelper.XML_ELEMENT_NAME);
        assertNull(descriptionElement);

        Element firstValue = XmlUtil.getFirstElement(firstEnumValue, IEnumAttributeValue.XML_TAG);
        assertEquals(GENDER_ENUM_LITERAL_MALE_ID, firstValue.getTextContent());
        assertEquals(1 + 2, xmlElement.getChildNodes().getLength());

        IEnumContent loadedEnumContent = newEnumContent(ipsProject, "LoadedEnumValues");
        loadedEnumContent.initFromXml(xmlElement);
        assertEquals(GENDER_ENUM_LITERAL_MALE_ID, loadedEnumContent.getEnumValues().get(0).getEnumAttributeValues()
                .get(0).getValue());
        assertEquals(2, loadedEnumContent.getEnumValues().size());
    }

    public void testValidateParsable() throws CoreException {
        IEnumAttribute stringAttribute = genderEnumType.newEnumAttribute();
        stringAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        stringAttribute.setName("StringAttribute");

        IEnumAttribute integerAttribute = genderEnumType.newEnumAttribute();
        integerAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        integerAttribute.setName("IntegerAttribute");

        IEnumAttribute booleanAttribute = genderEnumType.newEnumAttribute();
        booleanAttribute.setDatatype(Datatype.BOOLEAN.getQualifiedName());
        booleanAttribute.setName("BooleanAttribute");

        genderEnumType.setContainingValues(true);
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

        // Test value parsable with datatype integer
        ipsModel.clearValidationCache();
        integerNewAttributeValue.setValue("fooBar");
        MessageList validationMessageList = integerNewAttributeValue.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_VALUE_NOT_PARSABLE));
        integerNewAttributeValue.setValue("4");

        // Test value parsable with datatype boolean
        ipsModel.clearValidationCache();
        booleanNewAttributeValue.setValue("fooBar");
        validationMessageList = booleanNewAttributeValue.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_VALUE_NOT_PARSABLE));
        booleanNewAttributeValue.setValue("false");
    }

    public void testValidateUniqueIdentifierValueEmpty() throws CoreException {
        IIpsModel ipsModel = getIpsModel();
        IEnumAttributeValue uniqueIdentifierEnumAttributeValue = genderEnumValueFemale.getEnumAttributeValues().get(0);

        uniqueIdentifierEnumAttributeValue.setValue("");
        MessageList validationMessageList = genderEnumValueFemale.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_VALUE_EMPTY));

        ipsModel.clearValidationCache();
        uniqueIdentifierEnumAttributeValue.setValue(null);
        validationMessageList = genderEnumValueFemale.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_VALUE_EMPTY));
    }

    public void testValidateUniqueIdentifierValueNotUnique() throws CoreException {
        IEnumAttributeValue uniqueIdentifierEnumAttributeValueMale = genderEnumValueMale.getEnumAttributeValues()
                .get(0);
        IEnumAttributeValue uniqueIdentifierEnumAttributeValueFemale = genderEnumValueFemale.getEnumAttributeValues()
                .get(0);

        uniqueIdentifierEnumAttributeValueMale.setValue("foo");
        uniqueIdentifierEnumAttributeValueFemale.setValue("foo");

        MessageList validationMessageList = uniqueIdentifierEnumAttributeValueMale.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_NOT_UNIQUE));

        validationMessageList = uniqueIdentifierEnumAttributeValueFemale.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_NOT_UNIQUE));
    }

    public void testValidateLiteralNameValueNotJavaConform() throws CoreException {
        IEnumAttributeValue literalNameEnumAttributeValueMale = genderEnumValueMale.getEnumAttributeValues().get(0);
        literalNameEnumAttributeValueMale.setValue("3sdj4%332§4^2");
        MessageList validationMessageList = literalNameEnumAttributeValueMale.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_VALUE_LITERAL_NAME_NOT_JAVA_CONFORM));
    }

    public void testGetImage() {
        assertNull(genderEnumValueMale.getEnumAttributeValues().get(0).getImage());
    }

    public void testGetEnumValue() {
        assertEquals(genderEnumValueMale, genderEnumValueMale.getEnumAttributeValues().get(0).getEnumValue());
    }

}
