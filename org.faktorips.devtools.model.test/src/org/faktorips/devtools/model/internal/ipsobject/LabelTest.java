/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsobject.ILabel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class LabelTest extends AbstractIpsPluginTest {

    private ILabel label;

    private IIpsProject ipsProject;

    private IPolicyCmptType policyCmptType;

    private IPolicyCmptTypeAttribute attribute;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyCmptType(ipsProject, "TestPolicy");
        attribute = (IPolicyCmptTypeAttribute)policyCmptType.newAttribute();
        label = attribute.newLabel();
    }

    @Test
    public void testConstructor() {
        assertNull(label.getLocale());
        assertEquals("", label.getValue());
        assertEquals("", label.getPluralValue());
    }

    @Test
    public void testSetLocale() {
        label.setLocale(Locale.GERMAN);
        assertEquals(Locale.GERMAN, label.getLocale());
    }

    @Test
    public void testSetValue() {
        label.setValue("foo");
        assertEquals("foo", label.getValue());
    }

    @Test
    public void testSetValueNullPointer() {
        label.setValue(null);
        assertEquals("", label.getValue());
    }

    @Test
    public void testSetPluralValue() {
        label.setPluralValue("bar");
        assertEquals("bar", label.getPluralValue());
    }

    @Test
    public void testSetPluralValueNullPointer() {
        label.setPluralValue(null);
        assertEquals("", label.getPluralValue());
    }

    @Test
    public void testValidateLocaleMissing() {
        MessageList validationMessages = label.validate(ipsProject);
        assertEquals(1, validationMessages.size());
        Message message = validationMessages.getFirstMessage(Message.ERROR);
        assertEquals(ILabel.MSGCODE_LOCALE_MISSING, message.getCode());
    }

    @Test
    public void testValidateLocaleNotSupported() {
        label.setLocale(Locale.TAIWAN);
        MessageList validationMessages = label.validate(ipsProject);
        assertEquals(1, validationMessages.size());
        Message message = validationMessages.getFirstMessage(Message.WARNING);
        assertEquals(ILabel.MSGCODE_LOCALE_NOT_SUPPORTED_BY_IPS_PROJECT, message.getCode());
    }

    @Test
    public void testValidateLocaleNotSupportedByContextProject() {
        IIpsProject contextProject = newIpsProject("ContextProject");
        IIpsProjectProperties properties = contextProject.getProperties();
        properties.removeSupportedLanguage(Locale.GERMAN);
        contextProject.setProperties(properties);

        label.setLocale(Locale.GERMAN);
        MessageList validationMessages = label.validate(contextProject);
        assertTrue(validationMessages.isEmpty());
    }

    @Test
    public void testValidateOk() {
        label.setLocale(Locale.US);
        MessageList validationMessages = label.validate(ipsProject);
        assertEquals(0, validationMessages.size());
    }

    @Test
    public void testXml() throws ParserConfigurationException {
        label.setLocale(Locale.ENGLISH);
        label.setValue("foo");
        label.setPluralValue("bar");

        Element xmlElement = label.toXml(createXmlDocument(ILabel.XML_TAG_NAME));
        NamedNodeMap labelAttributes = xmlElement.getAttributes();
        assertEquals(Locale.ENGLISH.getLanguage(), labelAttributes.getNamedItem(ILabel.PROPERTY_LOCALE)
                .getTextContent());
        assertEquals("foo", labelAttributes.getNamedItem(ILabel.PROPERTY_VALUE).getTextContent());
        assertEquals("bar", labelAttributes.getNamedItem(ILabel.PROPERTY_PLURAL_VALUE).getTextContent());

        ILabel loadedLabel = attribute.newLabel();
        loadedLabel.initFromXml(xmlElement);
        assertEquals(Locale.ENGLISH, loadedLabel.getLocale());
        assertEquals("foo", loadedLabel.getValue());
        assertEquals("bar", loadedLabel.getPluralValue());
    }

}
