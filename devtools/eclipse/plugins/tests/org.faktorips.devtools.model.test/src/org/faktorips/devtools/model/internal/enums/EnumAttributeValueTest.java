/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.enums;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.abstracttest.AbstractIpsEnumPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.internal.value.InternationalStringValue;
import org.faktorips.devtools.model.internal.value.StringValue;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.devtools.model.value.ValueType;
import org.faktorips.runtime.MessageList;
import org.faktorips.values.LocalizedString;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class EnumAttributeValueTest extends AbstractIpsEnumPluginTest {

    private static final String TYPE_ID = "typeId";
    private static final String CONTENT_ID = "contentId";
    private static final String PAYMENT_CONTENT = "paymentContent";
    private IEnumAttributeValue maleIdAttributeValue;
    private IEnumAttributeValue maleNameAttributeValue;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        maleIdAttributeValue = genderEnumValueMale.getEnumAttributeValues().get(0);
        maleNameAttributeValue = genderEnumValueMale.getEnumAttributeValues().get(1);
    }

    @Test(expected = NullPointerException.class)
    public void testFindEnumAttribute_failOnNull() {
        maleIdAttributeValue.findEnumAttribute(null);
    }

    @Test
    public void testFindEnumAttribute() {
        assertThat(maleIdAttributeValue.findEnumAttribute(ipsProject), is(genderEnumAttributeId));
        assertThat(maleNameAttributeValue.findEnumAttribute(ipsProject), is(genderEnumAttributeName));

        genderEnumContent.setEnumType("");
        assertThat(maleIdAttributeValue.findEnumAttribute(ipsProject), is(nullValue()));
        genderEnumContent.setEnumType(genderEnumType.getQualifiedName());

        genderEnumAttributeId.delete();
        assertThat(maleIdAttributeValue.findEnumAttribute(ipsProject), is(nullValue()));

        genderEnumAttributeName.delete();
        assertThat(maleNameAttributeValue.findEnumAttribute(ipsProject), is(nullValue()));
    }

    @Test
    public void testIsEnumLiteralNameAttributeValue() {
        assertThat(maleNameAttributeValue.isEnumLiteralNameAttributeValue(), is(false));
        IEnumValue enumValue = paymentMode.getEnumValues().get(0);
        IEnumAttributeValue literalNameValue = enumValue.getEnumAttributeValues().get(0);
        assertThat(literalNameValue.isEnumLiteralNameAttributeValue(), is(true));
        assertThat(enumValue.getEnumAttributeValues().get(1).isEnumLiteralNameAttributeValue(), is(false));
    }

    @Test
    public void testXml() throws ParserConfigurationException {
        Element xmlElement = maleIdAttributeValue.toXml(createXmlDocument(IEnumAttributeValue.XML_TAG));
        assertThat(xmlElement.getTextContent(), is(GENDER_ENUM_LITERAL_MALE_ID));

        IEnumAttributeValue loadedAttributeValue = genderEnumValueMale.newEnumAttributeValue();
        loadedAttributeValue.initFromXml(xmlElement);
        assertThat(loadedAttributeValue.getStringValue(), is(GENDER_ENUM_LITERAL_MALE_ID));
    }

    @Test
    public void testPropertiesToXml() throws Exception {
        IEnumType enumType = newEnumType(ipsProject, "AnEnum");
        IEnumAttribute enumAttr = enumType.newEnumAttribute();
        enumAttr.setDatatype(Datatype.STRING.getQualifiedName());
        enumAttr.setName("a");
        enumAttr.setIdentifier(true);
        enumAttr.setUnique(true);

        IEnumAttribute enumAttr2 = enumType.newEnumAttribute();
        enumAttr2.setDatatype(Datatype.INTEGER.getQualifiedName());
        enumAttr2.setName("b");
        enumAttr2.setUnique(true);

        IEnumValue enumValue = enumType.newEnumValue();
        List<IEnumAttributeValue> enumAttrList = enumValue.getEnumAttributeValues();
        enumAttrList.get(0).setValue(ValueFactory.createStringValue(null));
        enumAttrList.get(1).setValue(ValueFactory.createStringValue(null));

        Element enumTypeEl = enumType.toXml(createXmlDocument(IEnumContent.XML_TAG));
        IEnumType enumType2 = newEnumType(ipsProject, "AnEnum2");
        enumType2.initFromXml(enumTypeEl);
        assertThat(enumType2.getEnumValues().get(0).getEnumAttributeValues().get(0).getStringValue(), is(nullValue()));
        assertThat(enumType2.getEnumValues().get(0).getEnumAttributeValues().get(1).getStringValue(), is(nullValue()));

        List<IEnumAttributeValue> enumAttrList1 = enumValue.getEnumAttributeValues();
        enumAttrList1.get(0).setValue(ValueFactory.createStringValue("foo"));
        enumAttrList1.get(1).setValue(ValueFactory.createStringValue("bar"));

        Element enumTypeEl3 = enumType.toXml(createXmlDocument(IEnumContent.XML_TAG));
        IEnumType enumType3 = newEnumType(ipsProject, "AnEnum3");
        enumType3.initFromXml(enumTypeEl3);
        assertThat(enumType3.getEnumValues().get(0).getEnumAttributeValues().get(0).getStringValue(), is("foo"));
        assertThat(enumType3.getEnumValues().get(0).getEnumAttributeValues().get(1).getStringValue(), is("bar"));
    }

    @Test
    public void testValidateAndAppendMessagesParsable() {
        IEnumAttribute stringAttribute = genderEnumType.newEnumAttribute();
        stringAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        stringAttribute.setName("StringAttribute");

        IEnumAttribute integerAttribute = genderEnumType.newEnumAttribute();
        integerAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        integerAttribute.setName("IntegerAttribute");

        IEnumAttribute booleanAttribute = genderEnumType.newEnumAttribute();
        booleanAttribute.setDatatype(Datatype.BOOLEAN.getQualifiedName());
        booleanAttribute.setName("BooleanAttribute");

        genderEnumType.setExtensible(false);
        IEnumValue newEnumValue = genderEnumType.newEnumValue();

        IEnumAttributeValue stringNewAttributeValue = newEnumValue.getEnumAttributeValues().get(3);
        IEnumAttributeValue integerNewAttributeValue = newEnumValue.getEnumAttributeValues().get(4);
        IEnumAttributeValue booleanNewAttributeValue = newEnumValue.getEnumAttributeValues().get(5);

        stringNewAttributeValue.setValue(ValueFactory.createStringValue("String"));
        integerNewAttributeValue.setValue(ValueFactory.createStringValue("4"));
        booleanNewAttributeValue.setValue(ValueFactory.createStringValue("false"));

        assertThat(stringNewAttributeValue.validate(ipsProject).toString(),
                stringNewAttributeValue.isValid(ipsProject), is(true));
        assertThat(integerNewAttributeValue.validate(ipsProject).toString(),
                integerNewAttributeValue.isValid(ipsProject), is(true));
        assertThat(booleanNewAttributeValue.validate(ipsProject).toString(),
                booleanNewAttributeValue.isValid(ipsProject), is(true));

        IIpsModel ipsModel = getIpsModel();

        // Test value parsable with data type Integer.
        ipsModel.clearValidationCache();
        integerNewAttributeValue.setValue(ValueFactory.createStringValue("fooBar"));
        MessageList validationMessageList = integerNewAttributeValue.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertThat(validationMessageList
                .getMessageByCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE),
                is(notNullValue()));
        integerNewAttributeValue.setValue(ValueFactory.createStringValue("4"));

        // Test value parsable with data type Boolean.
        ipsModel.clearValidationCache();
        booleanNewAttributeValue.setValue(ValueFactory.createStringValue("fooBar"));
        validationMessageList = booleanNewAttributeValue.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertThat(validationMessageList
                .getMessageByCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE),
                is(notNullValue()));
        booleanNewAttributeValue.setValue(ValueFactory.createStringValue("false"));
    }

    @Test
    public void testValidateAndAppendMessagesUniqueIdentifierValueEmpty() {
        IIpsModel ipsModel = getIpsModel();
        IEnumAttributeValue uniqueIdentifierEnumAttributeValue = genderEnumValueFemale.getEnumAttributeValues().get(0);

        uniqueIdentifierEnumAttributeValue.setValue(ValueFactory.createStringValue(""));
        MessageList validationMessageList = genderEnumValueFemale.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertThat(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_VALUE_EMPTY),
                is(notNullValue()));

        ipsModel.clearValidationCache();
        uniqueIdentifierEnumAttributeValue.setValue(ValueFactory.createStringValue(null));
        validationMessageList = genderEnumValueFemale.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertThat(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_VALUE_EMPTY),
                is(notNullValue()));
    }

    @Test
    public void testValidateAndAppendMessagesUniqueIdentifierValueNotUnique() {
        IEnumAttributeValue uniqueIdentifierEnumAttributeValueMale = genderEnumValueMale.getEnumAttributeValues()
                .get(0);
        IEnumAttributeValue uniqueIdentifierEnumAttributeValueFemale = genderEnumValueFemale.getEnumAttributeValues()
                .get(0);

        uniqueIdentifierEnumAttributeValueMale.setValue(ValueFactory.createStringValue("foo"));
        uniqueIdentifierEnumAttributeValueFemale.setValue(ValueFactory.createStringValue("foo"));

        MessageList validationMessageList = uniqueIdentifierEnumAttributeValueMale.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertThat(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_NOT_UNIQUE),
                is(notNullValue()));

        validationMessageList = uniqueIdentifierEnumAttributeValueFemale.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertThat(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_NOT_UNIQUE),
                is(notNullValue()));
    }

    @Test
    public void testValidateUniqueIdentifier_duplicateBetweenEnumTypeAndEnumContent_reportedOnContent() {
        IEnumValue typeValue = genderEnumType.newEnumValue();
        typeValue.setEnumAttributeValue(0, ValueFactory.createStringValue("MALE_LITERAL"));
        typeValue.setEnumAttributeValue(1, ValueFactory.createStringValue(GENDER_ENUM_LITERAL_MALE_ID));
        typeValue.setEnumAttributeValue(2, ValueFactory.createStringValue("Male"));

        IEnumAttributeValue contentIdAttributeValue = genderEnumValueMale.getEnumAttributeValues().get(0);
        MessageList validationMessageList = contentIdAttributeValue.validate(ipsProject);
        assertThat(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_NOT_UNIQUE),
                is(notNullValue()));
    }

    @Test
    public void testValidateUniqueIdentifier_duplicateBetweenEnumTypeAndEnumContent_notReportedOnType() {
        IEnumValue typeValue = genderEnumType.newEnumValue();
        typeValue.setEnumAttributeValue(0, ValueFactory.createStringValue("MALE_LITERAL"));
        typeValue.setEnumAttributeValue(1, ValueFactory.createStringValue(GENDER_ENUM_LITERAL_MALE_ID));
        typeValue.setEnumAttributeValue(2, ValueFactory.createStringValue("Male"));

        IEnumAttributeValue typeIdAttributeValue = typeValue.getEnumAttributeValues().get(1);
        MessageList validationMessageList = typeIdAttributeValue.validate(ipsProject);
        assertThat(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_NOT_UNIQUE),
                is(nullValue()));
    }

    @Test
    public void testValidateUniqueIdentifier_duplicateBetweenEnumContentAndEnumType() {
        IEnumValue typeValue = genderEnumType.newEnumValue();
        typeValue.setEnumAttributeValue(0, ValueFactory.createStringValue("DIVERSE_LITERAL"));
        typeValue.setEnumAttributeValue(1, ValueFactory.createStringValue("d"));
        typeValue.setEnumAttributeValue(2, ValueFactory.createStringValue("diverse"));

        getIpsModel().clearValidationCache();

        IEnumAttributeValue contentIdAttributeValue = genderEnumValueMale.getEnumAttributeValues().get(0);
        contentIdAttributeValue.setValue(ValueFactory.createStringValue("d"));

        MessageList validationMessageList = contentIdAttributeValue.validate(ipsProject);
        assertThat(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_NOT_UNIQUE),
                is(notNullValue()));
    }

    @Test
    public void testValidateUniqueIdentifier_noDuplicateBetweenEnumTypeAndEnumContent() {
        IEnumValue typeValue = genderEnumType.newEnumValue();
        typeValue.setEnumAttributeValue(0, ValueFactory.createStringValue("DIVERSE_LITERAL"));
        typeValue.setEnumAttributeValue(1, ValueFactory.createStringValue("d"));
        typeValue.setEnumAttributeValue(2, ValueFactory.createStringValue("diverse"));

        IEnumAttributeValue typeIdAttributeValue = typeValue.getEnumAttributeValues().get(1);
        MessageList validationMessageList = typeIdAttributeValue.validate(ipsProject);
        assertThat(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_NOT_UNIQUE),
                is(nullValue()));
    }

    @Test
    public void testValidateUniqueIdentifier_duplicateBetweenTypeAndContent_noLiteralNameAttribute_reportedOnContent() {
        IEnumType extensibleType = newEnumType(ipsProject, "NoLiteralType");
        extensibleType.setAbstract(false);
        extensibleType.setExtensible(true);
        extensibleType.setEnumContentName("enumcontents.NoLiteralContent");

        IEnumAttribute idAttr = extensibleType.newEnumAttribute();
        idAttr.setName("id");
        idAttr.setDatatype(Datatype.STRING.getQualifiedName());
        idAttr.setUnique(true);
        idAttr.setIdentifier(true);
        IEnumAttribute nameAttr = extensibleType.newEnumAttribute();
        nameAttr.setName("name");
        nameAttr.setDatatype(Datatype.STRING.getQualifiedName());
        nameAttr.setUnique(true);

        IEnumContent content = newEnumContent(ipsProject, "enumcontents.NoLiteralContent");
        content.setEnumType(extensibleType.getQualifiedName());

        IEnumValue contentValue = content.newEnumValue();
        contentValue.setEnumAttributeValue(0, ValueFactory.createStringValue("X"));
        contentValue.setEnumAttributeValue(1, ValueFactory.createStringValue("name_x"));

        IEnumValue typeValue = extensibleType.newEnumValue();
        typeValue.setEnumAttributeValue(0, ValueFactory.createStringValue("X"));
        typeValue.setEnumAttributeValue(1, ValueFactory.createStringValue("name_x_type"));

        IEnumAttributeValue contentIdAttributeValue = contentValue.getEnumAttributeValues().get(0);
        MessageList validationMessageList = contentIdAttributeValue.validate(ipsProject);
        assertThat(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_NOT_UNIQUE),
                is(notNullValue()));
    }

    @Test
    public void testValidateUniqueIdentifier_cacheInvalidatedOnCrossContainerChange() {
        IEnumValue typeValue = genderEnumType.newEnumValue();
        typeValue.setEnumAttributeValue(0, ValueFactory.createStringValue("DIVERSE_LITERAL"));
        typeValue.setEnumAttributeValue(1, ValueFactory.createStringValue("d"));
        typeValue.setEnumAttributeValue(2, ValueFactory.createStringValue("diverse"));

        IEnumAttributeValue contentIdAttributeValue = genderEnumValueMale.getEnumAttributeValues().get(0);
        MessageList validationMessageList = contentIdAttributeValue.validate(ipsProject);
        assertThat(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_NOT_UNIQUE),
                is(nullValue()));

        contentIdAttributeValue.setValue(ValueFactory.createStringValue("d"));

        validationMessageList = contentIdAttributeValue.validate(ipsProject);
        assertThat(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_NOT_UNIQUE),
                is(notNullValue()));
    }

    @Test
    public void testValidateUniqueIdentifier_literalNameNoFalsePositiveWithContentId() {
        IEnumValue typeValue = genderEnumType.newEnumValue();
        typeValue.setEnumAttributeValue(0, ValueFactory.createStringValue(GENDER_ENUM_LITERAL_MALE_ID));
        typeValue.setEnumAttributeValue(1, ValueFactory.createStringValue("x"));
        typeValue.setEnumAttributeValue(2, ValueFactory.createStringValue("x_name"));

        IEnumAttributeValue literalNameAttributeValue = typeValue.getEnumAttributeValues().get(0);
        MessageList validationMessageList = literalNameAttributeValue.validate(ipsProject);
        assertThat(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_NOT_UNIQUE),
                is(nullValue()));
    }

    @Test
    public void testValidateAndAppendMessagesIdBoundary1() {
        paymentMode.setExtensible(true);
        paymentMode.setEnumContentName("EnumContentName");
        paymentMode.setIdentifierBoundary("P9");
        MessageList validationMessageList = paymentMode.validate(ipsProject);
        assertThat(validationMessageList.isEmpty(), is(true));

        getIpsModel().clearValidationCache();
        paymentMode.setIdentifierBoundary("B1");

        validationMessageList = paymentMode.validate(ipsProject);
        assertThat(validationMessageList.size(), is(2));
        assertThat(validationMessageList.getMessage(0).getCode(),
                is(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_ID_DISALLOWED_BY_IDENTIFIER_BOUNDARY));
        assertThat(validationMessageList.getMessage(1).getCode(),
                is(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_ID_DISALLOWED_BY_IDENTIFIER_BOUNDARY));
    }

    @Test
    public void testValidateAndAppendMessagesIdBoundary2() {
        paymentMode.setExtensible(true);
        paymentMode.setEnumContentName("EnumContentName");
        paymentMode.setIdentifierBoundary("P9");
        assertThat(paymentMode.isValid(ipsProject), is(true));

        IEnumValue monthly = paymentMode.getEnumValues().get(0);
        monthly.setEnumAttributeValue(1, ValueFactory.createStringValue("X1"));

        getIpsModel().clearValidationCache();

        MessageList validationMessageList = paymentMode.validate(ipsProject);
        assertThat(validationMessageList.size(), is(1));
        assertThat(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_ID_DISALLOWED_BY_IDENTIFIER_BOUNDARY),
                is(notNullValue()));
    }

    @Test
    public void testValidateThis_Mandatory() {
        IEnumAttribute stringAttribute = genderEnumType.newEnumAttribute();
        stringAttribute.setMandatory(true);
        stringAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        stringAttribute.setName("StringAttribute");

        IEnumAttribute integerAttribute = genderEnumType.newEnumAttribute();
        integerAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        integerAttribute.setName("IntegerAttribute");
        integerAttribute.setMandatory(true);

        IEnumValue newEnumValue = genderEnumType.newEnumValue();
        IEnumAttributeValue stringNewAttributeValue = newEnumValue.getEnumAttributeValues().get(0);
        stringNewAttributeValue.setValue(ValueFactory.createStringValue("123"));

        // Test for integer attribute not set
        IEnumAttributeValue integerNewAttributeValue = newEnumValue.getEnumAttributeValues().get(3);
        integerNewAttributeValue.setValue(ValueFactory.createStringValue(null));
        MessageList validationMessageList = integerNewAttributeValue.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertThat(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_MANDATORY_ATTRIBUTE_IS_EMPTY),
                is(notNullValue()));
        IEnumAttributeValue booleanNewAttributeValue = newEnumValue.getEnumAttributeValues().get(2);
        booleanNewAttributeValue.setValue(ValueFactory.createStringValue("true"));
        validationMessageList = booleanNewAttributeValue.validate(ipsProject);
        assertThat(validationMessageList.isEmpty(), is(true));

        // Test for integer attribute set
        integerNewAttributeValue.setValue(ValueFactory.createStringValue("123"));
        validationMessageList = integerNewAttributeValue.validate(ipsProject);
        assertThat(validationMessageList.isEmpty(), is(true));

        // Test for string attribute set
        stringNewAttributeValue.setValue(ValueFactory.createStringValue("Test"));
        validationMessageList = stringNewAttributeValue.validate(ipsProject);
        assertThat(validationMessageList.isEmpty(), is(true));

        // Test for multilingual string attribute set
        stringAttribute.setMultilingual(true);
        InternationalStringValue internationalStringValue = new InternationalStringValue();
        internationalStringValue.getContent().add(new LocalizedString(Locale.GERMAN, "foo"));
        internationalStringValue.getContent().add(new LocalizedString(Locale.ENGLISH, ""));
        IEnumAttributeValue internationalStringAttributeValue = newEnumValue.getEnumAttributeValue(stringAttribute);
        internationalStringAttributeValue.setValue(internationalStringValue);
        validationMessageList = internationalStringAttributeValue.validate(ipsProject);
        assertThat(validationMessageList.size(), is(2));
        assertThat(validationMessageList
                .getMessageByCode(IEnumAttributeValue.MSGCODE_MANDATORY_ATTRIBUTE_IS_EMPTY),
                is(notNullValue()));
        assertThat(validationMessageList
                .getMessageByCode(IAttributeValue.MSGCODE_MULTILINGUAL_NOT_SET),
                is(notNullValue()));
    }

    @Test
    public void testValidateIdentifierValueNotValid_NotExtensible() {
        paymentMode.setExtensible(true);
        paymentMode.setEnumContentName("EnumContentName");
        paymentMode.setIdentifierBoundary("P9");
        MessageList validationMessageList = paymentMode.validate(ipsProject);
        assertThat(validationMessageList.isEmpty(), is(true));

        getIpsModel().clearValidationCache();
        paymentMode.setIdentifierBoundary("B1");

        validationMessageList = paymentMode.validate(ipsProject);
        assertThat(validationMessageList.size(), is(2));

        paymentMode.setExtensible(false);
        validationMessageList = paymentMode.validate(ipsProject);
        System.out.println(validationMessageList.getText());
        assertThat(validationMessageList.size(), is(0));
    }

    @Test
    public void testValidateIdentifierValueNotValid_Abstract() {
        paymentMode.setExtensible(true);
        paymentMode.setEnumContentName("EnumContentName");
        paymentMode.setIdentifierBoundary("P9");
        MessageList validationMessageList = paymentMode.validate(ipsProject);
        assertThat(validationMessageList.isEmpty(), is(true));

        getIpsModel().clearValidationCache();
        paymentMode.setIdentifierBoundary("B1");

        validationMessageList = paymentMode.validate(ipsProject);
        assertThat(validationMessageList.size(), is(2));

        paymentMode.setAbstract(true);
        validationMessageList = paymentMode.validate(ipsProject);
        assertThat(validationMessageList.size(), is(1));
        assertThat(IEnumAttributeValue.MSGCODE_ENUM_ATTRIBUTE_ID_DISALLOWED_BY_IDENTIFIER_BOUNDARY == validationMessageList
                .getMessage(0).getCode(), is(false));
    }

    @Test
    public void testGetEnumValue() {
        assertThat(genderEnumValueMale.getEnumAttributeValues().get(0).getEnumValue(), is(genderEnumValueMale));
    }

    @Test
    public void testGetSetValue() {
        InternationalStringValue internationalStringValue = new InternationalStringValue();
        internationalStringValue.getContent().add(new LocalizedString(Locale.GERMAN, "foo"));
        maleNameAttributeValue.setValue(internationalStringValue);
        assertThat(maleNameAttributeValue.getValue(), is(internationalStringValue));

        StringValue stringValue = new StringValue("foo");
        maleNameAttributeValue.setValue(stringValue);
        assertThat(maleNameAttributeValue.getValue(), is(stringValue));
    }

    @Test
    public void testGetValueType() {
        InternationalStringValue internationalStringValue = new InternationalStringValue();
        internationalStringValue.getContent().add(new LocalizedString(Locale.GERMAN, "foo"));
        maleNameAttributeValue.setValue(internationalStringValue);
        assertThat(maleNameAttributeValue.getValueType(), is(ValueType.INTERNATIONAL_STRING));

        maleNameAttributeValue.setValue(new StringValue("foo"));
        assertThat(maleNameAttributeValue.getValueType(), is(ValueType.STRING));
    }

    @Test
    public void testGetSringValue() {
        InternationalStringValue internationalStringValue = new InternationalStringValue();
        internationalStringValue.getContent().add(new LocalizedString(Locale.GERMAN, "foo"));
        maleNameAttributeValue.setValue(internationalStringValue);
        assertThat(maleNameAttributeValue.getStringValue(), is("de=foo"));

        maleNameAttributeValue.setValue(new StringValue("foo"));
        assertThat(maleNameAttributeValue.getStringValue(), is("foo"));
    }

    @Test
    public void testIsNull() {
        InternationalStringValue internationalStringValue = new InternationalStringValue();
        maleNameAttributeValue.setValue(internationalStringValue);
        assertThat(maleNameAttributeValue.isNullValue(), is(false));

        maleNameAttributeValue.setValue(new StringValue(null));
        assertThat(maleNameAttributeValue.isNullValue(), is(true));
    }

    @Test
    public void testValidateAndAppendMessagesMultilingual() {
        InternationalStringValue internationalStringValue = new InternationalStringValue();
        internationalStringValue.getContent().add(new LocalizedString(Locale.GERMAN, "foo"));
        maleNameAttributeValue.setValue(internationalStringValue);
        assertThat(maleNameAttributeValue.getValueType(), is(ValueType.INTERNATIONAL_STRING));
        maleNameAttributeValue.validate(ipsProject);
        assertThat(maleNameAttributeValue.findEnumAttribute(ipsProject).isMultilingual(), is(false));

        maleNameAttributeValue.setValue(new StringValue("foo"));
        maleNameAttributeValue.findEnumAttribute(ipsProject).setMultilingual(true);
        assertThat(maleNameAttributeValue.getValueType(), is(ValueType.STRING));
        maleNameAttributeValue.validate(ipsProject);
        assertThat(maleNameAttributeValue.findEnumAttribute(ipsProject).isMultilingual(), is(true));

    }

    @Test
    public void testFixValueType() {
        InternationalStringValue internationalStringValue = new InternationalStringValue();
        internationalStringValue.getContent().add(new LocalizedString(Locale.GERMAN, "foo"));
        maleNameAttributeValue.setValue(internationalStringValue);
        assertThat(maleNameAttributeValue.getValueType(), is(ValueType.INTERNATIONAL_STRING));
        maleNameAttributeValue.fixValueType(false);
        assertThat(maleNameAttributeValue.getValueType(), is(ValueType.STRING));

        maleNameAttributeValue.setValue(new StringValue("foo"));
        assertThat(maleNameAttributeValue.getValueType(), is(ValueType.STRING));
        maleNameAttributeValue.fixValueType(true);
        assertThat(maleNameAttributeValue.getValueType(), is(ValueType.INTERNATIONAL_STRING));
    }

    @Test
    public void testValidateAndAppendMessages_referenceExtensibleEnumValueInContent() throws Exception {
        IEnumValue newEnumValue = createEnumValueWithEnumReference(CONTENT_ID);

        MessageList messageList = newEnumValue.validate(ipsProject);

        assertThat(messageList
                .getMessageByCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE),
                is(notNullValue()));
    }

    @Test
    public void testValidateAndAppendMessages_referenceExtensibleEnumValueInType() throws Exception {
        IEnumValue newEnumValue = createEnumValueWithEnumReference(TYPE_ID);

        MessageList messageList = newEnumValue.validate(ipsProject);

        assertThat(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE
                + "not expected but message list was: " + messageList.toString(),
                messageList
                        .getMessageByCode(
                                IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE),
                is(nullValue()));
    }

    private IEnumValue createEnumValueWithEnumReference(String refId) {
        paymentMode.setExtensible(true);
        paymentMode.setEnumContentName(PAYMENT_CONTENT);
        EnumContent newEnumContent = newEnumContent(paymentMode, PAYMENT_CONTENT);
        paymentMode.newEnumValue().getEnumAttributeValues().get(1).setValue(ValueFactory.createStringValue(TYPE_ID));
        newEnumContent.newEnumValue().getEnumAttributeValues().get(0)
                .setValue(ValueFactory.createStringValue(CONTENT_ID));
        genderEnumAttributeName.setDatatype(paymentMode.getName());
        IEnumValue newEnumValue = genderEnumType.newEnumValue();
        List<IEnumAttributeValue> enumAttributeValues = newEnumValue.getEnumAttributeValues();
        enumAttributeValues.get(0).setValue(ValueFactory.createStringValue("LITERAL"));
        enumAttributeValues.get(1).setValue(ValueFactory.createStringValue("id1"));
        enumAttributeValues.get(2).setValue(ValueFactory.createStringValue(refId));
        return newEnumValue;
    }

}
