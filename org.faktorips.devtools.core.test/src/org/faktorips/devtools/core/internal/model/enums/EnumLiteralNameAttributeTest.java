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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsEnumPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
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
    public void testXml() throws ParserConfigurationException, CoreException {
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
    public void testValidate() throws CoreException {
        assertEquals(0, literalNameAttribute.validate(ipsProject).size());
    }

    @Test
    public void testValidateIsNeeded() throws CoreException {
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

    private void assertOneValidationMessage(IEnumLiteralNameAttribute literalNameAttribute2) throws CoreException {
        assertOneValidationMessage(literalNameAttribute2.validate(ipsProject));
    }

    private boolean hasLiteralNameNotNeededMessage(IEnumLiteralNameAttribute literalNameAttribute2)
            throws CoreException {
        MessageList validationMessageList = literalNameAttribute2.validate(ipsProject);
        Message notNeededMessage = validationMessageList
                .getMessageByCode(IEnumLiteralNameAttribute.MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_NOT_NEEDED);
        return notNeededMessage != null;
    }

    @Test
    public void testValidateDefaultValueProviderAttributePassValidationIfAttributeNotSpecified() throws CoreException {
        literalNameAttribute.setDefaultValueProviderAttribute("");
        assertTrue(literalNameAttribute.isValid(ipsProject));

    }

    @Test
    public void testValidateDefaultValueProviderAttributeNotExistingProviderAttribute() throws CoreException {
        literalNameAttribute.setDefaultValueProviderAttribute("foo");
        getIpsModel().clearValidationCache();
        MessageList validationMessageList = literalNameAttribute.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumLiteralNameAttribute.MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_DOES_NOT_EXIST));

    }

    @Test
    public void testValidateDefaultValueProviderAttributeProviderAttributeNotString() throws CoreException {
        IEnumAttribute invalidProviderAttribute = paymentMode.newEnumAttribute();
        invalidProviderAttribute.setName("invalidProviderAttribute");
        invalidProviderAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        invalidProviderAttribute.setUnique(true);
        literalNameAttribute.setDefaultValueProviderAttribute("invalidProviderAttribute");
        getIpsModel().clearValidationCache();
        MessageList validationMessageList = literalNameAttribute.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumLiteralNameAttribute.MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_NOT_OF_DATATYPE_STRING));

    }

    @Test
    public void testValidateDefaultValueProviderAttributeUniquePossible() throws CoreException {
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
        }
    }

    @Test
    public void testGetSetInherited() {
        assertFalse(literalNameAttribute.isInherited());
        try {
            literalNameAttribute.setInherited(true);
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testGetSetUsedAsNameInFaktorIpsUi() {
        assertFalse(literalNameAttribute.isUsedAsNameInFaktorIpsUi());
        try {
            literalNameAttribute.setUsedAsNameInFaktorIpsUi(true);
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

}
