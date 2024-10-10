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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.faktorips.abstracttest.AbstractIpsEnumPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.StringUtil;
import org.junit.Test;

public class EnumValueTest extends AbstractIpsEnumPluginTest {

    private static final String PAYMENT_CONTENT = "paymentContent";

    @Test
    public void testNewEnumAttributeValue() {
        IEnumAttributeValue newEnumAttributeValue = genderEnumValueMale.newEnumAttributeValue();

        assertNotNull(newEnumAttributeValue);
    }

    @Test
    public void testNewEnumAttributeValue_fixedValue() {
        IEnumAttributeValue enumAttributeValue = genderEnumValueMale.getEnumAttributeValues()
                .get(genderEnumValueMale.getEnumAttributeValuesCount() - 1);
        IEnumAttribute enumAttribute = enumAttributeValue.findEnumAttribute(ipsProject);
        enumAttributeValue.delete();
        enumAttribute.setMultilingual(true);
        final IEnumAttributeValue newEnumAttributeValue = genderEnumValueMale.newEnumAttributeValue();

        assertNotNull(newEnumAttributeValue);
        assertTrue(newEnumAttributeValue.getValue().getContent() instanceof IInternationalString);
    }

    @Test
    public void testNewEnumLiteralNameAttributeValue() {
        assertNotNull(genderEnumValueMale.newEnumLiteralNameAttributeValue());
    }

    @Test
    public void testGetEnumAttributeValues() {
        assertEquals(2, genderEnumValueMale.getEnumAttributeValues().size());
    }

    @Test
    public void testValidateThis() {
        assertTrue(genderEnumValueFemale.isValid(ipsProject));
    }

