/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.valueset;

import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.enums.EnumContent;
import org.faktorips.devtools.model.internal.enums.EnumType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.IUnrestrictedValueSet;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests a common case when overriding attributes while using enum types as datatypes.
 * <p>
 * The problem arises when a base attribute knows only its enum type, but the overriding attribute
 * knows both the enum type as well as an enum content. The content is introduced in the same
 * project. {@link ValueSet#containsValueSet(org.faktorips.devtools.model.valueset.IValueSet)} must
 * return <code>true</code> in this case.
 */
public class ContainsValueSetEnumDatatpeTest extends AbstractIpsPluginTest {

    private IPolicyCmptTypeAttribute baseAttr;
    private IPolicyCmptTypeAttribute attr;

    private IIpsProject baseProject;
    private IIpsProject ipsProject;

    private IPolicyCmptType policyCmptType;
    private IPolicyCmptType stdPolicyCmptType;
    private EnumType enumType;
    private EnumContent enumContent;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        baseProject = super.newIpsProject("BaseProject");

        enumType = newEnumType(baseProject, "test.EnumType");
        enumType.setExtensible(true);

        stdPolicyCmptType = newPolicyCmptType(baseProject, "test.StdPolicy");
        baseAttr = stdPolicyCmptType.newPolicyCmptTypeAttribute();
        baseAttr.setName("baseAttr");
        baseAttr.setDatatype(enumType.getQualifiedName());

        ipsProject = super.newIpsProject("Project");
        IIpsObjectPath objectPath = ipsProject.getIpsObjectPath();
        objectPath.newIpsProjectRefEntry(baseProject);
        ipsProject.setIpsObjectPath(objectPath);

        enumContent = newEnumContent(ipsProject, "test.EnumContent");
        enumContent.setEnumType(enumType.getQualifiedName());
        enumType.setEnumContentName(enumContent.getQualifiedName());

        policyCmptType = newPolicyCmptType(ipsProject, "test.Policy");
        attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("attr");
        attr.setDatatype(enumType.getQualifiedName());
    }

    @Test
    public void testContainsValueSet_Unrestricted_And_EnumValueset_with_EnumDatatype() {
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(baseAttr, "1", false);
        EnumValueSet enumValueSet = new EnumValueSet(attr, Collections.<String> emptyList(), "2");

        assertTrue(unrestrictedValueSet.containsValueSet(enumValueSet));
    }

    @Test
    public void testContainsValueSet_EnumValuesets_with_EnumDatatype() {
        EnumValueSet baseEnumValueSet = new EnumValueSet(baseAttr, Collections.<String> emptyList(), "1");
        EnumValueSet enumValueSet = new EnumValueSet(attr, Collections.<String> emptyList(), "2");

        assertTrue(baseEnumValueSet.containsValueSet(enumValueSet));
    }

}
