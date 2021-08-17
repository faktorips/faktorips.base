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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.enums.EnumContent;
import org.faktorips.devtools.model.internal.enums.EnumType;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.IDerivedValueSet;
import org.faktorips.devtools.model.valueset.IUnrestrictedValueSet;
import org.junit.Before;
import org.junit.Test;

public class DerivedValueSetTest extends AbstractIpsPluginTest {

    private static final String MY_ENUM_CONTENT = "MyEnumContent";

    private static final String MY_EXTENSIBLE_ENUM = "MyExtensibleEnum";

    private static final String MY_SUPER_ENUM = "MySuperEnum";

    private IPolicyCmptTypeAttribute attr;

    private IIpsProject ipsProject;

    private IPolicyCmptType policyCmptType;

    private IIpsProject productIpsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject();
        productIpsProject = super.newIpsProject();
        IIpsObjectPath ipsObjectPath = productIpsProject.getIpsObjectPath();
        ipsObjectPath.newIpsProjectRefEntry(ipsProject);
        productIpsProject.setIpsObjectPath(ipsObjectPath);
        policyCmptType = newPolicyCmptType(ipsProject, "test.Base");
        attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("attr");
        attr.setDatatype(Datatype.STRING.getQualifiedName());

        EnumType enumType = newEnumType(ipsProject, MY_EXTENSIBLE_ENUM);
        enumType.setExtensible(true);
        enumType.setSuperEnumType(MY_SUPER_ENUM);
        enumType.setEnumContentName(MY_ENUM_CONTENT);
        EnumContent newEnumContent = newEnumContent(productIpsProject, MY_ENUM_CONTENT);
        newEnumContent.setEnumType(MY_EXTENSIBLE_ENUM);

