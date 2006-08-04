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
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private ITestCase testCase;
    
    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject("TestProject");
        testCase = (ITestCase) newIpsObject(ipsProject, IpsObjectType.TEST_CASE, "PremiumCalculationTest");
    }

    public void testNewParameter() {
        ITestValue param = testCase.newInputValue();
        assertNotNull(param);
        assertEquals(1, testCase.getInputObjects().length);
        assertEquals(0, testCase.getExpectedResultObjects().length);
        ITestValue param2 = testCase.newExpectedResultValue();
        assertNotNull(param2);
        assertEquals(1, testCase.getExpectedResultObjects().length);
        assertEquals(1, testCase.getInputObjects().length);
        
        ITestPolicyCmpt param3 = testCase.newInputPolicyCmpt();
        assertNotNull(param3);
        assertEquals(2, testCase.getInputObjects().length);
        assertEquals(1, testCase.getExpectedResultObjects().length);
        ITestPolicyCmpt param4 = testCase.newExpectedResultPolicyCmpt();
        assertNotNull(param4);
        assertEquals(2, testCase.getInputObjects().length);
        assertEquals(2, testCase.getExpectedResultObjects().length);
        
        assertEquals(param, testCase.getInputObjects()[0]);
        assertEquals(param3, testCase.getInputObjects()[1]);
        
        assertEquals(param2, testCase.getExpectedResultObjects()[0]);
        assertEquals(param4, testCase.getExpectedResultObjects()[1]);
    }
    
    public void testGetParameters() {
        assertEquals(0, testCase.getInputObjects().length);
        ITestValue param = testCase.newInputValue();
        ITestValue param2 = testCase.newInputValue();
        ITestPolicyCmpt param3 = testCase.newInputPolicyCmpt();
        ITestPolicyCmpt param4 = testCase.newInputPolicyCmpt();
        assertEquals(4, testCase.getInputObjects().length);
        assertEquals(param, testCase.getInputObjects()[0]);
        assertEquals(param2, testCase.getInputObjects()[1]);
        assertEquals(param3, testCase.getInputObjects()[2]);
        assertEquals(param4, testCase.getInputObjects()[3]);

        assertEquals(0, testCase.getExpectedResultObjects().length);
        ITestValue param5 = testCase.newExpectedResultValue();
        ITestValue param6 = testCase.newExpectedResultValue();
        ITestPolicyCmpt param7 = testCase.newExpectedResultPolicyCmpt();
        ITestPolicyCmpt param8 = testCase.newExpectedResultPolicyCmpt();
        assertEquals(4, testCase.getExpectedResultObjects().length);
        assertEquals(param5, testCase.getExpectedResultObjects()[0]);
        assertEquals(param6, testCase.getExpectedResultObjects()[1]);
        assertEquals(param7, testCase.getExpectedResultObjects()[2]);
        assertEquals(param8, testCase.getExpectedResultObjects()[3]);   
    }
    
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element typeEl = XmlUtil.getFirstElement(docEl);
        testCase.initFromXml(typeEl);
        assertEquals(4, testCase.getInputObjects().length);
        assertEquals(4, testCase.getExpectedResultObjects().length);
        assertEquals("testCaseType1", testCase.getTestCaseType());
    }
    
    public void testToXml() {
      ITestValue valueParamInput = testCase.newInputValue();
      ITestValue valueParamInput2 = testCase.newInputValue();
      ITestValue valueParamExpectedResult = testCase.newExpectedResultValue();
      ITestPolicyCmpt pctypeParamInput = testCase.newInputPolicyCmpt();
      ITestPolicyCmpt pctypeParamExpectedResult = testCase.newExpectedResultPolicyCmpt();
      
      testCase.setTestCaseType("testCaseType1");
      valueParamInput.setTestValueParameter("Integer");
      valueParamInput2.setTestValueParameter("Decimal");
      pctypeParamInput.setTestPolicyCmptType("test1");
      valueParamExpectedResult.setTestValueParameter("Money");
      pctypeParamExpectedResult.setTestPolicyCmptType("test2");
      
      Document doc = newDocument();
      Element el = testCase.toXml(doc);
      
      // overwrite parameter
      testCase.setTestCaseType("temp");
      valueParamInput.setTestValueParameter("Test");
      valueParamExpectedResult.setTestValueParameter("Test2");
      valueParamExpectedResult.setInputParameter(true);
      pctypeParamExpectedResult.setInputParameter(true);
      pctypeParamInput.setInputParameter(false);
      
      // read the xml which was written before
      testCase.initFromXml(el);
      assertEquals(3,  testCase.getInputObjects().length);
      assertEquals(2,  testCase.getExpectedResultObjects().length);
      assertEquals("Integer", ((ITestValue) testCase.getInputObjects()[0]).getTestValueParameter());
      assertEquals("Decimal", ((ITestValue) testCase.getInputObjects()[1]).getTestValueParameter());
      assertEquals("Money", ((ITestValue) testCase.getExpectedResultObjects()[0]).getTestValueParameter());
      assertEquals("testCaseType1", testCase.getTestCaseType());
    }
}
