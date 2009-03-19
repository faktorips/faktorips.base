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

package org.faktorips.devtools.core.model.enums;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.enums.EnumsUtil;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public class EnumsUtilTest extends AbstractIpsPluginTest {

    private final String STRING_DATATYPE_NAME = "String";
    private final String INTEGER_DATATYPE_NAME = "Integer";

    private IIpsProject ipsProject;

    private IEnumType enumType;
    private IEnumType superEnumType;
    private IEnumAttribute enumAttribute1;
    private IEnumAttribute enumAttribute2;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();
        enumType = newEnumType(ipsProject, "EnumType");
        superEnumType = newEnumType(ipsProject, "SuperEnumType");

        enumAttribute1 = enumType.newEnumAttribute();
        enumAttribute1.setName("attr1");
        enumAttribute1.setDatatype("String");
        enumAttribute1.setIdentifier(true);

        enumAttribute2 = superEnumType.newEnumAttribute();
        enumAttribute2.setName("attr2");
        enumAttribute2.setDatatype(INTEGER_DATATYPE_NAME);
        enumAttribute2.setIdentifier(false);
    }

    public void testEqualEnumAttributes() throws CoreException {
        assertFalse(EnumsUtil.equalEnumAttributes(enumAttribute1, enumAttribute2));

        enumAttribute2.setName("attr1");
        assertFalse(EnumsUtil.equalEnumAttributes(enumAttribute1, enumAttribute2));

        enumAttribute2.setDatatype(STRING_DATATYPE_NAME);
        assertFalse(EnumsUtil.equalEnumAttributes(enumAttribute1, enumAttribute2));

        enumAttribute2.setIdentifier(true);
        assertTrue(EnumsUtil.equalEnumAttributes(enumAttribute1, enumAttribute2));
    }

    public void testContainsEqualEnumAttribute() {
        assertFalse(EnumsUtil.containsEqualEnumAttribute(enumType.getEnumAttributes(), enumAttribute2));
        assertTrue(EnumsUtil.containsEqualEnumAttribute(enumType.getEnumAttributes(), enumAttribute1));
    }

    public void testSplitProjectAndSourceFolder() {
        String test = null;
        try {
            EnumsUtil.splitProjectAndSourceFolder(test);
            fail();
        } catch (NullPointerException e) {
        }

        test = "/";
        Object[] array = EnumsUtil.splitProjectAndSourceFolder(test);
        assertEquals("", array[0]);
        assertEquals(null, array[1]);

        test = "TestProject/source";
        array = EnumsUtil.splitProjectAndSourceFolder(test);
        assertEquals("source", array[0]);
        assertEquals(ipsProject, array[1]);
    }

}
