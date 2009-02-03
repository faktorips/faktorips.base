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

import org.faktorips.devtools.core.model.enumtype.IEnumValue;

public class EnumValueContainerTest extends AbstractIpsEnumPluginTest {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testGetEnumValues() {
        assertEquals(2, genderEnumValues.getEnumValues().size());
    }

    public void testGetEnumValue() {
        assertEquals(genderEnumMaleValue, genderEnumValues.getEnumValue(0));
    }

    public void testNewEnumValue() {
        IEnumValue newEnumValue = genderEnumValues.newEnumValue();
        assertEquals(newEnumValue, genderEnumValues.getEnumValue(2));
    }

}
