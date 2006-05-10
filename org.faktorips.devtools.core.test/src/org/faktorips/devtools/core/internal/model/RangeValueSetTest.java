/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.PrimitiveIntegerDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.DefaultTestContent;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IRangeValueSet;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RangeValueSetTest extends IpsPluginTest {

	private IConfigElement ce;
	private IConfigElement intEl;
	private DefaultTestContent content;
	
	public void setUp() throws Exception {
		super.setUp();
        content = new DefaultTestContent();
        IProductCmptGeneration gen = (IProductCmptGeneration)content.getComfortCollisionCoverageA().getGenerations()[0];
        ce = gen.newConfigElement();
        
        IAttribute attr = content.getCoverage().newAttribute();
        attr.setName("test");
        attr.setDatatype(Datatype.INTEGER.getQualifiedName());
        attr.setProductRelevant(true);
        
        content.getCoverage().getIpsSrcFile().save(true, null);
        intEl = gen.newConfigElement();
        intEl.setPcTypeAttribute("test");
	}
	
	public void testCreateFromXml() throws CoreException {
		Document doc = getTestDocument();
		Element root = doc.getDocumentElement();
		Element element = XmlUtil.getFirstElement(root);
		IRangeValueSet range = new RangeValueSet(ce, 1);
		range.initFromXml(element);
		assertEquals("42", range.getLowerBound());
		assertEquals("trulala", range.getUpperBound());
		assertEquals("4",range.getStep());
		assertTrue(range.getContainsNull());
	}

	public void testToXml() {
		IRangeValueSet range = new RangeValueSet(ce, 1);
		range.setLowerBound("10");
		range.setUpperBound("100");
		range.setStep("10");
		Element element = range.toXml(this.newDocument());
		IRangeValueSet r2 = new RangeValueSet(ce, 1);
		r2.initFromXml(element);
		assertEquals(range.getLowerBound(), r2.getLowerBound());
		assertEquals(range.getUpperBound(), r2.getUpperBound());
		assertEquals(range.getStep(), r2.getStep());
		assertEquals(range.getContainsNull(), r2.getContainsNull());
	}
	
	public void testContainsValue() {
	    RangeValueSet range = new RangeValueSet (intEl, 50);
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
 	}
	
	public void testContainsValueSet() {
		RangeValueSet range = new RangeValueSet(intEl, 50);
		range.setLowerBound("10");
		range.setUpperBound("20");
		range.setStep("2");
		
		RangeValueSet subRange = new RangeValueSet(intEl, 100);
		subRange.setLowerBound("10");
		subRange.setUpperBound("20");
		subRange.setStep("2");
		assertTrue(range.containsValueSet(subRange));
		
		subRange.setStep("3");
		assertFalse(range.containsValueSet(subRange));
		
		subRange.setStep("2");
		subRange.setLowerBound("9");
		assertFalse(range.containsValueSet(subRange));

		subRange.setLowerBound("15");
		assertTrue(range.containsValueSet(subRange));

		subRange.setUpperBound("17");
		assertTrue(range.containsValueSet(subRange));
		
		subRange.setUpperBound("23");
		assertFalse(range.containsValueSet(subRange));

	    MessageList list = new MessageList();
	    range.containsValueSet(subRange, list, null, null);
	    assertTrue(list.containsErrorMsg());
	    
	    subRange.setUpperBound("17");
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
        IAttribute attr = content.getCoverage().newAttribute();
        attr.setName("attr");
        attr.setDatatype(Datatype.DECIMAL.getQualifiedName());
        attr.setProductRelevant(true);
        attr.setValueSetType(ValueSetType.RANGE);
        
        content.getCoverage().getIpsSrcFile().save(true, null);

        IProductCmptGeneration gen = (IProductCmptGeneration)content.getComfortCollisionCoverageA().getGenerations()[0];
        IConfigElement el = gen.newConfigElement();
        el.setPcTypeAttribute("attr");
        
        RangeValueSet range = new RangeValueSet(el, 10);
        range.getValueDatatype().getQualifiedName().equals(Datatype.DECIMAL.getQualifiedName());
        RangeValueSet subset = new RangeValueSet(el, 20);
        subset.getValueDatatype().getQualifiedName().equals(Datatype.DECIMAL.getQualifiedName());
        
        assertTrue(range.containsValueSet(subset));
    }
    
	public void testValidate () throws Exception {
	    RangeValueSet range = new RangeValueSet (intEl, 50);
	    range.setLowerBound("20");
	    range.setUpperBound("25");
	    MessageList list = new MessageList();
	    range.validate(list);
	    assertTrue(list.isEmpty());
	    
	    range.setLowerBound("blabla");
	    range.validate(list);
	    assertFalse(list.isEmpty());
	    
	    range.setLowerBound("22");
	    range.setUpperBound("blabla");
	    list.clear();
	    range.validate(list);
	    assertFalse(list.isEmpty());
	    
	    range.setUpperBound("12");
	    list.clear();
	    range.validate(list);
	    assertFalse(list.isEmpty());

	    range.setLowerBound("");
	    range.setUpperBound("");
        list.clear();
        range.validate(list);
        assertFalse(list.containsErrorMsg());
        
		ValueDatatype[] vds = content.getProject().getValueDatatypes(false);
		ArrayList vdlist = new ArrayList();
		vdlist.addAll(Arrays.asList(vds));
		vdlist.add(new PrimitiveIntegerDatatype());
		content.getProject().setValueDatatypes((ValueDatatype[])vdlist.toArray(new ValueDatatype[vdlist.size()]));
		IAttribute attr = content.getCoverage().getAttribute("test");
		attr.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
		content.getCoverage().getIpsSrcFile().save(true, null);

		range.validate(list);
		assertNull(list.getMessageByCode(IRangeValueSet.MSGCODE_NULL_NOT_SUPPORTED));
		
		range.setContainsNull(true);
		range.validate(list);
		assertNotNull(list.getMessageByCode(IRangeValueSet.MSGCODE_NULL_NOT_SUPPORTED));
    }
    
}