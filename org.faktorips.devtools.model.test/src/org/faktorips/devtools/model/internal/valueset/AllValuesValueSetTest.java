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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.PrimitiveIntegerDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.devtools.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AllValuesValueSetTest extends AbstractIpsPluginTest {

    private IPolicyCmptTypeAttribute attr;
    private IConfiguredValueSet cValueSet1;

    private IConfiguredValueSet cValueSet2;

    private IIpsProject ipsProject;
    private IProductCmptGeneration generation;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject("TestProject");
        IPolicyCmptType policy = newPolicyCmptType(ipsProject, "test.Base");
        attr = policy.newPolicyCmptTypeAttribute();
        attr.setName("attr");
        attr.setDatatype(Datatype.MONEY.getQualifiedName());

        IPolicyCmptTypeAttribute attr2 = policy.newPolicyCmptTypeAttribute();
        attr2.setName("attr2");
        attr2.setDatatype(Datatype.STRING.getQualifiedName());

        IProductCmptType productType = newProductCmptType(ipsProject, "test.Product", policy);

        IProductCmpt cmpt = newProductCmpt(ipsProject, "test.Product");
        cmpt.setProductCmptType(productType.getQualifiedName());
        generation = (IProductCmptGeneration)cmpt.newGeneration(new GregorianCalendar(20006, 4, 26));

        cValueSet1 = generation.newPropertyValue(attr, IConfiguredValueSet.class);

        cValueSet2 = generation.newPropertyValue(attr2, IConfiguredValueSet.class);
    }

    @Test
    public void testCreateFromXml() {
        Document doc = getTestDocument();
        Element root = doc.getDocumentElement();
        Element element = XmlUtil.getFirstElement(root);

        IValueSet allValues = new UnrestrictedValueSet(cValueSet1, "1");
        allValues.initFromXml(element);
        assertNotNull(allValues);
    }

    @Test
    public void testToXml() {
        UnrestrictedValueSet allValues = new UnrestrictedValueSet(cValueSet1, "1");
        Element element = allValues.toXml(newDocument());
        IUnrestrictedValueSet allValues2 = new UnrestrictedValueSet(cValueSet1, "2");
        allValues2.initFromXml(element);
        assertNotNull(allValues2);
    }

    @Test
    public void testContainsValue() throws Exception {
        UnrestrictedValueSet allValues = new UnrestrictedValueSet(cValueSet1, "1");
        assertFalse(allValues.containsValue("abc", ipsProject));
        assertTrue(allValues.containsValue("1EUR", ipsProject));

        cValueSet1.findPcTypeAttribute(ipsProject).setDatatype(Datatype.INTEGER.getQualifiedName());
        assertFalse(allValues.containsValue("1EUR", ipsProject));
        assertTrue(allValues.containsValue("99", ipsProject));
    }

    @Test
    public void testContainsValueSet() throws Exception {
        IUnrestrictedValueSet allValues = (IUnrestrictedValueSet)cValueSet1.getValueSet();

        assertTrue(allValues.containsValueSet(allValues));
        assertTrue(allValues.containsValueSet(new UnrestrictedValueSet(cValueSet1, "99")));
        assertFalse(allValues.containsValueSet(cValueSet2.getValueSet()));
    }

    @Test
    public void testIsContainsNull() throws Exception {
        IUnrestrictedValueSet allValues = (IUnrestrictedValueSet)cValueSet1.getValueSet();

        // test with non-primitive datatype
        assertTrue(allValues.isContainsNull());

        // test with no datatype
        attr.setDatatype("");
        assertTrue(allValues.isContainsNull());

        // test with primitive datatype
        Datatype[] vds = ipsProject.findDatatypes(true, false);
        ArrayList<Datatype> list = new ArrayList<>();
        list.addAll(Arrays.asList(vds));
        list.add(new PrimitiveIntegerDatatype());
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setPredefinedDatatypesUsed(list.toArray(new ValueDatatype[list.size()]));
        ipsProject.setProperties(properties);

        attr.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
        assertFalse(allValues.isContainsNull());
    }
}
