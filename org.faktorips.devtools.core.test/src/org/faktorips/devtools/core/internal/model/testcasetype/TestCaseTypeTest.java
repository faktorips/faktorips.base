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
        ITestValueParameter param = type.newInputValueParameter();
        assertNotNull(param);
        assertEquals(1, type.getInputParameters().length);
        assertEquals(0, type.getExpectedResultParameter().length);
        ITestValueParameter param2 = type.newExpectedResultValueParameter();
        assertNotNull(param2);
        assertEquals(1, type.getExpectedResultParameter().length);
        assertEquals(1, type.getInputParameters().length);
        
        ITestPolicyCmptTypeParameter param3 = type.newInputPolicyCmptTypeParameter();
        assertNotNull(param3);
        assertEquals(2, type.getInputParameters().length);
        assertEquals(1, type.getExpectedResultParameter().length);
        ITestPolicyCmptTypeParameter param4 = type.newExpectedResultPolicyCmptParameter();
        assertNotNull(param4);
        assertEquals(2, type.getInputParameters().length);
        assertEquals(2, type.getExpectedResultParameter().length);
        
        assertEquals(param, type.getInputParameters()[0]);
        assertEquals(param3, type.getInputParameters()[1]);
        
        assertEquals(param2, type.getExpectedResultParameter()[0]);
        assertEquals(param4, type.getExpectedResultParameter()[1]);
    }
    
    public void testGetParameters() {
        assertEquals(0, type.getInputParameters().length);
        ITestValueParameter param = type.newInputValueParameter();
        ITestValueParameter param2 = type.newInputValueParameter();
        ITestPolicyCmptTypeParameter param3 = type.newInputPolicyCmptTypeParameter();
        ITestPolicyCmptTypeParameter param4 = type.newInputPolicyCmptTypeParameter();
        assertEquals(4, type.getInputParameters().length);
        assertEquals(param, type.getInputParameters()[0]);
        assertEquals(param2, type.getInputParameters()[1]);
        assertEquals(param3, type.getInputParameters()[2]);
        assertEquals(param4, type.getInputParameters()[3]);

        assertEquals(0, type.getExpectedResultParameter().length);
        ITestValueParameter param5 = type.newExpectedResultValueParameter();
        ITestValueParameter param6 = type.newExpectedResultValueParameter();
        ITestPolicyCmptTypeParameter param7 = type.newExpectedResultPolicyCmptParameter();
        ITestPolicyCmptTypeParameter param8 = type.newExpectedResultPolicyCmptParameter();
        assertEquals(4, type.getExpectedResultParameter().length);
        assertEquals(param5, type.getExpectedResultParameter()[0]);
        assertEquals(param6, type.getExpectedResultParameter()[1]);
        assertEquals(param7, type.getExpectedResultParameter()[2]);
        assertEquals(param8, type.getExpectedResultParameter()[3]);   
    }
    
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element typeEl = XmlUtil.getFirstElement(docEl);
        type.initFromXml(typeEl);
        assertEquals(4, type.getInputParameters().length);
        assertEquals(4, type.getExpectedResultParameter().length);
    }
    
    public void testToXml() {
      ITestValueParameter valueParamInput = type.newInputValueParameter();
      ITestValueParameter valueParamInput2 = type.newInputValueParameter();
      ITestValueParameter valueParamExpectedResult = type.newExpectedResultValueParameter();
      type.newInputPolicyCmptTypeParameter();
      ITestPolicyCmptTypeParameter pctypeParamExpectedResult = type.newExpectedResultPolicyCmptParameter();
      
      valueParamInput.setValueDatatype("Integer");
      valueParamInput2.setValueDatatype("Decimal");
      valueParamExpectedResult.setValueDatatype("Money");
      
      Document doc = newDocument();
      Element el = type.toXml(doc);
      
      // overwrite parameter
      valueParamInput.setValueDatatype("Test");
      valueParamExpectedResult.setValueDatatype("Test2");
      valueParamExpectedResult.setInputParameter(true);
      pctypeParamExpectedResult.setInputParameter(true);
      
      // read the xml which was written before
      type.initFromXml(el);
      assertEquals(3,  type.getInputParameters().length);
      assertEquals(2,  type.getExpectedResultParameter().length);
      assertEquals("Integer", ((ITestValueParameter) type.getInputParameters()[0]).getValueDatatype());
      assertEquals("Decimal", ((ITestValueParameter) type.getInputParameters()[1]).getValueDatatype());
      assertEquals("Money", ((ITestValueParameter) type.getExpectedResultParameter()[0]).getValueDatatype());
    }
}
