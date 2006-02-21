package org.faktorips.devtools.core.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.util.XmlAbstractTestCase;
import org.faktorips.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RangeTest extends XmlAbstractTestCase {

	public void testCreateFromXml() throws CoreException {
		Document doc = getTestDocument();
		Element root = doc.getDocumentElement();
		Element element = XmlUtil.getFirstElement(root);
		Range range = (Range)Range.createFromXml(element);
		assertEquals("42", range.getLowerBound());
		assertEquals("trulala", range.getUpperBound());
		assertEquals("4",range.getStep());
	}

	public void testToXml() {
		Range range = new Range("10", "100", "10");
		Element element = range.toXml(this.newDocument());
		Range r2 = (Range)Range.createFromXml(element);
		assertEquals(range.getLowerBound(), r2.getLowerBound());
		assertEquals(range.getUpperBound(), r2.getUpperBound());
		assertEquals(range.getStep(), r2.getStep());
	}
	
	public void testContainsValue() {
	    Range range = new Range ("20", "25");
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
		Range range = new Range("10", "20", "2");
		
		Range subRange = new Range("10", "20", "2");
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
	    Range range = new Range ("20", "25");
	    MessageList list = new MessageList();
	    range.validate(Datatype.DECIMAL, list);
	    assertTrue(list.isEmpty());
	    
	    range = new Range ( "blabla", "25");
	    range.validate(Datatype.DECIMAL, list);
	    assertFalse(list.isEmpty());
	    
	    range = new Range ("22", "blabla");
	    list.clear();
	    range.validate(Datatype.DECIMAL, list);
	    assertFalse(list.isEmpty());
	    
	    range = new Range("22", "12");
	    list.clear();
	    range.validate(Datatype.DECIMAL, list);
	    assertFalse(list.isEmpty());

	    list.clear();
	    range.validate(null, list);
	    assertFalse(list.isEmpty());

        range = new Range("", "");
        list.clear();
        range.validate(Datatype.MONEY, list);
        assertFalse(list.containsErrorMsg());
    }
	
}