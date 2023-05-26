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

import java.util.GregorianCalendar;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.enums.EnumContent;
import org.faktorips.devtools.model.internal.enums.EnumType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.devtools.model.valueset.IDerivedValueSet;
import org.faktorips.devtools.model.valueset.IUnrestrictedValueSet;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class UnrestrictedValueSetTest extends AbstractIpsPluginTest {

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
    public void testUnrestrictedValueSet() {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(cValueSet, "1");
        assertTrue(unrestricted.isContainsNull());

        unrestricted = new UnrestrictedValueSet(cValueSet, "1", true);
        assertTrue(unrestricted.isContainsNull());

        unrestricted = new UnrestrictedValueSet(cValueSet, "1", false);
        assertFalse(unrestricted.isContainsNull());
    }

    @Test
    public void testPropertiesToXml() throws Exception {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(cValueSet, "1");
        Element xml = unrestricted.toXml(newDocument());

        IUnrestrictedValueSet unrestricted2 = new UnrestrictedValueSet(cValueSet, "1");
        unrestricted2.initFromXml(xml);
        assertTrue(unrestricted2.isContainsNull());
    }

    @Test
    public void testInitPropertiesFromXml() throws Exception {
        Document doc = getTestDocument();
        Element root = doc.getDocumentElement();

        // first
        Element element = XmlUtil.getFirstElement(root);
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(cValueSet, "1");
        unrestricted.initFromXml(element);
        assertFalse(unrestricted.isContainsNull());

        // second
        element = XmlUtil.getElement(root, 1);
        unrestricted = new UnrestrictedValueSet(cValueSet, "2");
        unrestricted.initFromXml(element);
        assertTrue(unrestricted.isContainsNull());

        // third
        element = XmlUtil.getElement(root, 2);
        unrestricted = new UnrestrictedValueSet(cValueSet, "3");
        unrestricted.initFromXml(element);
        assertTrue(unrestricted.isContainsNull());
    }

    @Test
    public void testCopy() throws Exception {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(cValueSet, "1");
        unrestricted.setContainsNull(false);

        IUnrestrictedValueSet unrestricted2 = (IUnrestrictedValueSet)unrestricted.copy(cValueSet, "2");
        assertFalse(unrestricted2.isContainsNull());
    }

    @Test
    public void testCopyPropertiesFrom() throws Exception {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(cValueSet, "1");
        unrestricted.setContainsNull(false);

        IUnrestrictedValueSet unrestricted2 = new UnrestrictedValueSet(cValueSet, "2");
        assertTrue(unrestricted2.isContainsNull());

        unrestricted2.copyFrom(unrestricted);
        assertFalse(unrestricted2.isContainsNull());
    }

    @Test
    public void testIsContainsNull() throws Exception {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(cValueSet, "1");
        assertTrue(unrestricted.isContainsNull());
    }

    @Test
    public void testSetContainsNull() throws Exception {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(cValueSet, "1");
        unrestricted.setContainsNull(false);
        assertFalse(unrestricted.isContainsNull());
    }

    @Test
    public void testIsContainsNullPrimitive() throws Exception {
        IUnrestrictedValueSet unrestricted = new UnrestrictedValueSet(cValueSet, "1");

        attr.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
        assertFalse(unrestricted.isContainsNull());
    }

    @Test
    public void testContainsValue() throws Exception {
        attr.setDatatype(Datatype.MONEY.getQualifiedName());
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(cValueSet, "1", false);

        assertTrue(unrestrictedValueSet.containsValue("10EUR", ipsProject));
    }

    @Test
    public void testContainsValue_DatatypeIsNull() throws Exception {
        attr.setDatatype(null);
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(cValueSet, "1", false);

        assertFalse(unrestrictedValueSet.containsValue("someValue", ipsProject));
    }

    @Test
    public void testContainsValue_NotParsableValue() throws Exception {
        attr.setDatatype(Datatype.MONEY.getQualifiedName());
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(cValueSet, "1", false);

        assertFalse(unrestrictedValueSet.containsValue("notParsable", ipsProject));
    }

    @Test
    public void testContainsValue_ValueIsNull_UnrestrictedValueSetWithoutNull() throws Exception {
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(cValueSet, "1", false);

        assertFalse(unrestrictedValueSet.containsValue(null, ipsProject));
    }

    @Test
    public void testContainsValue_ValueIsNull_UnrestrictedValueSetWithNull() throws Exception {
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(cValueSet, "1", true);

        assertTrue(unrestrictedValueSet.containsValue(null, ipsProject));
    }

    @Test
    public void testContainsValueSet_EqualValueSetsWithoutNull() {
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(cValueSet, "1", false);
        UnrestrictedValueSet subSet = new UnrestrictedValueSet(cValueSet, "1", false);

        assertTrue(unrestrictedValueSet.containsValueSet(subSet));
    }

    @Test
    public void testContainsValueSet_EqualValueSetsWithNull() {
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(cValueSet, "1", true);
        UnrestrictedValueSet subSet = new UnrestrictedValueSet(cValueSet, "1", true);

        assertTrue(unrestrictedValueSet.containsValueSet(subSet));
    }

    @Test
    public void testContainsValueSet_ContainsSubValueSet() {
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(cValueSet, "1", true);
        UnrestrictedValueSet subSet = new UnrestrictedValueSet(cValueSet, "1", false);

        assertTrue(unrestrictedValueSet.containsValueSet(subSet));
    }

    @Test
    public void testContainsValueSet_ContainsSubValueSetNot() {
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(cValueSet, "1", false);
        UnrestrictedValueSet subSet = new UnrestrictedValueSet(cValueSet, "1", true);

        assertFalse(unrestrictedValueSet.containsValueSet(subSet));
    }

    @Test
    public void testContainsValueSet_Enum() {
        attr.setDatatype(MY_EXTENSIBLE_ENUM);

        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(attr, "1", true);
        UnrestrictedValueSet subSet = new UnrestrictedValueSet(cValueSet, "1", true);

        assertTrue(unrestrictedValueSet.containsValueSet(subSet));
    }

    @Test
    public void testContainsValueSet_Derived_WithNull() {
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(attr, "1", true);
        IDerivedValueSet subSet = new DerivedValueSet(cValueSet, "1");

        assertTrue(unrestrictedValueSet.containsValueSet(subSet));
    }

    @Test
    public void testContainsValueSet_Derived_WithoutNull() {
        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(attr, "1", false);
        IDerivedValueSet subSet = new DerivedValueSet(cValueSet, "1");

        assertTrue(unrestrictedValueSet.containsValueSet(subSet));
    }

    @Test
    public void testContainsValueSet_DifferentDatatypes() throws Exception {
        IPolicyCmptTypeAttribute attr2 = policyCmptType.newPolicyCmptTypeAttribute();
        attr2.setName("attr");
        attr2.setDatatype(MY_EXTENSIBLE_ENUM);

        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(attr, "1", true);
        UnrestrictedValueSet subSet = new UnrestrictedValueSet(attr2, "1", true);

        assertFalse(subSet.containsValueSet(unrestrictedValueSet));
    }

    @Test
    public void testContainsValueSet_Covariant() throws Exception {
        attr.setDatatype(MY_SUPER_ENUM);
        IPolicyCmptTypeAttribute attr2 = policyCmptType.newPolicyCmptTypeAttribute();
        attr2.setName("attr");
        attr2.setDatatype(MY_EXTENSIBLE_ENUM);

        IUnrestrictedValueSet unrestrictedValueSet = new UnrestrictedValueSet(attr, "1", true);
        UnrestrictedValueSet subSet = new UnrestrictedValueSet(attr2, "1", true);

        assertTrue(unrestrictedValueSet.containsValueSet(subSet));
        assertFalse(subSet.containsValueSet(unrestrictedValueSet));
    }

    @Test
    public void testCompareTo_BothContainsNull() throws Exception {
        IUnrestrictedValueSet u1 = createUnrestricted(true);
        IUnrestrictedValueSet u2 = createUnrestricted(true);

        assertThat(u1.compareTo(u2), is(0));
        assertThat(u2.compareTo(u1), is(0));
    }

    @Test
    public void testCompareTo_BothNotContainsNull() throws Exception {
        IUnrestrictedValueSet u1 = createUnrestricted(false);
        IUnrestrictedValueSet u2 = createUnrestricted(false);

        assertThat(u1.compareTo(u2), is(0));
        assertThat(u2.compareTo(u1), is(0));
    }

    @Test
    public void testCompareTo_OneContainsNull() throws Exception {
        IUnrestrictedValueSet u1 = createUnrestricted(true);
        IUnrestrictedValueSet u2 = createUnrestricted(false);

        assertThat(u1.compareTo(u2), is(-1));
        assertThat(u2.compareTo(u1), is(1));
    }

    @Test
    public void testStringDatatypeEmpty() {
        IUnrestrictedValueSet u1 = createUnrestricted(true);
        IUnrestrictedValueSet u2 = createUnrestricted(false);

        assertThat(u1.containsValue("", ipsProject), is(true));
        assertThat(u2.containsValue("", ipsProject), is(false));
    }

    private IUnrestrictedValueSet createUnrestricted(boolean containsNull) {
        return new UnrestrictedValueSet(attr, "1", containsNull);
    }

}
