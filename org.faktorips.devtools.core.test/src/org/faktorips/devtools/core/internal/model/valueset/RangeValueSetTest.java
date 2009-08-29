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

package org.faktorips.devtools.core.internal.model.valueset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.PrimitiveIntegerDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RangeValueSetTest extends AbstractIpsPluginTest {

    private IPolicyCmptTypeAttribute attr;
    private IConfigElement ce;
    private IConfigElement intEl;

    private IIpsProject ipsProject;
    private IProductCmptGeneration generation;

    private IPolicyCmptType policyCmptType;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject("TestProject");
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "test.Base", "test.Product");
        IProductCmptType productCmptType = policyCmptType.findProductCmptType(ipsProject);
        attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("attr");
        attr.setDatatype(Datatype.MONEY.getQualifiedName());

        IProductCmpt cmpt = newProductCmpt(productCmptType, "test.Product");
        generation = (IProductCmptGeneration)cmpt.newGeneration(new GregorianCalendar(20006, 4, 26));

        ce = generation.newConfigElement();
        ce.setPolicyCmptTypeAttribute("attr");

        IPolicyCmptTypeAttribute attr2 = policyCmptType.newPolicyCmptTypeAttribute();
        attr2.setName("test");
        attr2.setDatatype(Datatype.INTEGER.getQualifiedName());
        attr2.setProductRelevant(true);

        intEl = generation.newConfigElement();
        intEl.setPolicyCmptTypeAttribute("test");
    }

    public void testCreateFromXml() throws CoreException {
        Document doc = getTestDocument();
        Element root = doc.getDocumentElement();

        // old format
        Element element = XmlUtil.getFirstElement(root);
        IRangeValueSet range = new RangeValueSet(ce, 1);
        range.initFromXml(element);
        assertEquals("42", range.getLowerBound());
        assertEquals("trulala", range.getUpperBound());
        assertEquals("4", range.getStep());
        assertTrue(range.getContainsNull());

        // new format
        element = XmlUtil.getElement(root, 1);
        range = new RangeValueSet(ce, 1);
        range.initFromXml(element);
        assertEquals("1", range.getLowerBound());
        assertEquals("10", range.getUpperBound());
        assertEquals("2", range.getStep());
        assertTrue(range.getContainsNull());

    }

    public void testToXml() {
        IRangeValueSet range = new RangeValueSet(ce, 1);
        range.setLowerBound("10");
        range.setUpperBound("100");
        range.setStep("10");
        Element element = range.toXml(newDocument());
        IRangeValueSet r2 = new RangeValueSet(ce, 1);
        r2.initFromXml(element);
        assertEquals(range.getLowerBound(), r2.getLowerBound());
        assertEquals(range.getUpperBound(), r2.getUpperBound());
        assertEquals(range.getStep(), r2.getStep());
        assertEquals(range.getContainsNull(), r2.getContainsNull());
    }

    public void testContainsValue() {
        RangeValueSet range = new RangeValueSet(intEl, 50);
        range.setLowerBound("20");
        range.setUpperBound("25");
        assertTrue(range.containsValue("20"));
        assertTrue(range.containsValue("22"));
        assertTrue(range.containsValue("25"));

        assertFalse(range.containsValue("19"));
        assertFalse(range.containsValue("26"));
        assertFalse(range.containsValue("19"));
        assertFalse(range.containsValue("20EUR"));

        range.setContainsNull(false);
        assertFalse(range.containsValue(null));

        range.setContainsNull(true);
        assertTrue(range.containsValue(null));

        range.setStep("2");
        range.setUpperBound("26");
        assertTrue(range.containsValue("20"));
        assertTrue(range.containsValue("24"));
        assertTrue(range.containsValue("26"));
        assertFalse(range.containsValue("18"));
        assertFalse(range.containsValue("21"));
        assertFalse(range.containsValue("28"));
    }

    public void testContainsValueSet_BothSetsAreRanges() {
        RangeValueSet range = new RangeValueSet(intEl, 50);
        range.setLowerBound("10");
        range.setUpperBound("20");
        range.setStep("2");

        IRangeValueSet abstractRange = new RangeValueSet(intEl, 200);
        abstractRange.setLowerBound("10");
        abstractRange.setUpperBound("14");
        abstractRange.setStep("2");
        abstractRange.setAbstract(true);
        assertTrue(abstractRange.containsValueSet(range));
        assertFalse(range.containsValueSet(abstractRange));

        RangeValueSet subRange = new RangeValueSet(intEl, 100);
        subRange.setLowerBound("10");
        subRange.setUpperBound("20");
        subRange.setStep("2");
        assertTrue(range.containsValueSet(subRange));

        subRange.setStep("3");
        assertFalse(range.containsValueSet(subRange));

        subRange.setStep("2");
        subRange.setLowerBound("8");
        assertFalse(range.containsValueSet(subRange));

        subRange.setLowerBound("14");
        assertTrue(range.containsValueSet(subRange));

        subRange.setUpperBound("18");
        assertTrue(range.containsValueSet(subRange));

        subRange.setUpperBound("24");
        assertFalse(range.containsValueSet(subRange));

        MessageList list = new MessageList();
        range.containsValueSet(subRange, list, null, null);
        assertTrue(list.containsErrorMsg());

        subRange.setUpperBound("18");
        list.clear();
        range.containsValueSet(subRange, list, null, null);
        assertFalse(list.containsErrorMsg());

        range.setContainsNull(false);
        subRange.setContainsNull(true);
        assertFalse(range.containsValueSet(subRange, list, null, null));
        assertNotNull(list.getMessageByCode(IValueSet.MSGCODE_NOT_SUBSET));

        range.setContainsNull(true);
        assertTrue(range.containsValueSet(subRange));

        range.setUpperBound("");
        range.setLowerBound("");
        range.setStep("");

        assertTrue(range.containsValueSet(subRange));

        subRange.setUpperBound("");
        subRange.setLowerBound("");
        subRange.setStep("");

        assertTrue(range.containsValueSet(subRange));
    }

    public void testContainsValueSetEmptyWithDecimal() throws Exception {
        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("attrX");
        attr.setDatatype(Datatype.DECIMAL.getQualifiedName());
        attr.setProductRelevant(true);
        attr.setValueSetType(ValueSetType.RANGE);

        IConfigElement el = generation.newConfigElement();
        el.setPolicyCmptTypeAttribute("attrX");

        RangeValueSet range = new RangeValueSet(el, 10);
        range.getValueDatatype().getQualifiedName().equals(Datatype.DECIMAL.getQualifiedName());
        RangeValueSet subset = new RangeValueSet(el, 20);
        subset.getValueDatatype().getQualifiedName().equals(Datatype.DECIMAL.getQualifiedName());

        assertTrue(range.containsValueSet(subset));
    }

    public void testValidate() throws Exception {
        RangeValueSet range = new RangeValueSet(intEl, 50);
        range.setLowerBound("20");
        range.setUpperBound("25");
        MessageList list = range.validate(ipsProject);
        assertTrue(list.isEmpty());

        range.setLowerBound("blabla");
        list = range.validate(ipsProject);
        assertFalse(list.isEmpty());

        range.setLowerBound("22");
        range.setUpperBound("blabla");
        list.clear();
        list = range.validate(ipsProject);
        assertFalse(list.isEmpty());

        range.setUpperBound("12");
        list.clear();
        list = range.validate(ipsProject);
        assertFalse(list.isEmpty());

        range.setLowerBound(null);
        range.setUpperBound(null);
        list.clear();
        list = range.validate(ipsProject);
        assertFalse(list.containsErrorMsg());

        Datatype[] vds = ipsProject.findDatatypes(true, false);
        ArrayList<Datatype> vdlist = new ArrayList<Datatype>();
        vdlist.addAll(Arrays.asList(vds));
        vdlist.add(new PrimitiveIntegerDatatype());
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setPredefinedDatatypesUsed(vdlist.toArray(new ValueDatatype[vdlist.size()]));
        ipsProject.setProperties(properties);

        IPolicyCmptTypeAttribute attr = intEl.findPcTypeAttribute(ipsProject);
        attr.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());

        list = range.validate(ipsProject);
        assertNull(list.getMessageByCode(IValueSet.MSGCODE_NULL_NOT_SUPPORTED));

        range.setContainsNull(true);
        list = range.validate(ipsProject);
        assertNotNull(list.getMessageByCode(IValueSet.MSGCODE_NULL_NOT_SUPPORTED));
    }

    public void testContainsValueSetStep() {
        RangeValueSet range = new RangeValueSet(intEl, 50);
        range.setLowerBound("10");
        range.setUpperBound("20");
        range.setStep("2");

        RangeValueSet subRange = new RangeValueSet(intEl, 100);
        subRange.setLowerBound("12");
        subRange.setUpperBound("20");
        subRange.setStep("2");

        assertTrue(range.containsValueSet(subRange));

        subRange.setStep("4");
        assertTrue(range.containsValueSet(subRange));

        subRange.setUpperBound("21");
        subRange.setStep("3");
        assertFalse(range.containsValueSet(subRange));
    }
}
