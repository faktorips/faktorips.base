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
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.TestParameterRole;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestAttributeTest extends AbstractIpsPluginTest {

    private ITestAttribute testAttribute;
    
    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject("TestProject");
        ITestCaseType type = (ITestCaseType )newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculation");
        testAttribute = type.newExpectedResultPolicyCmptTypeParameter().newExpectedResultTestAttribute();
    }
    
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element attributeEl = XmlUtil.getFirstElement(docEl);
        testAttribute.initFromXml(attributeEl);
        assertEquals("attributeName1", testAttribute.getName());
        assertEquals("attribute1", testAttribute.getAttribute());
        assertTrue(testAttribute.isInputAttribute());
        assertFalse(testAttribute.isExpextedResultAttribute());
        
        attributeEl = XmlUtil.getElement(docEl, 1);
        testAttribute.initFromXml(attributeEl);
        assertEquals("attribute2", testAttribute.getAttribute());
        assertEquals("attributeName2", testAttribute.getName());
        assertFalse(testAttribute.isInputAttribute());
        assertTrue(testAttribute.isExpextedResultAttribute());

        attributeEl = XmlUtil.getElement(docEl, 2);
        testAttribute.initFromXml(attributeEl);
        assertEquals("attribute3", testAttribute.getAttribute());
        assertEquals("attributeName3", testAttribute.getName());
        assertFalse(testAttribute.isInputAttribute());
        assertFalse(testAttribute.isExpextedResultAttribute());
        
        attributeEl = XmlUtil.getElement(docEl, 3);
        testAttribute.initFromXml(attributeEl);
        assertEquals("attribute4", testAttribute.getAttribute());
        assertEquals("attributeName4", testAttribute.getName());
        assertFalse(testAttribute.isInputAttribute());
        assertFalse(testAttribute.isExpextedResultAttribute());
        
        boolean exceptionOccored = false;
        try {
            // test unsupported test attribute role
            ((TestAttribute)testAttribute).setTestAttributeRole(TestParameterRole.COMBINED);
        } catch (Exception e) {
            exceptionOccored = true;
        } finally{
            assertTrue(exceptionOccored);
        }
    }

    public void testToXml() {
        testAttribute.setAttribute("attribute2");
        ((TestAttribute)testAttribute).setTestAttributeRole(TestParameterRole.INPUT);
        testAttribute.setName("attributeName2");
        Element el = testAttribute.toXml(newDocument());

        testAttribute.setAttribute("test");
        testAttribute.setName("attributeName3");
        ((TestAttribute)testAttribute).setTestAttributeRole(TestParameterRole.EXPECTED_RESULT);
        
        testAttribute.initFromXml(el);
        assertEquals("attribute2", testAttribute.getAttribute());
        assertEquals("attributeName2", testAttribute.getName());
        assertTrue(testAttribute.isInputAttribute());
        assertFalse(testAttribute.isExpextedResultAttribute());
    }
}
