/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.type;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class MethodTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IType type;
    private IMethod method;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        type = newProductCmptType(ipsProject, "Product");
        method = type.newMethod();
    }
    
    public void testInitFromXml() {
        Element docElement = this.getTestDocument().getDocumentElement();
        
        method.initFromXml(XmlUtil.getElement(docElement, "Method", 0));
        assertEquals(42, method.getId());
        assertEquals("calcPremium", method.getName());
        assertEquals("Money", method.getDatatype());
        assertEquals(Modifier.PUBLIC, method.getModifier());
        assertTrue(method.isAbstract());
        IParameter[] params = method.getParameters();
        assertEquals(2, params.length);
        assertEquals("p1", params[0].getName());
        assertEquals("Money", params[0].getDatatype());
        assertEquals("p2", params[1].getName());
        assertEquals("Decimal", params[1].getDatatype());
        
        method.initFromXml(XmlUtil.getElement(docElement, "Method", 1));
        assertEquals(0, method.getNumOfParameters());
    }

    /*
     * Class under test for Element toXml(Document)
     */
    public void testToXmlDocument() {
        method = type.newMethod(); // => id=1, because it's the second method
        method.setName("getAge");
        method.setModifier(Modifier.PUBLIC);
        method.setDatatype("Decimal");
        method.setAbstract(true);
        IParameter param0 = method.newParameter();
        param0.setName("p0");
        param0.setDatatype("Decimal");
        IParameter param1 = method.newParameter();
        param1.setName("p1");
        param1.setDatatype("Money");

        Element element = method.toXml(this.newDocument());
        
        IMethod copy = type.newMethod();
        copy.initFromXml(element);
        IParameter[] copyParams = copy.getParameters();  
        assertEquals(1, copy.getId());
        assertEquals("getAge", copy.getName());
        assertEquals("Decimal", copy.getDatatype());
        assertEquals(Modifier.PUBLIC, copy.getModifier());
        assertTrue(copy.isAbstract());
        assertEquals(2, copyParams.length);
        assertEquals("p0", copyParams[0].getName());
        assertEquals("Decimal", copyParams[0].getDatatype());
        assertEquals("p1", copyParams[1].getName());
        assertEquals("Money", copyParams[1].getDatatype());

    }
    
    public void testNewParameter() {
        IParameter param = method.newParameter();
        assertEquals(1, method.getParameters().length);
        assertEquals(param, method.getParameters()[0]);
        assertTrue(method.getIpsSrcFile().isDirty());
    }
    
    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.type.Method#setName(java.lang.String)}.
     */
    public void testSetName() {
        testPropertyAccessReadWrite(Method.class, IMethod.PROPERTY_NAME, method, "calcPremium");
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.type.Method#setDatatype(java.lang.String)}.
     */
    public void testSetDatatype() {
        testPropertyAccessReadWrite(Method.class, IMethod.PROPERTY_DATATYPE, method, "Integer");
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.type.Method#setAbstract(boolean)}.
     */
    public void testSetAbstract() {
        testPropertyAccessReadWrite(Method.class, IMethod.PROPERTY_ABSTRACT, method, Boolean.TRUE);
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.type.Method#setModifier(org.faktorips.devtools.core.model.pctype.Modifier)}.
     */
    public void testSetModifier() {
        testPropertyAccessReadWrite(Method.class, IMethod.PROPERTY_MODIFIER, method, Modifier.PUBLIC);
    }

    public void testOverrides() {
        method.setName("calc");
        method.setDatatype("void");
        IParameter param0 = method.newParameter();
        param0.setName("p1");
        param0.setDatatype("Decimal");
        IParameter param1 = method.newParameter();
        param1.setName("p2");
        param1.setDatatype("Money");

        IMethod other = type.newMethod();
        other.setDatatype("Money");
        other.setName("calc");
        IParameter otherParam0 = other.newParameter();
        otherParam0.setName("x");
        otherParam0.setDatatype("Decimal");
        IParameter otherParam1 = other.newParameter();
        otherParam1.setName("y");
        otherParam1.setDatatype("Money");
        
        // ok case
        assertTrue(method.overrides(other));
        
        // different name
        other.setName("differentName");
        assertFalse(method.overrides(other));
        
        // different parameter type 
        other.setName("calc"); // make names equals again
        assertTrue(method.overrides(other)); // and test it
        otherParam1.setDatatype("int");
        assertFalse(method.overrides(other));
        
        // different number of parameters 
        other.newParameter();
        assertFalse(method.overrides(other));
    }

}
