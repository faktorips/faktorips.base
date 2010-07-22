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

package org.faktorips.devtools.core.internal.model.enums;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumAttributeReference;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class EnumAttributeReferenceTest extends AbstractIpsEnumPluginTest {

    private IEnumAttributeReference genderIdReference;
    private IEnumAttributeReference genderNameReference;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        genderIdReference = genderEnumContent.getEnumAttributeReferences().get(0);
        genderNameReference = genderEnumContent.getEnumAttributeReferences().get(1);
    }

    public void testGetSetName() {
        assertEquals(GENDER_ENUM_ATTRIBUTE_ID_NAME, genderIdReference.getName());
        genderIdReference.setName("foo");
        assertEquals("foo", genderIdReference.getName());
    }

    public void testXml() throws ParserConfigurationException, CoreException {
        Element xmlElement = genderEnumContent.toXml(createXmlDocument(IEnumAttributeReference.XML_TAG));
        assertEquals(5, xmlElement.getChildNodes().getLength());
        NamedNodeMap attributesIdReference = xmlElement.getChildNodes().item(3).getAttributes();
        assertEquals(GENDER_ENUM_ATTRIBUTE_ID_NAME, attributesIdReference.getNamedItem(IIpsElement.PROPERTY_NAME)
                .getTextContent());

        IEnumContent loadedEnumContent = newEnumContent(ipsProject, "LoadedEnumContent");
        loadedEnumContent.initFromXml(xmlElement);
        assertEquals(2, loadedEnumContent.getEnumAttributeReferencesCount());
        assertEquals(genderIdReference.getName(), loadedEnumContent.getEnumAttributeReferences().get(0).getName());
        assertEquals(genderNameReference.getName(), loadedEnumContent.getEnumAttributeReferences().get(1).getName());
    }

    public void testIsDescriptionChangable() {
        assertFalse(genderIdReference.isDescriptionChangable());
    }

}
