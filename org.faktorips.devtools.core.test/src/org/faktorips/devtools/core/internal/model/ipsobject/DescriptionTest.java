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
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DescriptionTest extends AbstractIpsPluginTest {

    private IDescription description;

    private IIpsProject ipsProject;

    private IPolicyCmptType policyCmptType;

    private IPolicyCmptTypeAttribute attribute;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyCmptType(ipsProject, "TestPolicy");
        attribute = (IPolicyCmptTypeAttribute)policyCmptType.newAttribute();
        description = attribute.newDescription();
    }

    public void testGetSetLocale() {
        assertNull(description.getLocale());
        description.setLocale(Locale.GERMAN);
        assertEquals(Locale.GERMAN, description.getLocale());
    }

    public void testGetSetText() {
        assertEquals("", description.getText());
        description.setText("foo");
        assertEquals("foo", description.getText());
        description.setText(null);
        assertEquals("", description.getText());
    }

    public void testXml() throws ParserConfigurationException, CoreException {
        description.setLocale(Locale.ENGLISH);
        description.setText("foo");

        Element xmlElement = policyCmptType.toXml(createXmlDocument(IDescription.XML_TAG_NAME));
        Node descriptionNode = xmlElement.getChildNodes().item(1).getChildNodes().item(2);
        NamedNodeMap descriptionAttributes = descriptionNode.getAttributes();
        assertEquals(Locale.ENGLISH.getLanguage(), descriptionAttributes.getNamedItem(IDescription.PROPERTY_LOCALE)
                .getTextContent());
        assertEquals("foo", descriptionNode.getTextContent());

        IPolicyCmptType loadedPolicyCmptType = newPolicyCmptType(ipsProject, "LoadedPolicy");
        loadedPolicyCmptType.initFromXml(xmlElement);
        IDescription loadedDescription = loadedPolicyCmptType.getAttributes()[0].getDescriptions().toArray(
                new IDescription[1])[0];
        assertEquals(Locale.ENGLISH, loadedDescription.getLocale());
        assertEquals("foo", loadedDescription.getText());
    }

}
