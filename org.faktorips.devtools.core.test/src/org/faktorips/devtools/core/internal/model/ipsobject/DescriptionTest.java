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
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class DescriptionTest extends AbstractIpsPluginTest {

    private IDescription description;

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
        description = attribute.newDescription();
    }

    @Test
    public void testSetLocale() {
        description.setLocale(Locale.GERMAN);
        assertEquals(Locale.GERMAN, description.getLocale());
    }

    @Test
    public void testSetText() {
        description.setText("foo");
        assertEquals("foo", description.getText());
    }

    @Test
    public void testSetTextNullPointer() {
        description.setText(null);
        assertEquals("", description.getText());
    }

    @Test
    public void testValidateLocaleMissing() throws CoreException {
        MessageList validationMessages = description.validate(ipsProject);
        assertEquals(1, validationMessages.getNoOfMessages());
        Message message = validationMessages.getFirstMessage(Message.ERROR);
        assertEquals(IDescription.MSGCODE_LOCALE_MISSING, message.getCode());
    }

    @Test
    public void testValidateLocaleNotSupported() throws CoreException {
        description.setLocale(Locale.TAIWAN);
        MessageList validationMessages = description.validate(ipsProject);
        assertEquals(1, validationMessages.getNoOfMessages());
        Message message = validationMessages.getFirstMessage(Message.WARNING);
        assertEquals(IDescription.MSGCODE_LOCALE_NOT_SUPPORTED_BY_IPS_PROJECT, message.getCode());
    }

    @Test
    public void testValidateLocaleNotSupportedByContextProject() throws CoreException {
        IIpsProject contextProject = newIpsProject("ContextProject");
        IIpsProjectProperties properties = contextProject.getProperties();
        properties.removeSupportedLanguage(Locale.GERMAN);
        contextProject.setProperties(properties);

        description.setLocale(Locale.GERMAN);
        MessageList validationMessages = description.validate(contextProject);
        assertTrue(validationMessages.isEmpty());
    }

    @Test
    public void testValidateOk() throws CoreException {
        description.setLocale(Locale.US);
        MessageList validationMessages = description.validate(ipsProject);
        assertEquals(0, validationMessages.getNoOfMessages());
    }

    @Test
    public void testXml() throws ParserConfigurationException {
        description.setLocale(Locale.ENGLISH);
        description.setText("foo");

        Element xmlElement = description.toXml(createXmlDocument(IDescription.XML_TAG_NAME));
        NamedNodeMap descriptionAttributes = xmlElement.getAttributes();
        assertEquals(Locale.ENGLISH.getLanguage(), descriptionAttributes.getNamedItem(IDescription.PROPERTY_LOCALE)
                .getTextContent());
        assertEquals("foo", xmlElement.getTextContent());

        IDescription loadedDescription = policyCmptType.newDescription();
        loadedDescription.initFromXml(xmlElement);
        assertEquals(Locale.ENGLISH, loadedDescription.getLocale());
        assertEquals("foo", loadedDescription.getText());
    }

}
