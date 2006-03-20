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

package org.faktorips.devtools.core.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.DefaultTestContent;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.internal.model.RangeValueSet;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RangeTest extends IpsPluginTest {

	private IConfigElement ce;
	private IConfigElement intEl;
	
	public void setUp() throws Exception {
		super.setUp();
        DefaultTestContent content = new DefaultTestContent();
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
	}
	
	public void testValidate () {
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
    }
	
}