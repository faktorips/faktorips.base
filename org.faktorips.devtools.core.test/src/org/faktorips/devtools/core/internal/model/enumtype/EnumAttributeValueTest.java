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

package org.faktorips.devtools.core.internal.model.enumtype;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.enumtype.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enumtype.IEnumContent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EnumAttributeValueTest extends AbstractIpsEnumPluginTest {

    private IEnumAttributeValue maleIdAttributeValue;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        maleIdAttributeValue = genderEnumValueMale.getEnumAttributeValue(0);
    }

    public void testGetEnumAttribute() {
        assertEquals(genderEnumAttributeId, maleIdAttributeValue.getEnumAttribute());
    }

    public void testGetSetValue() {
        maleIdAttributeValue.setValue("otherValue");
        assertEquals("otherValue", maleIdAttributeValue.getValue());

        try {
            maleIdAttributeValue.setValue(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testXml() throws ParserConfigurationException, CoreException {
        Element xmlElement = genderEnumContent.toXml(createXmlDocument(IEnumContent.XML_TAG));
        // Get first enum attribute value of the first enum value
        Node firstEnumAttributeValue = xmlElement.getChildNodes().item(1).getChildNodes().item(1);
        assertEquals(GENDER_ENUM_LITERAL_MALE_ID, firstEnumAttributeValue.getAttributes().getNamedItem(
                IEnumAttributeValue.PROPERTY_VALUE).getTextContent());
        assertEquals(1 + 2, xmlElement.getChildNodes().getLength());

        IEnumContent loadedEnumContent = newEnumContent(ipsProject, "LoadedEnumValues");
        loadedEnumContent.initFromXml(xmlElement);
        assertEquals(GENDER_ENUM_LITERAL_MALE_ID, loadedEnumContent.getEnumValues().get(0).getEnumAttributeValue(0)
                .getValue());
        assertEquals(2, loadedEnumContent.getEnumValues().size());
    }
}
