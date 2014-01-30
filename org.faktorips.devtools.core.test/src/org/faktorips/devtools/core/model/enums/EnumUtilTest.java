/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
        assertFalse(inheritedAttribute.findIsUnique(ipsProject));
        inheritedAttribute.setUnique(true);
        inheritedAttribute.setInherited(true);
        assertFalse(inheritedAttribute.findIsUnique(ipsProject));
        superAttribute.setUnique(true);
        assertTrue(inheritedAttribute.findIsUnique(ipsProject));
    }

    @Test
    public void testFindEnumAttributeIsIdentifier() {
        assertFalse(inheritedAttribute.findIsIdentifier(ipsProject));
        inheritedAttribute.setIdentifier(true);
        inheritedAttribute.setInherited(true);
        assertFalse(inheritedAttribute.findIsIdentifier(ipsProject));
        superAttribute.setIdentifier(true);
        assertTrue(inheritedAttribute.findIsIdentifier(ipsProject));
    }

    @Test
    public void testFindEnumAttributeIsUsedAsNameInFaktorIpsUi() throws CoreException {
        assertFalse(inheritedAttribute.findIsUsedAsNameInFaktorIpsUi(ipsProject));
        inheritedAttribute.setUsedAsNameInFaktorIpsUi(true);
        inheritedAttribute.setInherited(true);
        assertFalse(inheritedAttribute.findIsUsedAsNameInFaktorIpsUi(ipsProject));
        superAttribute.setUsedAsNameInFaktorIpsUi(true);
        assertTrue(inheritedAttribute.findIsUsedAsNameInFaktorIpsUi(ipsProject));
    }

}
