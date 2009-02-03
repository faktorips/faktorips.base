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
import org.faktorips.devtools.core.model.enumtype.IEnumValues;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.w3c.dom.Element;

public class EnumValuesTest extends AbstractIpsEnumPluginTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testGetSetEnumType() throws CoreException {
        assertEquals(genderEnumType.getQualifiedName(), genderEnumValues.getEnumType());
        try {
            genderEnumValues.setEnumType(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testGetIpsObjectType() {
        assertEquals(IpsObjectType.ENUM_VALUES, genderEnumValues.getIpsObjectType());
    }

    public void testFindEnumType() throws CoreException {
        assertEquals(genderEnumType, genderEnumValues.findEnumType());
    }

    public void testXml() throws ParserConfigurationException, CoreException {
        Element xmlElement = genderEnumValues.toXml(createXmlDocument(IEnumValues.XML_TAG));
        assertEquals(genderEnumType.getQualifiedName(), xmlElement.getAttribute(IEnumValues.XML_ATTRIBUTE_ENUM_TYPE));

        IEnumValues loadedEnumValues = newEnumValues(ipsProject, "LoadedEnumValues");
        loadedEnumValues.initFromXml(xmlElement);
        assertEquals(genderEnumType.getQualifiedName(), loadedEnumValues.getEnumType());
    }

}
