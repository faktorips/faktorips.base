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
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTypeTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private ITestCaseType type;
    
    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject("TestProject");
        type = (ITestCaseType) newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculationTest");
    }

    public void testNewParameter() {
        ITestValueParameter param = type.newInputTestValueParameter();
        assertNotNull(param);
        assertEquals(1, type.getInputTestParameters().length);
        assertEquals(0, type.getExpectedResultTestParameters().length);
        assertTrue(param.isInputParameter());
        assertFalse(param.isExpextedResultParameter());
        assertFalse(param.isCombinedParameter());
        
        ITestValueParameter param2 = type.newExpectedResultValueParameter();
        assertNotNull(param2);
        assertEquals(1, type.getExpectedResultTestParameters().length);
        assertEquals(1, type.getInputTestParameters().length);
        assertFalse(param2.isInputParameter());
        assertTrue(param2.isExpextedResultParameter());
        assertFalse(param2.isCombinedParameter());
        
        ITestPolicyCmptTypeParameter param3 = type.newInputTestPolicyCmptTypeParameter();
        assertNotNull(param3);
        assertEquals(2, type.getInputTestParameters().length);
        assertEquals(1, type.getExpectedResultTestParameters().length);
        assertTrue(param3.isInputParameter());
        assertFalse(param3.isExpextedResultParameter());
        assertFalse(param3.isCombinedParameter());        
        
        ITestPolicyCmptTypeParameter param4 = type.newExpectedResultPolicyCmptTypeParameter();
        assertNotNull(param4);
        assertEquals(2, type.getInputTestParameters().length);
        assertEquals(2, type.getExpectedResultTestParameters().length);
        assertFalse(param4.isInputParameter());
        assertTrue(param4.isExpextedResultParameter());
        assertFalse(param4.isCombinedParameter());  
        
        ITestPolicyCmptTypeParameter param5 = type.newCombinedPolicyCmptTypeParameter();
        assertNotNull(param5);
        assertEquals(3, type.getInputTestParameters().length);
        assertEquals(3, type.getExpectedResultTestParameters().length);
        assertTrue(param5.isInputParameter());
        assertTrue(param5.isExpextedResultParameter());
        assertTrue(param5.isCombinedParameter()); 
        
        // assert the correct storing of elements
        assertEquals(param, type.getInputTestParameters()[0]);
        assertEquals(param3, type.getInputTestParameters()[1]);
        assertEquals(param2, type.getExpectedResultTestParameters()[0]);
        assertEquals(param4, type.getExpectedResultTestParameters()[1]);
        // the combined element will be returned in the input and exp result list
        assertEquals(param5, type.getExpectedResultTestParameters()[2]);   
        assertEquals(param5, type.getInputTestParameters()[2]); 
    }
    
    public void testGetParameters() {
        assertEquals(0, type.getInputTestParameters().length);
        ITestValueParameter param = type.newInputTestValueParameter();
        ITestValueParameter param2 = type.newInputTestValueParameter();
        ITestPolicyCmptTypeParameter param3 = type.newInputTestPolicyCmptTypeParameter();
        ITestPolicyCmptTypeParameter param4 = type.newInputTestPolicyCmptTypeParameter();
        assertEquals(4, type.getInputTestParameters().length);
        assertEquals(param, type.getInputTestParameters()[0]);
        assertEquals(param2, type.getInputTestParameters()[1]);
        assertEquals(param3, type.getInputTestParameters()[2]);
        assertEquals(param4, type.getInputTestParameters()[3]);

        assertEquals(0, type.getExpectedResultTestParameters().length);
        ITestValueParameter param5 = type.newExpectedResultValueParameter();
        ITestValueParameter param6 = type.newExpectedResultValueParameter();
        ITestPolicyCmptTypeParameter param7 = type.newExpectedResultPolicyCmptTypeParameter();
        ITestPolicyCmptTypeParameter param8 = type.newExpectedResultPolicyCmptTypeParameter();
        assertEquals(4, type.getExpectedResultTestParameters().length);
        assertEquals(param5, type.getExpectedResultTestParameters()[0]);
        assertEquals(param6, type.getExpectedResultTestParameters()[1]);
        assertEquals(param7, type.getExpectedResultTestParameters()[2]);
        assertEquals(param8, type.getExpectedResultTestParameters()[3]);   
    }
    
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element typeEl = XmlUtil.getFirstElement(docEl);
        type.initFromXml(typeEl);
        assertEquals(4, type.getInputTestParameters().length);
        assertEquals(4, type.getExpectedResultTestParameters().length);
    }
    
    public void testToXml() {
      ITestValueParameter valueParamInput = type.newInputTestValueParameter();
      ITestValueParameter valueParamInput2 = type.newInputTestValueParameter();
      ITestValueParameter valueParamExpectedResult = type.newExpectedResultValueParameter();
      type.newInputTestPolicyCmptTypeParameter();
      type.newExpectedResultPolicyCmptTypeParameter();
      
      valueParamInput.setValueDatatype("Integer");
      valueParamInput2.setValueDatatype("Decimal");
      valueParamExpectedResult.setValueDatatype("Money");
      
      Document doc = newDocument();
      Element el = type.toXml(doc);
      
      // overwrite parameter
      valueParamInput.setValueDatatype("Test");
      valueParamExpectedResult.setValueDatatype("Test2");
      
      // read the xml which was written before
      type.initFromXml(el);
      assertEquals(3,  type.getInputTestParameters().length);
      assertEquals(2,  type.getExpectedResultTestParameters().length);
      assertEquals("Integer", ((ITestValueParameter) type.getInputTestParameters()[0]).getValueDatatype());
      assertEquals("Decimal", ((ITestValueParameter) type.getInputTestParameters()[1]).getValueDatatype());
      assertEquals("Money", ((ITestValueParameter) type.getExpectedResultTestParameters()[0]).getValueDatatype());
    }
}
