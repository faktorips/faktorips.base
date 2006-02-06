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
	
	public void testContains () {
	    Range range = new Range ("20", "25");
	    assertTrue(range.contains("20", Datatype.DECIMAL));
	    assertTrue(range.contains("22", Datatype.DECIMAL));
	    assertTrue(range.contains("25", Datatype.DECIMAL));

	    assertFalse(range.contains("19", Datatype.DECIMAL));
	    assertFalse(range.contains("26", Datatype.DECIMAL));
	    assertFalse(range.contains("19", Datatype.DECIMAL));
	    assertFalse(range.contains("20EUR", Datatype.DECIMAL));
	    assertFalse(range.contains("20", Datatype.MONEY));
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