    @Test
    public void testValidateNumberEnumAttributeValues() {
        genderEnumType.newEnumAttribute();
        assertTrue(genderEnumValueFemale.isValid(ipsProject));

        getIpsModel().clearValidationCache();
        genderEnumValueFemale.getEnumAttributeValues().get(0).delete();
        MessageList validationMessageList = genderEnumValueFemale.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(
                IEnumValue.MSGCODE_ENUM_VALUE_NUMBER_ATTRIBUTE_VALUES_DOES_NOT_CORRESPOND_TO_NUMBER_ATTRIBUTES));
    }

    @Test
    public void testGetEnumValueContainer() {
        assertEquals(genderEnumContent, genderEnumValueMale.getEnumValueContainer());
    }

    @Test
    public void testGetEnumAttributeValue() {
        assertNull(genderEnumValueMale.getEnumAttributeValue(null));

        assertEquals(genderEnumValueMale.getEnumAttributeValues().get(0),
                genderEnumValueMale.getEnumAttributeValue(genderEnumAttributeId));
        assertEquals(genderEnumValueFemale.getEnumAttributeValues().get(0),
                genderEnumValueFemale.getEnumAttributeValue(genderEnumAttributeId));
        assertEquals(genderEnumValueMale.getEnumAttributeValues().get(1),
                genderEnumValueMale.getEnumAttributeValue(genderEnumAttributeName));
        assertEquals(genderEnumValueFemale.getEnumAttributeValues().get(1),
                genderEnumValueFemale.getEnumAttributeValue(genderEnumAttributeName));
    }

    @Test
    public void testGetEnumAttributeValue_extensibleContent() {
        paymentMode.setExtensible(true);
        paymentMode.setEnumContentName(PAYMENT_CONTENT);
        EnumContent enumContent = newEnumContent(paymentMode, PAYMENT_CONTENT);
        IEnumValue enumValue = enumContent.newEnumValue();
        enumValue.getEnumAttributeValues().get(0).setValue(ValueFactory.createStringValue("idValue"));
        enumValue.getEnumAttributeValues().get(1).setValue(ValueFactory.createStringValue("nameValue"));

        IEnumAttributeValue enumAttributeValue = enumValue
                .getEnumAttributeValue(paymentMode.findIdentiferAttribute(ipsProject));

        assertEquals(2, enumValue.getEnumAttributeValues().size());
        assertEquals("idValue", enumAttributeValue.getStringValue());
    }

    @Test
    public void testSetEnumAttributeValueAttributeGiven() {
        genderEnumValueMale.setEnumAttributeValue(genderEnumAttributeName, ValueFactory.createStringValue("foo"));
        assertEquals("foo", genderEnumValueMale.getEnumAttributeValue(genderEnumAttributeName).getStringValue());
    }

    @Test(expected = NullPointerException.class)
    public void testSetEnumAttributeValueAttributeGiven_Null() {
        genderEnumValueMale.setEnumAttributeValue((IEnumAttribute)null, ValueFactory.createStringValue(""));
    }

    @Test
    public void testSetEnumAttributeValueAttributeNameGiven() {
        genderEnumValueMale.setEnumAttributeValue(genderEnumAttributeName.getName(),
                ValueFactory.createStringValue("foo"));
        assertEquals("foo", genderEnumValueMale.getEnumAttributeValue(genderEnumAttributeName).getStringValue());
    }

    @Test(expected = NullPointerException.class)
    public void testSetEnumAttributeValueAttributeNameGiven_Null() {
        genderEnumValueMale.setEnumAttributeValue((String)null, ValueFactory.createStringValue(""));

    }

    @Test
    public void testSetEnumAttributeValueAttributeValueIndexGiven() {
        genderEnumValueMale.setEnumAttributeValue(1, ValueFactory.createStringValue("foo"));
        assertEquals("foo", genderEnumValueMale.getEnumAttributeValues().get(1).getStringValue());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetEnumAttributeValueAttributeValueIndexGiven_Negative() {
        genderEnumValueMale.setEnumAttributeValue(-1, ValueFactory.createStringValue(""));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetEnumAttributeValueAttributeValueIndexGiven_TooHigh() {
        genderEnumValueMale.setEnumAttributeValue(20, ValueFactory.createStringValue(""));
    }

    @Test
    public void testFindUniqueEnumAttributeValues() {
        IEnumValue value = paymentMode.getEnumValues().get(0);
        List<IEnumAttributeValue> uniqueAttributeValues = value
                .findUniqueEnumAttributeValues(paymentMode.findUniqueEnumAttributes(true, ipsProject), ipsProject);
        assertEquals(3, uniqueAttributeValues.size());
    }

    @Test
    public void testGetEnumLiteralNameAttributeValue() {
        assertNull(genderEnumValueMale.getEnumLiteralNameAttributeValue());
        IEnumValue value = paymentMode.getEnumValues().get(0);
        assertEquals("MONTHLY", value.getEnumLiteralNameAttributeValue().getStringValue());
    }

    @Test
    public void testDirectChangesToTheCorrespondingFile_EnumValue() throws Exception {
        IEnumAttribute enumAttribute = paymentMode.getEnumAttribute("name");
        IIpsSrcFile ipsFile = paymentMode.getIpsSrcFile();
        IEnumValue value = paymentMode.getEnumValues().get(0);
        value.setEnumAttributeValue(enumAttribute, ValueFactory.createStringValue("foo"));
        ipsFile.save(null);
        String encoding = paymentMode.getIpsProject().getXmlFileCharset();
        AFile file = ipsFile.getCorrespondingFile();
        String content = StringUtil.readFromInputStream(file.getContents(), encoding);
        content = content.replace("foo", "bar");
        file.setContents(StringUtil.getInputStreamForString(content, encoding), false, null);

        paymentMode = (EnumType)ipsFile.getIpsObject(); // forces a reload

        assertThat(paymentMode.getEnumValues().get(0), is(sameInstance(value)));
        assertThat(value.getEnumAttributeValue(enumAttribute).getStringValue(), is("bar"));
    }

}
