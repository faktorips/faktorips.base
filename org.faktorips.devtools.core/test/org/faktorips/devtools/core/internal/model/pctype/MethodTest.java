package org.faktorips.devtools.core.internal.model.pctype;

import org.faktorips.devtools.core.internal.model.IpsObjectTestCase;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.util.XmlUtil;
import org.w3c.dom.Element;


/**
 *
 */
public class MethodTest extends IpsObjectTestCase {
    
    private PolicyCmptType pcType;
    private IMethod method;
    
    protected void setUp() throws Exception {
        super.setUp(IpsObjectType.POLICY_CMPT_TYPE);
    }
    
    protected void createObjectAndPart() {
        pcType = new PolicyCmptType(pdSrcFile);
        method = pcType.newMethod();
    }
    
    public void testRemove() {
        method.delete();
        assertEquals(0, pcType.getAttributes().length);
        assertTrue(pdSrcFile.isDirty());
    }
    
    public void testSetDatatype() {
        method.setDatatype("Money");
        assertEquals("Money", method.getDatatype());
        assertTrue(pdSrcFile.isDirty());
    }
    
    public void testSetParameters() {
        method.setParameters(new Parameter[]{new Parameter(0, "p", "int")});
        assertEquals(1, method.getParameters().length);
        assertEquals("p", method.getParameters()[0].getName());
        assertEquals("int", method.getParameters()[0].getDatatype());
        assertTrue(pdSrcFile.isDirty());
    }
    
    public void testInitFromXml() {
        Element docElement = this.getTestDocument().getDocumentElement();
        
        method.initFromXml(XmlUtil.getElement(docElement, Method.TAG_NAME, 0));
        assertEquals(42, method.getId());
        assertEquals("calcPremium", method.getName());
        assertEquals("Money", method.getDatatype());
        assertEquals(Modifier.PRIVATE, method.getModifier());
        assertTrue(method.isAbstract());
        assertEquals("", method.getBody());
        Parameter[] params = method.getParameters();
        assertEquals(2, params.length);
        assertEquals(0, params[0].getIndex());
        assertEquals("p1", params[0].getName());
        assertEquals("Money", params[0].getDatatype());
        assertEquals(1, params[1].getIndex());
        assertEquals("p2", params[1].getName());
        assertEquals("Decimal", params[1].getDatatype());
        
        method.initFromXml(XmlUtil.getElement(docElement, Method.TAG_NAME, 1));
        assertEquals("return premium;", method.getBody());
        assertEquals(0, method.getNumOfParameters());
    }

    /*
     * Class under test for Element toXml(Document)
     */
    public void testToXmlDocument() {
        method = pcType.newMethod(); // => id=1, because it's the second method
        method.setName("getAge");
        method.setModifier(Modifier.PRIVATE);
        method.setDatatype("Decimal");
        method.setAbstract(true);
        method.setBody("return i;");
        Parameter[] params = new Parameter[2];
        params[0] = new Parameter(0, "p1", "Money");
        params[1] = new Parameter(1, "p2", "Decimal");
        method.setParameters(params);
        Element element = method.toXml(this.newDocument());
        
        Method copy = new Method();
        copy.initFromXml(element);
        Parameter[] copyParams = copy.getParameters();
        assertEquals(1, copy.getId());
        assertEquals("getAge", copy.getName());
        assertEquals("Decimal", copy.getDatatype());
        assertEquals(Modifier.PRIVATE, copy.getModifier());
        assertTrue(copy.isAbstract());
        assertEquals("return i;", copy.getBody());
        assertEquals(2, copyParams.length);
        assertEquals(0, copyParams[0].getIndex());
        assertEquals("p1", copyParams[0].getName());
        assertEquals("Money", copyParams[0].getDatatype());
        assertEquals(1, copyParams[1].getIndex());
        assertEquals("p2", copyParams[1].getName());
        assertEquals("Decimal", copyParams[1].getDatatype());

    }
    
    public void testIsSame() {
        method.setName("calc");
        method.setDatatype("void");
        Parameter[] params = new Parameter[2];
        params[0] = new Parameter(0, "p1", "Decimal");
        params[1] = new Parameter(1, "p2", "Money");
        method.setParameters(params);
        
        IMethod other = pcType.newMethod();
        other.setDatatype("Money");
        other.setName("calc");
        params = new Parameter[2];
        params[0] = new Parameter(0, "x1", "Decimal");
        params[1] = new Parameter(1, "x2", "Money");
        other.setParameters(params); // ok, as setParameters() makes a copy of the array
        
        // ok case
        assertTrue(method.isSame(other));
        
        // different name
        other.setName("differentName");
        assertFalse(method.isSame(other));
        
        // different parameter type 
        other.setName("calc"); // make names equals again
        assertTrue(method.isSame(other)); // and test it
        params = new Parameter[2];
        params[0] = new Parameter(0, "x1", "Decimal");
        params[1] = new Parameter(1, "x2", "int");
        other.setParameters(params);
        assertFalse(method.isSame(other));
        
        // different number of parameters 
        params = new Parameter[3];
        params[0] = new Parameter(0, "x1", "Decimal");
        params[1] = new Parameter(1, "x2", "Money");
        params[2] = new Parameter(2, "x3", "int");
        other.setParameters(params);
        assertFalse(method.isSame(other));
        
    }

    /**
     * Tests for the correct type of excetion to be thrwon - no part of any type could ever be created.
     */
    public void testNewPart() {
    	try {
			method.newPart(IAttribute.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
}
