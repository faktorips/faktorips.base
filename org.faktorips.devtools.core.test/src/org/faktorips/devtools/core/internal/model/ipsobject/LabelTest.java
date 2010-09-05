/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class LabelTest extends AbstractIpsPluginTest {

    private ILabel label;

    private IIpsProject ipsProject;

    private IPolicyCmptType policyCmptType;

    private IPolicyCmptTypeAttribute attribute;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyCmptType(ipsProject, "TestPolicy");
        attribute = (IPolicyCmptTypeAttribute)policyCmptType.newAttribute();
        label = attribute.newLabel();
    }

    public void testGetSetLocale() {
        assertNull(label.getLocale());
        label.setLocale(Locale.GERMAN);
        assertEquals(Locale.GERMAN, label.getLocale());
    }

    public void testGetSetValue() {
        assertNull(label.getValue());
        label.setValue("foo");
        assertEquals("foo", label.getValue());
    }

    public void testGetSetPluralValue() {
        assertNull(label.getPluralValue());
        label.setPluralValue("bar");
        assertEquals("bar", label.getPluralValue());
    }

    public void testValidateLocaleMissing() throws CoreException {
        MessageList validationMessages = label.validate(ipsProject);
        assertEquals(1, validationMessages.getNoOfMessages());
        Message message = validationMessages.getFirstMessage(Message.ERROR);
        assertEquals(ILabel.MSGCODE_LOCALE_MISSING, message.getCode());
    }

    public void testValidateLocaleNotSupported() throws CoreException {
        label.setLocale(Locale.CANADA);
        MessageList validationMessages = label.validate(ipsProject);
        assertEquals(1, validationMessages.getNoOfMessages());
        Message message = validationMessages.getFirstMessage(Message.ERROR);
        assertEquals(ILabel.MSGCODE_LOCALE_NOT_SUPPORTED_BY_IPS_PROJECT, message.getCode());
    }

    public void testValidateOk() throws CoreException {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.addSupportedLanguage(Locale.ENGLISH);
        ipsProject.setProperties(properties);
        label.setLocale(Locale.ENGLISH);
        MessageList validationMessages = label.validate(ipsProject);
        assertEquals(0, validationMessages.getNoOfMessages());
    }

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

        ILabel loadedLabel = policyCmptType.newLabel();
        loadedLabel.initFromXml(xmlElement);
        assertEquals(Locale.ENGLISH, loadedLabel.getLocale());
        assertEquals("foo", loadedLabel.getValue());
        assertEquals("bar", loadedLabel.getPluralValue());
    }

}
