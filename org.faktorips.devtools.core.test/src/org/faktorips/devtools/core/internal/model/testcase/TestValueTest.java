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

package org.faktorips.devtools.core.internal.model.testcase;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestValueTest extends AbstractIpsPluginTest {

    private ITestValue valueObjectInput;
    private ITestValue valueObjectInput2;
    private ITestValue valueObjectExpectedValue;
    private ITestValue valueObjectUnknown;
    
    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject("TestProject");
        
        ITestCaseType testCaseType = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculation");
        testCaseType.newInputTestValueParameter().setName("testValueParameter1");
        testCaseType.newInputTestValueParameter().setName("testValueParameter3");
        testCaseType.newExpectedResultValueParameter().setName("testValueParameter2");

        ITestCase testCase = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "PremiumCalculation");
        
        testCase.setTestCaseType(testCaseType.getName());
        (valueObjectInput = testCase.newTestValue()).setTestValueParameter("testValueParameter1");
        (valueObjectInput2 = testCase.newTestValue()).setTestValueParameter("testValueParameter3");
        (valueObjectExpectedValue = testCase.newTestValue()).setTestValueParameter("testValueParameter2");
        (valueObjectUnknown = testCase.newTestValue()).setTestValueParameter("testValueParameter4");
    }
    
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl, "ValueObject", 0);
        valueObjectInput.initFromXml(paramEl);
        assertEquals("newSumInsured", valueObjectInput.getTestValueParameter());
        assertEquals("500", valueObjectInput.getValue());
    }

    public void testToXml() {
        valueObjectInput.setTestValueParameter("Money");
        valueObjectInput.setValue("500");
        Element el = valueObjectInput.toXml(newDocument());
        
        valueObjectInput.setTestValueParameter("Test");
        valueObjectInput.setValue("Test");
        
        valueObjectInput.initFromXml(el);
        assertEquals("Money", valueObjectInput.getTestValueParameter());
        assertEquals("500", valueObjectInput.getValue());
    }
    
    public void testTestObjectRole(){
        assertTrue(valueObjectInput.isInput());
        assertFalse(valueObjectInput.isExpectedResult());
        assertFalse(valueObjectInput.isCombined());
        
        assertFalse(valueObjectExpectedValue.isInput());
        assertTrue(valueObjectExpectedValue.isExpectedResult());
        assertFalse(valueObjectExpectedValue.isCombined());
        
        assertTrue(valueObjectInput2.isInput());
        assertFalse(valueObjectInput2.isExpectedResult());
        assertFalse(valueObjectInput2.isCombined());
        
        assertFalse(valueObjectUnknown.isInput());
        assertFalse(valueObjectUnknown.isExpectedResult());
        // combined is the default role
        assertTrue(valueObjectUnknown.isCombined());
    }
}
