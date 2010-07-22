/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.valueset;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AllValuesValueSetTest extends AbstractIpsPluginTest {

    private IPolicyCmptTypeAttribute attr;
    private IConfigElement ce;

    private IConfigElement ce2;

    private IIpsProject ipsProject;
    private IProductCmptGeneration generation;

    @Override
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

        IProductCmptType productType = newProductCmptType(ipsProject, "test.Product");
        productType.setPolicyCmptType(policy.getQualifiedName());

        IProductCmpt cmpt = newProductCmpt(ipsProject, "test.Product");
        cmpt.setProductCmptType(productType.getQualifiedName());
        generation = (IProductCmptGeneration)cmpt.newGeneration(new GregorianCalendar(20006, 4, 26));

        ce = generation.newConfigElement();
        ce.setPolicyCmptTypeAttribute("attr");

        ce2 = generation.newConfigElement();
        ce2.setPolicyCmptTypeAttribute("attr2");
    }

    public void testCreateFromXml() {
        Document doc = getTestDocument();
        Element root = doc.getDocumentElement();
        Element element = XmlUtil.getFirstElement(root);

        IValueSet allValues = new UnrestrictedValueSet(ce, "1");
        allValues.initFromXml(element);
        assertNotNull(allValues);
    }

    public void testToXml() {
        UnrestrictedValueSet allValues = new UnrestrictedValueSet(ce, "1");
        Element element = allValues.toXml(newDocument());
        IUnrestrictedValueSet allValues2 = new UnrestrictedValueSet(ce, "2");
        allValues2.initFromXml(element);
        assertNotNull(allValues2);
    }

    public void testContainsValue() throws Exception {
        UnrestrictedValueSet allValues = new UnrestrictedValueSet(ce, "1");
        assertFalse(allValues.containsValue("abc"));
        assertTrue(allValues.containsValue("1EUR"));

        ce.findPcTypeAttribute(ipsProject).setDatatype(Datatype.INTEGER.getQualifiedName());
        assertFalse(allValues.containsValue("1EUR"));
        assertTrue(allValues.containsValue("99"));
    }

    public void testContainsValueSet() throws Exception {
        UnrestrictedValueSet allValues = (UnrestrictedValueSet)ce.getValueSet();

        assertTrue(allValues.containsValueSet(allValues));
        assertTrue(allValues.containsValueSet(new UnrestrictedValueSet(ce, "99")));
        assertFalse(allValues.containsValueSet(ce2.getValueSet()));
    }

    public void testGetContainsNull() throws Exception {
        UnrestrictedValueSet allValues = (UnrestrictedValueSet)ce.getValueSet();

        // test with non-primitive datatype
        assertTrue(allValues.getContainsNull());

        // test with no datatype
        attr.setDatatype("");
        assertTrue(allValues.getContainsNull());

        // test with primitive datatype
        Datatype[] vds = ipsProject.findDatatypes(true, false);
        ArrayList<Datatype> list = new ArrayList<Datatype>();
        list.addAll(Arrays.asList(vds));
        list.add(new PrimitiveIntegerDatatype());
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setPredefinedDatatypesUsed(list.toArray(new ValueDatatype[list.size()]));
        ipsProject.setProperties(properties);

        attr.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
        assertFalse(allValues.getContainsNull());
    }

    public void testSetContainsNull() throws Exception {
        UnrestrictedValueSet allValues = (UnrestrictedValueSet)ce.getValueSet();

        allValues.setContainsNull(true);

        try {
            allValues.setContainsNull(false);
            fail();
        } catch (UnsupportedOperationException e) {
            // nothing to do
        }

        Datatype[] vds = ipsProject.findDatatypes(true, false);
        ArrayList<Datatype> list = new ArrayList<Datatype>();
        list.addAll(Arrays.asList(vds));
        list.add(new PrimitiveIntegerDatatype());
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setPredefinedDatatypesUsed(list.toArray(new ValueDatatype[list.size()]));
        ipsProject.setProperties(properties);

        attr.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());

        allValues.setContainsNull(false);

        try {
            allValues.setContainsNull(true);
            fail();
        } catch (UnsupportedOperationException e) {
            // nothing to do
        }

    }

}
