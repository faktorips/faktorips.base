package org.faktorips.devtools.core.internal.model;

import org.faktorips.util.XmlAbstractTestCase;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 * 
 * @author Thorsten Guenther
 */
public class ValueToXmlHelperTest extends XmlAbstractTestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testAddValueToElement() {
    	Document doc = getTestDocument();
    	
    	Element node = (Element)doc.getDocumentElement().getElementsByTagName("DifferentElement").item(0);
    	
    	assertEquals(0, node.getChildNodes().getLength());
    	
    	ValueToXmlHelper.addValueToElement("TestValue", node, "ValueNode");
    	
    	node = (Element)node.getFirstChild();
    	
    	assertEquals(Node.CDATA_SECTION_NODE, node.getFirstChild().getNodeType());
    	assertEquals("TestValue", ((CDATASection)node.getFirstChild()).getData());
    	
    	node = (Element)doc.getDocumentElement().getElementsByTagName("EmptyTestElement").item(0);
    	ValueToXmlHelper.addValueToElement(null, node, "ValueNode");
    	node = (Element)node.getElementsByTagName("ValueNode").item(0);
    	assertEquals("true", node.getAttribute("isNull"));
    	
    }
    
    public void testGetValueFromElement() {
    	Document doc = getTestDocument();
    	
    	Element node = (Element)doc.getDocumentElement().getElementsByTagName("TestElement").item(0);
    	
    	assertEquals("cdataValue", ValueToXmlHelper.getValueFromElement(node, "ValueNode"));
    	
    	node = (Element)doc.getDocumentElement().getElementsByTagName("EmptyValueTestElement").item(0);
    	
    	assertEquals("", ValueToXmlHelper.getValueFromElement(node, "ValueNode"));
    	
    	node = (Element)doc.getDocumentElement().getElementsByTagName("NullTestElement").item(0);
    	assertNull(ValueToXmlHelper.getValueFromElement(node, "ValueNode"));
    }
}