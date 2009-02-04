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
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.enumtype.IEnumAttribute;
import org.faktorips.devtools.core.model.enumtype.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enumtype.IEnumContent;
import org.w3c.dom.Element;

public class EnumValueTest extends AbstractIpsEnumPluginTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testNewEnumAttributeValue() throws CoreException {
        try {
            genderEnumContent.newEnumValue().newEnumAttributeValue();
            fail("It is not allowed to have more enum attribute values than enum attributes.");
        } catch (IllegalStateException e) {
        }

        genderEnumValueMale.getEnumAttributeValue(1).delete();
        assertNotNull(genderEnumValueMale.newEnumAttributeValue());
    }

    public void testGetEnumAttributeValues() {
        assertEquals(2, genderEnumValueMale.getEnumAttributeValues().size());
    }

    public void testMoveEnumAttributeValueUp() throws CoreException {
        try {
            genderEnumValueMale.moveEnumAttributeValueUp(null);
            fail();
        } catch (NullPointerException e) {
        }

        IEnumAttribute newEnumAttribute = genderEnumType.newEnumAttribute();
        IEnumAttributeValue valueId = genderEnumValueMale.getEnumAttributeValue(0);
        IEnumAttributeValue valueName = genderEnumValueMale.getEnumAttributeValue(1);
        IEnumAttributeValue valueNew = genderEnumValueMale.getEnumAttributeValue(2);

        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(1));
        assertEquals(valueNew, genderEnumValueMale.getEnumAttributeValues().get(2));

        genderEnumValueMale.moveEnumAttributeValueUp(newEnumAttribute);
        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueNew, genderEnumValueMale.getEnumAttributeValues().get(1));
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(2));

        genderEnumValueMale.moveEnumAttributeValueUp(newEnumAttribute);
        assertEquals(valueNew, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(1));
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(2));

        // Nothing must change if the enum attribute value is the first one already
        genderEnumValueMale.moveEnumAttributeValueUp(newEnumAttribute);
        assertEquals(valueNew, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(1));
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(2));
    }

    public void testMoveEnumAttributeValueDown() throws CoreException {
        try {
            genderEnumValueMale.moveEnumAttributeValueDown(null);
            fail();
        } catch (NullPointerException e) {
        }

        genderEnumType.newEnumAttribute();
        IEnumAttributeValue valueId = genderEnumValueMale.getEnumAttributeValue(0);
        IEnumAttributeValue valueName = genderEnumValueMale.getEnumAttributeValue(1);
        IEnumAttributeValue valueNew = genderEnumValueMale.getEnumAttributeValue(2);

        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(1));
        assertEquals(valueNew, genderEnumValueMale.getEnumAttributeValues().get(2));

        genderEnumValueMale.moveEnumAttributeValueDown(genderEnumAttributeId);
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(1));
        assertEquals(valueNew, genderEnumValueMale.getEnumAttributeValues().get(2));

        genderEnumValueMale.moveEnumAttributeValueDown(genderEnumAttributeId);
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueNew, genderEnumValueMale.getEnumAttributeValues().get(1));
        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(2));

        // Nothing must change if the enum attribute value is the first one already
        genderEnumValueMale.moveEnumAttributeValueDown(genderEnumAttributeId);
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueNew, genderEnumValueMale.getEnumAttributeValues().get(1));
        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(2));
    }

    public void testXml() throws ParserConfigurationException, CoreException {
        Element xmlElement = genderEnumContent.toXml(createXmlDocument(IEnumContent.XML_TAG));
        assertEquals(1 + 2, xmlElement.getChildNodes().getLength());

        IEnumContent loadedEnumContent = newEnumContent(ipsProject, "LoadedEnumValues");
        loadedEnumContent.initFromXml(xmlElement);
        assertEquals(2, loadedEnumContent.getEnumValues().size());
    }

    public void testValidateThis() throws CoreException {
        assertTrue(genderEnumValueFemale.isValid());

        IIpsModel ipsModel = getIpsModel();

        ipsModel.clearValidationCache();
        genderEnumValueFemale.getEnumAttributeValue(0).delete();
        assertEquals(1, genderEnumValueFemale.validate(ipsProject).getNoOfMessages());
    }

}
