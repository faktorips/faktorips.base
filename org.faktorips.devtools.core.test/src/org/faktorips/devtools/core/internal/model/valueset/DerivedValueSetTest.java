/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.valueset;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.enums.EnumContent;
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.valueset.IDerivedValueSet;
import org.faktorips.devtools.core.model.valueset.IUnrestrictedValueSet;
import org.junit.Before;
import org.junit.Test;

public class DerivedValueSetTest extends AbstractIpsPluginTest {

    private static final String MY_ENUM_CONTENT = "MyEnumContent";

    private static final String MY_EXTENSIBLE_ENUM = "MyExtensibleEnum";

    private static final String MY_SUPER_ENUM = "MySuperEnum";

    private IPolicyCmptTypeAttribute attr;
    private IConfiguredValueSet cValueSet;

    private IIpsProject ipsProject;
    private IProductCmptGeneration generation;

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
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "test.Base", "test.Product");
        IProductCmptType productCmptType = policyCmptType.findProductCmptType(ipsProject);
        attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("attr");
        attr.setDatatype(Datatype.STRING.getQualifiedName());

        IProductCmpt cmpt = newProductCmpt(productIpsProject, "test.Product");
        cmpt.setProductCmptType(productCmptType.getQualifiedName());
        generation = (IProductCmptGeneration)cmpt.newGeneration(new GregorianCalendar(20006, 4, 26));

        cValueSet = generation.newPropertyValue(attr, IConfiguredValueSet.class);

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
        IDerivedValueSet derivedValueSet = new DerivedValueSet(cValueSet, "1");
        assertTrue(derivedValueSet.isContainsNull());
    }

    @Test
    public void testCopy() throws Exception {
        IDerivedValueSet derivedValueSet = new DerivedValueSet(cValueSet, "1");

        IDerivedValueSet derivedValueSet2 = (IDerivedValueSet)derivedValueSet.copy(cValueSet, "2");
        assertTrue(derivedValueSet2.compareTo(derivedValueSet) == 0);
    }

    @Test
    public void testIsContainsNull() throws Exception {
        IDerivedValueSet derivedValueSet = new DerivedValueSet(cValueSet, "1");
        assertTrue(derivedValueSet.isContainsNull());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetContainsNull() throws Exception {
        IDerivedValueSet derivedValueSet = new DerivedValueSet(cValueSet, "1");
        derivedValueSet.setContainsNull(false);
    }

    @Test
    public void testIsContainsNullPrimitive() throws Exception {
        IDerivedValueSet derivedValueSet = new DerivedValueSet(cValueSet, "1");

        attr.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
        assertFalse(derivedValueSet.isContainsNull());
    }

    @Test
    public void testContainsValue() throws Exception {
        attr.setDatatype(Datatype.MONEY.getQualifiedName());
        IDerivedValueSet derivedValueSet = new DerivedValueSet(cValueSet, "1");

        assertTrue(derivedValueSet.containsValue("10EUR", ipsProject));
    }

    @Test
    public void testContainsValue_DatatypeIsNull() throws Exception {
        attr.setDatatype(null);
        IDerivedValueSet derivedValueSet = new DerivedValueSet(cValueSet, "1");

        assertFalse(derivedValueSet.containsValue("someValue", ipsProject));
    }

    @Test
    public void testContainsValue_isNotParsableValue() throws Exception {
        attr.setDatatype(Datatype.MONEY.getQualifiedName());
        IDerivedValueSet derivedValueSet = new DerivedValueSet(cValueSet, "1");

        assertFalse(derivedValueSet.containsValue("notParsable", ipsProject));
    }

    @Test
    public void testContainsValue_ValueIsNull() throws CoreException {
        IDerivedValueSet derivedValueSet = new DerivedValueSet(cValueSet, "1");

        assertTrue(derivedValueSet.containsValue(null, ipsProject));
    }

    @Test
    public void testContainsValueSet_EqualValueSets() {
        IDerivedValueSet derivedValueSet = new DerivedValueSet(cValueSet, "1");
        DerivedValueSet subSet = new DerivedValueSet(cValueSet, "1");

        assertTrue(derivedValueSet.containsValueSet(subSet));
    }

    @Test
    public void testContainsValueSet_enum() {
        attr.setDatatype(MY_EXTENSIBLE_ENUM);

        IDerivedValueSet derivedValueSet = new DerivedValueSet(attr, "1");
        DerivedValueSet subSet = new DerivedValueSet(cValueSet, "1");

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
