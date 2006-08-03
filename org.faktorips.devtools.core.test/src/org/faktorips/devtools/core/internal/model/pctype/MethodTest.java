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

package org.faktorips.devtools.core.internal.model.pctype;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IParameter;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;


/**
 *
 */
public class MethodTest extends AbstractIpsPluginTest {
    
    private IIpsSrcFile ipsSrcFile;
    private PolicyCmptType pcType;
    private IMethod method;
    
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject ipsProject= newIpsProject("TestProject");
        pcType = newPolicyCmptType(ipsProject, "Policy");
        ipsSrcFile = pcType.getIpsSrcFile();
        method = pcType.newMethod();
    }
    
    public void testRemove() {
        method.delete();
        assertEquals(0, pcType.getAttributes().length);
        assertTrue(ipsSrcFile.isDirty());
    }
    
    public void testSetDatatype() {
        method.setDatatype("Money");
        assertEquals("Money", method.getDatatype());
        assertTrue(ipsSrcFile.isDirty());
    }
    
    public void testNewParameter() {
        IParameter param = method.newParameter();
        assertEquals(1, method.getParameters().length);
        assertEquals(param, method.getParameters()[0]);
        assertTrue(ipsSrcFile.isDirty());
    }
    
    public void testInitFromXml() {
        Element docElement = this.getTestDocument().getDocumentElement();
        
        method.initFromXml(XmlUtil.getElement(docElement, Method.TAG_NAME, 0));
        assertEquals(42, method.getId());
        assertEquals("calcPremium", method.getName());
        assertEquals("Money", method.getDatatype());
        assertEquals(Modifier.PRIVATE, method.getModifier());
        assertTrue(method.isAbstract());
        IParameter[] params = method.getParameters();
        assertEquals(2, params.length);
        assertEquals("p1", params[0].getName());
        assertEquals("Money", params[0].getDatatype());
        assertEquals("p2", params[1].getName());
        assertEquals("Decimal", params[1].getDatatype());
        
        method.initFromXml(XmlUtil.getElement(docElement, Method.TAG_NAME, 1));
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
        IParameter param0 = method.newParameter();
        param0.setName("p0");
        param0.setDatatype("Decimal");
        IParameter param1 = method.newParameter();
        param1.setName("p1");
        param1.setDatatype("Money");

        Element element = method.toXml(this.newDocument());
        
        Method copy = new Method();
        copy.initFromXml(element);
        IParameter[] copyParams = copy.getParameters();  
        assertEquals(1, copy.getId());
        assertEquals("getAge", copy.getName());
        assertEquals("Decimal", copy.getDatatype());
        assertEquals(Modifier.PRIVATE, copy.getModifier());
        assertTrue(copy.isAbstract());
        assertEquals(2, copyParams.length);
        assertEquals("p0", copyParams[0].getName());
        assertEquals("Decimal", copyParams[0].getDatatype());
        assertEquals("p1", copyParams[1].getName());
        assertEquals("Money", copyParams[1].getDatatype());

    }
    
    public void testIsSame() {
        method.setName("calc");
        method.setDatatype("void");
        IParameter param0 = method.newParameter();
        param0.setName("p1");
        param0.setDatatype("Decimal");
        IParameter param1 = method.newParameter();
        param1.setName("p2");
        param1.setDatatype("Money");

        IMethod other = pcType.newMethod();
        other.setDatatype("Money");
        other.setName("calc");
        IParameter otherParam0 = other.newParameter();
        otherParam0.setName("x");
        otherParam0.setDatatype("Decimal");
        IParameter otherParam1 = other.newParameter();
        otherParam1.setName("y");
        otherParam1.setDatatype("Money");
        
        // ok case
        assertTrue(method.isSame(other));
        
        // different name
        other.setName("differentName");
        assertFalse(method.isSame(other));
        
        // different parameter type 
        other.setName("calc"); // make names equals again
        assertTrue(method.isSame(other)); // and test it
        otherParam1.setDatatype("int");
        assertFalse(method.isSame(other));
        
        // different number of parameters 
        other.newParameter();
        assertFalse(method.isSame(other));
        
    }

    public void testNewPart() {
        assertNotNull(method.newPart(IParameter.class));
        
    	try {
			method.newPart(IAttribute.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
}
