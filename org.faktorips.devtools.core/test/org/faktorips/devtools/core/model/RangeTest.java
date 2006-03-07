package org.faktorips.devtools.core.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.DefaultTestContent;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.internal.model.RangeValueSet;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RangeTest extends IpsPluginTest {

	private IConfigElement ce;
	
	public void setUp() throws Exception {
		super.setUp();
        DefaultTestContent content = new DefaultTestContent();
        IProductCmptGeneration gen = (IProductCmptGeneration)content.getComfortCollisionCoverageA().getGenerations()[0];
        ce = gen.newConfigElement();
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
	    RangeValueSet range = new RangeValueSet (ce, 1);
		range.setLowerBound("20");
		range.setUpperBound("25");
	    assertTrue(range.containsValue("20", Datatype.DECIMAL));
	    assertTrue(range.containsValue("22", Datatype.DECIMAL));
	    assertTrue(range.containsValue("25", Datatype.DECIMAL));

	    assertFalse(range.containsValue("19", Datatype.DECIMAL));
	    assertFalse(range.containsValue("26", Datatype.DECIMAL));
	    assertFalse(range.containsValue("19", Datatype.DECIMAL));
	    assertFalse(range.containsValue("20EUR", Datatype.DECIMAL));
	    assertFalse(range.containsValue("20", Datatype.MONEY));
	    
	    try {
	    	range.containsValue("22", null);
	    	fail();
	    } 
	    catch (NullPointerException e) {
	    	// nothing to do
	    }
 	}
	
	public void testContainsValueSet() {
		RangeValueSet range = new RangeValueSet(ce, 1);
		range.setLowerBound("10");
		range.setUpperBound("20");
		range.setStep("2");
		
		RangeValueSet subRange = new RangeValueSet(ce, 1);
		subRange.setLowerBound("10");
		subRange.setUpperBound("20");
		subRange.setStep("2");
		assertTrue(range.containsValueSet(subRange, Datatype.INTEGER));
		
		subRange.setStep("3");
		assertFalse(range.containsValueSet(subRange, Datatype.INTEGER));
		
		subRange.setStep("2");
		subRange.setLowerBound("9");
		assertFalse(range.containsValueSet(subRange, Datatype.INTEGER));

		subRange.setLowerBound("15");
		assertTrue(range.containsValueSet(subRange, Datatype.INTEGER));

		subRange.setUpperBound("17");
		assertTrue(range.containsValueSet(subRange, Datatype.INTEGER));
		
		subRange.setUpperBound("23");
		assertFalse(range.containsValueSet(subRange, Datatype.INTEGER));

		try {
	    	range.containsValueSet(subRange, null);
	    	fail();
	    } 
	    catch (NullPointerException e) {
	    	// nothing to do
	    }
	    
	    MessageList list = new MessageList();
	    range.containsValueSet(subRange, Datatype.INTEGER, list, null, null);
	    assertTrue(list.containsErrorMsg());
	    
	    subRange.setUpperBound("17");
	    list.clear();
	    range.containsValueSet(subRange, Datatype.INTEGER, list, null, null);
	    assertFalse(list.containsErrorMsg());   
	}
	
	public void testValidate () {
	    RangeValueSet range = new RangeValueSet (ce, 1);
	    range.setLowerBound("20");
	    range.setUpperBound("25");
	    MessageList list = new MessageList();
	    range.validate(Datatype.DECIMAL, list);
	    assertTrue(list.isEmpty());
	    
	    range.setLowerBound("blabla");
	    range.validate(Datatype.DECIMAL, list);
	    assertFalse(list.isEmpty());
	    
	    range.setLowerBound("22");
	    range.setUpperBound("blabla");
	    list.clear();
	    range.validate(Datatype.DECIMAL, list);
	    assertFalse(list.isEmpty());
	    
	    range.setUpperBound("12");
	    list.clear();
	    range.validate(Datatype.DECIMAL, list);
	    assertFalse(list.isEmpty());

	    list.clear();
	    range.validate(null, list);
	    assertFalse(list.isEmpty());

	    range.setLowerBound("");
	    range.setUpperBound("");
        list.clear();
        range.validate(Datatype.MONEY, list);
        assertFalse(list.containsErrorMsg());
    }
	
}