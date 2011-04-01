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

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
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
        MessageList validationMessageList = literalNameAttribute.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumLiteralNameAttribute.MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_NOT_NEEDED));

        paymentMode.setAbstract(false);
        paymentMode.setContainingValues(false);
        getIpsModel().clearValidationCache();
        validationMessageList = literalNameAttribute.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumLiteralNameAttribute.MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_NOT_NEEDED));
    }

    @Test
    public void testValidateDefaultValueProviderAttribute() throws CoreException {
        // Test pass validation if no default value provider attribute specified.
        literalNameAttribute.setDefaultValueProviderAttribute("");
        assertTrue(literalNameAttribute.isValid(ipsProject));

        // Test not existing default value provider attribute.
        literalNameAttribute.setDefaultValueProviderAttribute("foo");
        getIpsModel().clearValidationCache();
        MessageList validationMessageList = literalNameAttribute.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumLiteralNameAttribute.MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_DOES_NOT_EXIST));

        // Test default value provider attribute not of data type String.
        IEnumAttribute invalidProviderAttribute = paymentMode.newEnumAttribute();
        invalidProviderAttribute.setName("invalidProviderAttribute");
        invalidProviderAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        invalidProviderAttribute.setUnique(true);
        literalNameAttribute.setDefaultValueProviderAttribute("invalidProviderAttribute");
        getIpsModel().clearValidationCache();
        validationMessageList = literalNameAttribute.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumLiteralNameAttribute.MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_NOT_OF_DATATYPE_STRING));

        // Test default value provider attribute not unique.
        invalidProviderAttribute.setUnique(false);
        invalidProviderAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        getIpsModel().clearValidationCache();
        validationMessageList = literalNameAttribute.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumLiteralNameAttribute.MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_NOT_UNIQUE));
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

    @Test
    public void testGetRenameRefactoring() {
        assertNull(literalNameAttribute.getRenameRefactoring());
    }

}
