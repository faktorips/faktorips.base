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
import org.faktorips.devtools.core.internal.model.ipsobject.DescriptionHelper;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

public class EnumValueTest extends AbstractIpsEnumPluginTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testNewEnumAttributeValue() throws CoreException {
        assertNotNull(genderEnumValueMale.newEnumAttributeValue());
    }

    public void testGetEnumAttributeValues() {
        assertEquals(2, genderEnumValueMale.getEnumAttributeValues().size());
    }

    public void testXml() throws ParserConfigurationException, CoreException {
        Element xmlElement = genderEnumContent.toXml(createXmlDocument(IEnumContent.XML_TAG));
        assertEquals(1 + 2, xmlElement.getChildNodes().getLength());
        Element enumValue = XmlUtil.getFirstElement(xmlElement, IEnumValue.XML_TAG);
        Element descriptionElement = XmlUtil.getFirstElement(enumValue, DescriptionHelper.XML_ELEMENT_NAME);
        assertNull(descriptionElement);
        IEnumContent loadedEnumContent = newEnumContent(ipsProject, "LoadedEnumValues");
        loadedEnumContent.initFromXml(xmlElement);
        assertEquals(2, loadedEnumContent.getEnumValues().size());
    }

    public void testValidateThis() throws CoreException {
        assertTrue(genderEnumValueFemale.isValid());
    }

    public void testValidateNumberEnumAttributeValues() throws CoreException {
        genderEnumType.newEnumAttribute();
        assertTrue(genderEnumValueFemale.isValid());

        getIpsModel().clearValidationCache();
        genderEnumValueFemale.getEnumAttributeValues().get(0).delete();
        assertEquals(1, genderEnumValueFemale.validate(ipsProject).getNoOfMessages());
    }

    public void testGetImage() {
        assertNull(genderEnumValueMale.getImage());
    }

    public void testGetEnumValueContainer() {
        assertEquals(genderEnumContent, genderEnumValueMale.getEnumValueContainer());
    }

    public void testFindEnumAttributeValue() throws CoreException {
        assertNull(genderEnumValueMale.findEnumAttributeValue(null));

        try {
            genderEnumValueMale.findEnumAttributeValue(paymentMode.getEnumAttributes().get(0));
            fail();
        } catch (IllegalArgumentException e) {
        }

        assertEquals(genderEnumValueMale.getEnumAttributeValues().get(0), genderEnumValueMale
                .findEnumAttributeValue(genderEnumAttributeId));
        assertEquals(genderEnumValueFemale.getEnumAttributeValues().get(0), genderEnumValueFemale
                .findEnumAttributeValue(genderEnumAttributeId));
        assertEquals(genderEnumValueMale.getEnumAttributeValues().get(1), genderEnumValueMale
                .findEnumAttributeValue(genderEnumAttributeName));
        assertEquals(genderEnumValueFemale.getEnumAttributeValues().get(1), genderEnumValueFemale
                .findEnumAttributeValue(genderEnumAttributeName));
    }

}
