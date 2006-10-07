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

package org.faktorips.devtools.core.internal.model.testcasetype;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestAttributeTest extends AbstractIpsPluginTest {

    private ITestAttribute testAttribute;
    private IIpsProject project;
    
    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        ITestCaseType type = (ITestCaseType )newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculation");
        testAttribute = type.newExpectedResultPolicyCmptTypeParameter().newExpectedResultTestAttribute();
    }
    
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element attributeEl = XmlUtil.getFirstElement(docEl);
        testAttribute.initFromXml(attributeEl);
        assertEquals("attribute1", testAttribute.getAttribute());
        assertEquals("attribute1Name", testAttribute.getName());
        assertTrue(testAttribute.isInputAttribute());
        assertFalse(testAttribute.isExpextedResultAttribute());
        
        attributeEl = XmlUtil.getElement(docEl, 1);
        testAttribute.initFromXml(attributeEl);
        assertEquals("attribute2", testAttribute.getAttribute());
        assertEquals("attribute2Name", testAttribute.getName());
        assertFalse(testAttribute.isInputAttribute());
        assertTrue(testAttribute.isExpextedResultAttribute());

        attributeEl = XmlUtil.getElement(docEl, 2);
        testAttribute.initFromXml(attributeEl);
        assertEquals("attribute3", testAttribute.getAttribute());
        assertEquals("attribute3Name", testAttribute.getName());
        assertFalse(testAttribute.isInputAttribute());
        assertFalse(testAttribute.isExpextedResultAttribute());
        
        attributeEl = XmlUtil.getElement(docEl, 3);
        testAttribute.initFromXml(attributeEl);
        assertEquals("attribute4", testAttribute.getAttribute());
        assertEquals("attribute4Name", testAttribute.getName());
        assertFalse(testAttribute.isInputAttribute());
        assertFalse(testAttribute.isExpextedResultAttribute());
        
        boolean exceptionOccored = false;
        try {
            // test unsupported test attribute type
            ((TestAttribute)testAttribute).setTestAttributeType(TestParameterType.COMBINED);
        } catch (Exception e) {
            exceptionOccored = true;
        } finally{
            assertTrue(exceptionOccored);
        }
    }

    public void testToXml() {
        testAttribute.setAttribute("attribute2");
        testAttribute.setName("attribute2Name");
        ((TestAttribute)testAttribute).setTestAttributeType(TestParameterType.INPUT);
        Element el = testAttribute.toXml(newDocument());

        testAttribute.setAttribute("attributeName3");
        testAttribute.setName("attribute3Name");
        ((TestAttribute)testAttribute).setTestAttributeType(TestParameterType.EXPECTED_RESULT);
        
        testAttribute.initFromXml(el);
        assertEquals("attribute2", testAttribute.getAttribute());
        assertEquals("attribute2Name", testAttribute.getName());
        assertTrue(testAttribute.isInputAttribute());
        assertFalse(testAttribute.isExpextedResultAttribute());
    }
    
    public void testFindAttribute() throws Exception{
        IPolicyCmptType policyCmptTypeSuper = newPolicyCmptType(project, "policyCmptSuper");
        IAttribute attr1 = policyCmptTypeSuper.newAttribute();
        attr1.setName("attribute1");
        IAttribute attr2 = policyCmptTypeSuper.newAttribute();
        attr2.setName("attribute2");
        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmpt");
        IAttribute attr3 = policyCmptType.newAttribute();
        attr3.setName("attribute3");
        IAttribute attr4 = policyCmptType.newAttribute();
        attr4.setName("attribute4");
        policyCmptType.setSupertype(policyCmptTypeSuper.getQualifiedName());
        
        ((ITestPolicyCmptTypeParameter)testAttribute.getParent()).setPolicyCmptType("policyCmpt");
        testAttribute.setAttribute("attribute4");
        assertEquals(attr4, testAttribute.findAttribute());
        testAttribute.setAttribute("attribute3");
        assertEquals(attr3, testAttribute.findAttribute());
        testAttribute.setAttribute("attribute2");
        assertEquals(attr2, testAttribute.findAttribute());
        testAttribute.setAttribute("attribute1");
        assertEquals(attr1, testAttribute.findAttribute());
    }
}
