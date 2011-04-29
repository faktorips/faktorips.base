/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.enums;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.enums.AbstractIpsEnumPluginTest;
import org.junit.Before;
import org.junit.Test;

public class EnumUtilTest extends AbstractIpsEnumPluginTest {

    private IEnumType superEnumType;
    private IEnumAttribute inheritedAttribute;
    private IEnumAttribute superAttribute;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        superEnumType = newEnumType(ipsProject, "SuperEnumType");
        superEnumType.setAbstract(true);
        paymentMode.setSuperEnumType(superEnumType.getQualifiedName());
        inheritedAttribute = paymentMode.newEnumAttribute();
        inheritedAttribute.setName("inherited");
        superAttribute = superEnumType.newEnumAttribute();
        superAttribute.setName("inherited");
    }

    @Test
    public void testFindEnumAttributeIsUnique() throws CoreException {
        assertFalse(EnumUtil.findEnumAttributeIsUnique(inheritedAttribute, ipsProject));
        inheritedAttribute.setUnique(true);
        inheritedAttribute.setInherited(true);
        assertFalse(EnumUtil.findEnumAttributeIsUnique(inheritedAttribute, ipsProject));
        superAttribute.setUnique(true);
        assertTrue(EnumUtil.findEnumAttributeIsUnique(inheritedAttribute, ipsProject));
    }

    @Test
    public void testFindEnumAttributeIsIdentifier() throws CoreException {
        assertFalse(EnumUtil.findEnumAttributeIsIdentifier(inheritedAttribute, ipsProject));
        inheritedAttribute.setIdentifier(true);
        inheritedAttribute.setInherited(true);
        assertFalse(EnumUtil.findEnumAttributeIsIdentifier(inheritedAttribute, ipsProject));
        superAttribute.setIdentifier(true);
        assertTrue(EnumUtil.findEnumAttributeIsIdentifier(inheritedAttribute, ipsProject));
    }

    @Test
    public void testFindEnumAttributeIsUsedAsNameInFaktorIpsUi() throws CoreException {
        assertFalse(EnumUtil.findEnumAttributeIsUsedAsNameInFaktorIpsUi(inheritedAttribute, ipsProject));
        inheritedAttribute.setUsedAsNameInFaktorIpsUi(true);
        inheritedAttribute.setInherited(true);
        assertFalse(EnumUtil.findEnumAttributeIsUsedAsNameInFaktorIpsUi(inheritedAttribute, ipsProject));
        superAttribute.setUsedAsNameInFaktorIpsUi(true);
        assertTrue(EnumUtil.findEnumAttributeIsUsedAsNameInFaktorIpsUi(inheritedAttribute, ipsProject));
    }

}
