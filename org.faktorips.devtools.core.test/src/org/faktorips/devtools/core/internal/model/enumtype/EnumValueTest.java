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
import org.faktorips.devtools.core.model.enumtype.IEnumAttribute;
import org.faktorips.devtools.core.model.enumtype.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enumtype.IEnumValues;
import org.w3c.dom.Element;

public class EnumValueTest extends AbstractIpsEnumPluginTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testNewEnumAttributeValue() throws CoreException {
        try {
            genderEnumValues.newEnumValue().newEnumAttributeValue();
            fail("It is not allowed to have more enum attribute values than enum attributes.");
        } catch (IllegalStateException e) {
        }

        genderEnumMaleValue.getEnumAttributeValue(1).delete();
        assertNotNull(genderEnumMaleValue.newEnumAttributeValue());
    }

    public void testGetEnumAttributeValues() {
        assertEquals(2, genderEnumMaleValue.getEnumAttributeValues().size());
    }

    public void testMoveEnumAttributeValueUp() throws CoreException {
        try {
            genderEnumMaleValue.moveEnumAttributeValueUp(null);
            fail();
        } catch (NullPointerException e) {
        }

        IEnumAttribute newEnumAttribute = genderEnumType.newEnumAttribute();
        IEnumAttributeValue valueId = genderEnumMaleValue.getEnumAttributeValue(0);
        IEnumAttributeValue valueName = genderEnumMaleValue.getEnumAttributeValue(1);
        IEnumAttributeValue valueNew = genderEnumMaleValue.getEnumAttributeValue(2);

        assertEquals(valueId, genderEnumMaleValue.getEnumAttributeValues().get(0));
        assertEquals(valueName, genderEnumMaleValue.getEnumAttributeValues().get(1));
        assertEquals(valueNew, genderEnumMaleValue.getEnumAttributeValues().get(2));

        genderEnumMaleValue.moveEnumAttributeValueUp(newEnumAttribute);
        assertEquals(valueId, genderEnumMaleValue.getEnumAttributeValues().get(0));
        assertEquals(valueNew, genderEnumMaleValue.getEnumAttributeValues().get(1));
        assertEquals(valueName, genderEnumMaleValue.getEnumAttributeValues().get(2));

        genderEnumMaleValue.moveEnumAttributeValueUp(newEnumAttribute);
        assertEquals(valueNew, genderEnumMaleValue.getEnumAttributeValues().get(0));
        assertEquals(valueId, genderEnumMaleValue.getEnumAttributeValues().get(1));
        assertEquals(valueName, genderEnumMaleValue.getEnumAttributeValues().get(2));

        // Nothing must change if the enum attribute value is the first one already
        genderEnumMaleValue.moveEnumAttributeValueUp(newEnumAttribute);
        assertEquals(valueNew, genderEnumMaleValue.getEnumAttributeValues().get(0));
        assertEquals(valueId, genderEnumMaleValue.getEnumAttributeValues().get(1));
        assertEquals(valueName, genderEnumMaleValue.getEnumAttributeValues().get(2));
    }

    public void testMoveEnumAttributeValueDown() throws CoreException {
        try {
            genderEnumMaleValue.moveEnumAttributeValueDown(null);
            fail();
        } catch (NullPointerException e) {
        }

        genderEnumType.newEnumAttribute();
        IEnumAttributeValue valueId = genderEnumMaleValue.getEnumAttributeValue(0);
        IEnumAttributeValue valueName = genderEnumMaleValue.getEnumAttributeValue(1);
        IEnumAttributeValue valueNew = genderEnumMaleValue.getEnumAttributeValue(2);

        assertEquals(valueId, genderEnumMaleValue.getEnumAttributeValues().get(0));
        assertEquals(valueName, genderEnumMaleValue.getEnumAttributeValues().get(1));
        assertEquals(valueNew, genderEnumMaleValue.getEnumAttributeValues().get(2));

        genderEnumMaleValue.moveEnumAttributeValueDown(genderEnumAttributeId);
        assertEquals(valueName, genderEnumMaleValue.getEnumAttributeValues().get(0));
        assertEquals(valueId, genderEnumMaleValue.getEnumAttributeValues().get(1));
        assertEquals(valueNew, genderEnumMaleValue.getEnumAttributeValues().get(2));

        genderEnumMaleValue.moveEnumAttributeValueDown(genderEnumAttributeId);
        assertEquals(valueName, genderEnumMaleValue.getEnumAttributeValues().get(0));
        assertEquals(valueNew, genderEnumMaleValue.getEnumAttributeValues().get(1));
        assertEquals(valueId, genderEnumMaleValue.getEnumAttributeValues().get(2));

        // Nothing must change if the enum attribute value is the first one already
        genderEnumMaleValue.moveEnumAttributeValueDown(genderEnumAttributeId);
        assertEquals(valueName, genderEnumMaleValue.getEnumAttributeValues().get(0));
        assertEquals(valueNew, genderEnumMaleValue.getEnumAttributeValues().get(1));
        assertEquals(valueId, genderEnumMaleValue.getEnumAttributeValues().get(2));
    }

    public void testXml() throws ParserConfigurationException, CoreException {
        Element xmlElement = genderEnumValues.toXml(createXmlDocument(IEnumValues.XML_TAG));
        assertEquals(1 + 2, xmlElement.getChildNodes().getLength());

        IEnumValues loadedEnumValues = newEnumValues(ipsProject, "LoadedEnumValues");
        loadedEnumValues.initFromXml(xmlElement);
        assertEquals(2, loadedEnumValues.getEnumValues().size());
    }

}
