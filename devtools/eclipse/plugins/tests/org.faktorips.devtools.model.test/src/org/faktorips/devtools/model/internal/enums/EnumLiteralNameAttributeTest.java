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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.abstracttest.AbstractIpsEnumPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class EnumLiteralNameAttributeTest extends AbstractIpsEnumPluginTest {

    private IEnumLiteralNameAttribute literalNameAttribute;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        literalNameAttribute = paymentMode.getEnumLiteralNameAttribute();
    }

    @Test
    public void testGetSetDefaultValueProviderAttribute() {
        assertEquals("name", literalNameAttribute.getDefaultValueProviderAttribute());
        literalNameAttribute.setDefaultValueProviderAttribute("foo");
        assertEquals("foo", literalNameAttribute.getDefaultValueProviderAttribute());
    }

    @Test
    public void testXml() throws ParserConfigurationException {
        Element xmlElement = literalNameAttribute.toXml(createXmlDocument(IEnumLiteralNameAttribute.XML_TAG));
        NamedNodeMap attributes = xmlElement.getAttributes();
        assertEquals("name",
                attributes.getNamedItem(IEnumLiteralNameAttribute.PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE)
                        .getTextContent());

        IEnumLiteralNameAttribute loadedAttribute = paymentMode.newEnumLiteralNameAttribute();
        loadedAttribute.initFromXml(xmlElement);
        assertEquals(IEnumLiteralNameAttribute.DEFAULT_NAME, loadedAttribute.getName());
        assertEquals("name", loadedAttribute.getDefaultValueProviderAttribute());
    }

    @Test
    public void testNoWriteDefaultValueProviderAttributeWhenEmpty()
            throws ParserConfigurationException {
        literalNameAttribute.setDefaultValueProviderAttribute("");
        Element xmlElement = literalNameAttribute.toXml(createXmlDocument(IEnumLiteralNameAttribute.XML_TAG));
        NamedNodeMap attributes = xmlElement.getAttributes();

        assertThat(attributes.getNamedItem(IEnumLiteralNameAttribute.PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE),
                is(nullValue()));
    }

    @Test
    public void testValidate() {
        assertEquals(0, literalNameAttribute.validate(ipsProject).size());
    }

    @Test
    public void testValidateIsNeeded() {
        paymentMode.setAbstract(true);
        assertOneValidationMessage(literalNameAttribute);
        assertTrue(hasLiteralNameNotNeededMessage(literalNameAttribute));

        paymentMode.setAbstract(false);
        paymentMode.setExtensible(true);
        getIpsModel().clearValidationCache();
        assertFalse(hasLiteralNameNotNeededMessage(literalNameAttribute));

        paymentMode.setAbstract(false);
        paymentMode.setExtensible(false);
        getIpsModel().clearValidationCache();
        assertFalse(hasLiteralNameNotNeededMessage(literalNameAttribute));
    }

    private void assertOneValidationMessage(IEnumLiteralNameAttribute literalNameAttribute2) {
        assertOneValidationMessage(literalNameAttribute2.validate(ipsProject));
    }

    private boolean hasLiteralNameNotNeededMessage(IEnumLiteralNameAttribute literalNameAttribute2) {
        MessageList validationMessageList = literalNameAttribute2.validate(ipsProject);
        Message notNeededMessage = validationMessageList
                .getMessageByCode(IEnumLiteralNameAttribute.MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_NOT_NEEDED);
        return notNeededMessage != null;
    }

    @Test
    public void testValidateDefaultValueProviderAttributePassValidationIfAttributeNotSpecified() {
        literalNameAttribute.setDefaultValueProviderAttribute("");
        assertTrue(literalNameAttribute.isValid(ipsProject));

    }

    @Test
    public void testValidateDefaultValueProviderAttributeNotExistingProviderAttribute() {
        literalNameAttribute.setDefaultValueProviderAttribute("foo");
        getIpsModel().clearValidationCache();
        MessageList validationMessageList = literalNameAttribute.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(
                        IEnumLiteralNameAttribute.MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_DOES_NOT_EXIST));

    }

    @Test
    public void testValidateDefaultValueProviderAttributeProviderAttributeNotString() {
        IEnumAttribute invalidProviderAttribute = paymentMode.newEnumAttribute();
        invalidProviderAttribute.setName("invalidProviderAttribute");
        invalidProviderAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        invalidProviderAttribute.setUnique(true);
        literalNameAttribute.setDefaultValueProviderAttribute("invalidProviderAttribute");
        getIpsModel().clearValidationCache();
        MessageList validationMessageList = literalNameAttribute.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(
                        IEnumLiteralNameAttribute.MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_NOT_OF_DATATYPE_STRING));

    }

    @Test
    public void testValidateDefaultValueProviderAttributeUniquePossible() {
        IEnumAttribute invalidProviderAttribute = paymentMode.newEnumAttribute();
        invalidProviderAttribute.setName("notUniqueProviderAttribute");
        invalidProviderAttribute.setUnique(false);
        invalidProviderAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        literalNameAttribute.setDefaultValueProviderAttribute("notUniqueProviderAttribute");
        getIpsModel().clearValidationCache();
        MessageList validationMessageList = literalNameAttribute.validate(ipsProject);
        assertTrue(validationMessageList.toString(), validationMessageList.isEmpty());
    }

    @Test
    public void testGetSetIdentifier() {
        assertFalse(literalNameAttribute.isIdentifier());
        try {
            literalNameAttribute.setIdentifier(true);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetSetInherited() {
        assertFalse(literalNameAttribute.isInherited());
        try {
            literalNameAttribute.setInherited(true);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetSetMandatory() {
        assertTrue(literalNameAttribute.isMandatory());
        try {
            literalNameAttribute.setMandatory(false);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetSetUsedAsNameInFaktorIpsUi() {
        assertFalse(literalNameAttribute.isUsedAsNameInFaktorIpsUi());
        try {
            literalNameAttribute.setUsedAsNameInFaktorIpsUi(true);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

}
