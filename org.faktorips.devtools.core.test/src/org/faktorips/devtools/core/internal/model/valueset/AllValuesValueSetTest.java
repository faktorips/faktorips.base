/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.valueset;

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
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.util.XmlUtil;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AllValuesValueSetTest extends AbstractIpsPluginTest {

    private IPolicyCmptTypeAttribute attr;
    private IConfigElement ce;

    private IConfigElement ce2;

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

        ce = generation.newConfigElement();
        ce.setPolicyCmptTypeAttribute("attr");

        ce2 = generation.newConfigElement();
        ce2.setPolicyCmptTypeAttribute("attr2");
    }

    @Test
    public void testCreateFromXml() {
        Document doc = getTestDocument();
        Element root = doc.getDocumentElement();
        Element element = XmlUtil.getFirstElement(root);

        IValueSet allValues = new UnrestrictedValueSet(ce, "1");
        allValues.initFromXml(element);
        assertNotNull(allValues);
    }

    @Test
    public void testToXml() {
        UnrestrictedValueSet allValues = new UnrestrictedValueSet(ce, "1");
        Element element = allValues.toXml(newDocument());
        IUnrestrictedValueSet allValues2 = new UnrestrictedValueSet(ce, "2");
        allValues2.initFromXml(element);
        assertNotNull(allValues2);
    }

    @Test
    public void testContainsValue() throws Exception {
        UnrestrictedValueSet allValues = new UnrestrictedValueSet(ce, "1");
        assertFalse(allValues.containsValue("abc", ipsProject));
        assertTrue(allValues.containsValue("1EUR", ipsProject));

        ce.findPcTypeAttribute(ipsProject).setDatatype(Datatype.INTEGER.getQualifiedName());
        assertFalse(allValues.containsValue("1EUR", ipsProject));
        assertTrue(allValues.containsValue("99", ipsProject));
    }

    @Test
    public void testContainsValueSet() throws Exception {
        IUnrestrictedValueSet allValues = (IUnrestrictedValueSet)ce.getValueSet();

        assertTrue(allValues.containsValueSet(allValues));
        assertTrue(allValues.containsValueSet(new UnrestrictedValueSet(ce, "99")));
        assertFalse(allValues.containsValueSet(ce2.getValueSet()));
    }

    @Test
    public void testIsContainsNull() throws Exception {
        IUnrestrictedValueSet allValues = (IUnrestrictedValueSet)ce.getValueSet();

        // test with non-primitive datatype
        assertTrue(allValues.isContainsNull());

        // test with no datatype
        attr.setDatatype("");
        assertTrue(allValues.isContainsNull());

        // test with primitive datatype
        Datatype[] vds = ipsProject.findDatatypes(true, false);
        ArrayList<Datatype> list = new ArrayList<Datatype>();
        list.addAll(Arrays.asList(vds));
        list.add(new PrimitiveIntegerDatatype());
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setPredefinedDatatypesUsed(list.toArray(new ValueDatatype[list.size()]));
        ipsProject.setProperties(properties);

        attr.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
        assertFalse(allValues.isContainsNull());
    }
}