        EnumType superEnumType = newEnumType(ipsProject, MY_SUPER_ENUM);
        superEnumType.setAbstract(true);
    }

    @Test
    public void testDerivedValueSet() {
        IDerivedValueSet derivedValueSet = new DerivedValueSet(attr, "1");
        assertTrue(derivedValueSet.isContainsNull());
    }

    @Test
    public void testCopy() throws Exception {
        IDerivedValueSet derivedValueSet = new DerivedValueSet(attr, "1");

        IDerivedValueSet derivedValueSet2 = (IDerivedValueSet)derivedValueSet.copy(attr, "2");
        assertTrue(derivedValueSet2.compareTo(derivedValueSet) == 0);
    }

    @Test
    public void testIsContainsNull() throws Exception {
        IDerivedValueSet derivedValueSet = new DerivedValueSet(attr, "1");

        assertTrue(derivedValueSet.isContainsNull());
    }

    @Test
    public void testIsContainsNull_Primitive() throws Exception {
        attr.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
        IDerivedValueSet derivedValueSet = new DerivedValueSet(attr, "1");

        assertFalse(derivedValueSet.isContainsNull());
    }

    @Test
    public void testIsContainsNull_UnknownDatatype() throws Exception {
        attr.setDatatype("Ceci n’est pas un type de données");
        IDerivedValueSet derivedValueSet = new DerivedValueSet(attr, "1");

        assertTrue(derivedValueSet.isContainsNull());
    }

    @Test
    public void testIsContainsNull_MissingOverwrittenAttribute() throws Exception {
        attr.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
        attr.setOverwrite(true);
        IDerivedValueSet derivedValueSet = new DerivedValueSet(attr, "1");

        // the normal implementation should be used if the overwritten attribute cannot be found
        assertFalse(derivedValueSet.isContainsNull());
    }

    @Test
    public void testIsContainsNull_Overwritten_WithoutNull() throws Exception {
        PolicyCmptType subPolicyCmptType = newPolicyCmptType(ipsProject, "test.Sub");
        subPolicyCmptType.setSupertype(policyCmptType.getQualifiedName());
        IPolicyCmptTypeAttribute subAttr = subPolicyCmptType.newPolicyCmptTypeAttribute();
        subAttr.setOverwrite(true);
        subAttr.setName("attr");
        subAttr.setDatatype(Datatype.STRING.getQualifiedName());
        attr.setValueSetCopy(new UnrestrictedValueSet(attr, "0", false));
        IDerivedValueSet derivedValueSet = new DerivedValueSet(subAttr, "1");

        assertFalse(derivedValueSet.isContainsNull());
    }

    @Test
    public void testIsContainsNull_Overwritten_WithNull() throws Exception {
        PolicyCmptType subPolicyCmptType = newPolicyCmptType(ipsProject, "test.Sub");
        subPolicyCmptType.setSupertype(policyCmptType.getQualifiedName());
        IPolicyCmptTypeAttribute subAttr = subPolicyCmptType.newPolicyCmptTypeAttribute();
        subAttr.setOverwrite(true);
        subAttr.setName("attr");
        subAttr.setDatatype(Datatype.STRING.getQualifiedName());
        attr.setValueSetCopy(new UnrestrictedValueSet(attr, "0", true));
        IDerivedValueSet derivedValueSet = new DerivedValueSet(subAttr, "1");

        assertTrue(derivedValueSet.isContainsNull());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetContainsNull() throws Exception {
        IDerivedValueSet derivedValueSet = new DerivedValueSet(attr, "1");
        derivedValueSet.setContainsNull(false);
    }

    @Test
    public void testContainsValue() throws Exception {
        attr.setDatatype(Datatype.MONEY.getQualifiedName());
        IDerivedValueSet derivedValueSet = new DerivedValueSet(attr, "1");

        assertTrue(derivedValueSet.containsValue("10EUR", ipsProject));
    }

    @Test
    public void testContainsValue_DatatypeIsNull() throws Exception {
        attr.setDatatype(null);
        IDerivedValueSet derivedValueSet = new DerivedValueSet(attr, "1");

        assertFalse(derivedValueSet.containsValue("someValue", ipsProject));
    }

    @Test
    public void testContainsValue_isNotParsableValue() throws Exception {
        attr.setDatatype(Datatype.MONEY.getQualifiedName());
        IDerivedValueSet derivedValueSet = new DerivedValueSet(attr, "1");

        assertFalse(derivedValueSet.containsValue("notParsable", ipsProject));
    }

    @Test
    public void testContainsValue_ValueIsNull() throws CoreException {
        IDerivedValueSet derivedValueSet = new DerivedValueSet(attr, "1");

        assertTrue(derivedValueSet.containsValue(null, ipsProject));
    }

    @Test
    public void testContainsValueSet_EqualValueSets() {
        IDerivedValueSet derivedValueSet = new DerivedValueSet(attr, "1");
        DerivedValueSet subSet = new DerivedValueSet(attr, "1");

        assertTrue(derivedValueSet.containsValueSet(subSet));
    }

    @Test
    public void testContainsValueSet_enum() {
        attr.setDatatype(MY_EXTENSIBLE_ENUM);

        IDerivedValueSet derivedValueSet = new DerivedValueSet(attr, "1");
        DerivedValueSet subSet = new DerivedValueSet(attr, "1");

        assertTrue(derivedValueSet.containsValueSet(subSet));
    }

    @Test
    public void testContainsValueSet_differentDatatypes() throws Exception {
        IPolicyCmptTypeAttribute attr2 = policyCmptType.newPolicyCmptTypeAttribute();
        attr2.setName("attr");
        attr2.setDatatype(MY_EXTENSIBLE_ENUM);

        IDerivedValueSet derivedValueSet = new DerivedValueSet(attr, "1");
        DerivedValueSet subSet = new DerivedValueSet(attr2, "1");

        assertFalse(subSet.containsValueSet(derivedValueSet));
    }

    @Test
    public void testContainsValueSet_covariant() throws Exception {
        attr.setDatatype(MY_SUPER_ENUM);
        IPolicyCmptTypeAttribute attr2 = policyCmptType.newPolicyCmptTypeAttribute();
        attr2.setName("attr");
        attr2.setDatatype(MY_EXTENSIBLE_ENUM);

        IDerivedValueSet derivedValueSet = new DerivedValueSet(attr, "1");
        DerivedValueSet subSet = new DerivedValueSet(attr2, "1");

        assertTrue(derivedValueSet.containsValueSet(subSet));
        assertFalse(subSet.containsValueSet(derivedValueSet));
    }

    @Test
    public void testCompareTo() throws Exception {
        IDerivedValueSet u1 = new DerivedValueSet(attr, "1");
        IDerivedValueSet u2 = new DerivedValueSet(attr, "1");

        assertThat(u1.compareTo(u2), is(0));
        assertThat(u2.compareTo(u1), is(0));
    }

    @Test
    public void testCompareTo_Unrestricted() throws Exception {
        IDerivedValueSet derivedValueSet = new DerivedValueSet(attr, "1");
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(attr, "1");

        assertTrue(derivedValueSet.compareTo(unrestrictedValueSet) < 0);
        assertTrue(unrestrictedValueSet.compareTo(derivedValueSet) > 0);
    }

}
