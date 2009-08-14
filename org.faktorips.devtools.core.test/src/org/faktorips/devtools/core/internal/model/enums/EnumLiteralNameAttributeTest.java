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
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class EnumLiteralNameAttributeTest extends AbstractIpsEnumPluginTest {

    private IEnumLiteralNameAttribute literalNameAttribute;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        literalNameAttribute = paymentMode.getEnumLiteralNameAttribute();
    }

    public void testGetSetDefaultValueProviderAttribute() {
        assertEquals("name", literalNameAttribute.getDefaultValueProviderAttribute());
        literalNameAttribute.setDefaultValueProviderAttribute("foo");
        assertEquals("foo", literalNameAttribute.getDefaultValueProviderAttribute());
    }

    public void testXml() throws ParserConfigurationException, CoreException {
        Element xmlElement = paymentMode.toXml(createXmlDocument(IEnumLiteralNameAttribute.XML_TAG));
        NamedNodeMap attributes = xmlElement.getChildNodes().item(3).getAttributes();
        assertEquals("name", attributes.getNamedItem(
                IEnumLiteralNameAttribute.PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE).getTextContent());
        assertEquals(6, xmlElement.getChildNodes().getLength());

        IEnumType loadedEnumType = newEnumType(ipsProject, "LoadedEnumType");
        loadedEnumType.initFromXml(xmlElement);
        IEnumLiteralNameAttribute literalNameAttribute = loadedEnumType.getEnumLiteralNameAttribute();
        assertEquals(IEnumLiteralNameAttribute.DEFAULT_NAME, literalNameAttribute.getName());
        assertEquals("name", literalNameAttribute.getDefaultValueProviderAttribute());
        assertTrue(loadedEnumType.containsEnumLiteralNameAttribute());
    }

    public void testValidate() throws CoreException {
        assertEquals(0, literalNameAttribute.validate(ipsProject).getNoOfMessages());
    }

    public void testValidateDefaultValueProviderAttribute() throws CoreException {
        // Test pass validation if no default value provider attribute specified.
        literalNameAttribute.setDefaultValueProviderAttribute("");
        assertTrue(literalNameAttribute.isValid());

        // Test pass validation if literal name attribute currently not needed.
        literalNameAttribute.setDefaultValueProviderAttribute("xyz");
        paymentMode.setAbstract(true);
        getIpsModel().clearValidationCache();
        assertTrue(literalNameAttribute.isValid());
        paymentMode.setAbstract(false);

        // Test not existing default value provider attribute.
        literalNameAttribute.setDefaultValueProviderAttribute("foo");
        getIpsModel().clearValidationCache();
        MessageList validationMessageList = literalNameAttribute.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumLiteralNameAttribute.MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_DOES_NOT_EXIST));

        // Test default value provider attribute not of datatype String.
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

    public void testGetSetIdentifier() {
        assertFalse(literalNameAttribute.isIdentifier());
        try {
            literalNameAttribute.setIdentifier(true);
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testGetSetInherited() {
        assertFalse(literalNameAttribute.isInherited());
        try {
            literalNameAttribute.setInherited(true);
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testGetSetUsedAsNameInFaktorIpsUi() {
        assertFalse(literalNameAttribute.isUsedAsNameInFaktorIpsUi());
        try {
            literalNameAttribute.setUsedAsNameInFaktorIpsUi(true);
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

}
