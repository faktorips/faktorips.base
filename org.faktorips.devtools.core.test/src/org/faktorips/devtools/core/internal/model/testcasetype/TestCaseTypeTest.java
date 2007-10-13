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

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.Dependency;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.core.util.CollectionUtil;
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
    private IIpsPackageFragmentRoot root;

    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject("TestProject");
        root = ipsProject.getIpsPackageFragmentRoots()[0];
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
        
        ITestRuleParameter param6 = type.newExpectedResultRuleParameter();
        assertNotNull(param6);
        assertEquals(3, type.getInputTestParameters().length);
        assertEquals(4, type.getExpectedResultTestParameters().length);
        assertFalse(param6.isInputParameter());
        assertTrue(param6.isExpextedResultParameter());
        assertFalse(param6.isCombinedParameter()); 
        
        // assert the correct storing of elements
        assertEquals(param, type.getInputTestParameters()[0]);
        assertEquals(param3, type.getInputTestParameters()[1]);
        assertEquals(param2, type.getExpectedResultTestParameters()[0]);
        assertEquals(param4, type.getExpectedResultTestParameters()[1]);
        assertEquals(param6, type.getExpectedResultTestParameters()[3]);
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
        ITestRuleParameter param9 = type.newExpectedResultRuleParameter();
        assertEquals(5, type.getExpectedResultTestParameters().length);
        assertEquals(param5, type.getExpectedResultTestParameters()[0]);
        assertEquals(param6, type.getExpectedResultTestParameters()[1]);
        assertEquals(param7, type.getExpectedResultTestParameters()[2]);
        assertEquals(param8, type.getExpectedResultTestParameters()[3]);
        assertEquals(param9, type.getExpectedResultTestParameters()[4]);
    }
    
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element typeEl = XmlUtil.getFirstElement(docEl);
        type.initFromXml(typeEl);
        assertEquals(4, type.getInputTestParameters().length);
        assertEquals(5, type.getExpectedResultTestParameters().length);
    }
    
    public void testToXml() {
      ITestValueParameter valueParamInput = type.newInputTestValueParameter();
      ITestValueParameter valueParamInput2 = type.newInputTestValueParameter();
      ITestValueParameter valueParamExpectedResult = type.newExpectedResultValueParameter();
      ITestRuleParameter ruleParamExpectedResult = type.newExpectedResultRuleParameter();
      ruleParamExpectedResult.setName("Rule1");
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
      assertEquals(3,  type.getExpectedResultTestParameters().length);
      assertEquals("Integer", ((ITestValueParameter) type.getInputTestParameters()[0]).getValueDatatype());
      assertEquals("Decimal", ((ITestValueParameter) type.getInputTestParameters()[1]).getValueDatatype());
      assertEquals("Money", ((ITestValueParameter) type.getExpectedResultTestParameters()[0]).getValueDatatype());
      assertEquals("Rule1", ((ITestRuleParameter) type.getExpectedResultTestParameters()[1]).getName());
    }

    public void testDependsOn() throws Exception {
        List dependsOnList = CollectionUtil.toArrayList(type.dependsOn());
        assertEquals(0, dependsOnList.size());
        
        IPolicyCmptType pcType1 = newPolicyCmptType(root, "PolicyCmptType1");
        IPolicyCmptType pcType2 = newPolicyCmptType(root, "PolicyCmptType2");
        IPolicyCmptType pcType3 = newPolicyCmptType(root, "PolicyCmptType3");
        
        // test dependency to policy cmpt type of root
        ITestPolicyCmptTypeParameter param1 = type.newInputTestPolicyCmptTypeParameter();
        param1.setPolicyCmptType(pcType1.getQualifiedName());
        dependsOnList = CollectionUtil.toArrayList(type.dependsOn());
        assertEquals(1, dependsOnList.size());
        assertTrue(dependsOnList.contains(Dependency.createReferenceDependency(type.getQualifiedNameType(), pcType1.getQualifiedNameType())));

        // test dependency to policy cmpt type of child
        ITestPolicyCmptTypeParameter param2 = param1.newTestPolicyCmptTypeParamChild();
        param2.setPolicyCmptType(pcType2.getQualifiedName());
        dependsOnList = CollectionUtil.toArrayList(type.dependsOn());
        assertEquals(2, dependsOnList.size());
        assertTrue(dependsOnList.contains(Dependency.createReferenceDependency(type.getQualifiedNameType(), pcType1.getQualifiedNameType())));
        assertTrue(dependsOnList.contains(Dependency.createReferenceDependency(type.getQualifiedNameType(), pcType2.getQualifiedNameType())));
 
        // test duplicate dependency
        ITestPolicyCmptTypeParameter param3 = param1.newTestPolicyCmptTypeParamChild();
        param3.setPolicyCmptType(pcType1.getQualifiedName());
        dependsOnList = CollectionUtil.toArrayList(type.dependsOn());
        assertEquals(2, dependsOnList.size());
        assertTrue(dependsOnList.contains(Dependency.createReferenceDependency(type.getQualifiedNameType(), pcType1.getQualifiedNameType())));
        assertTrue(dependsOnList.contains(Dependency.createReferenceDependency(type.getQualifiedNameType(), pcType2.getQualifiedNameType())));

        // test dependency to policy cmpt type child of child
        ITestPolicyCmptTypeParameter param4 = param3.newTestPolicyCmptTypeParamChild();
        param4.setPolicyCmptType(pcType3.getQualifiedName());
        dependsOnList = CollectionUtil.toArrayList(type.dependsOn());
        assertEquals(3, dependsOnList.size());
        assertTrue(dependsOnList.contains(Dependency.createReferenceDependency(type.getQualifiedNameType(), pcType1.getQualifiedNameType())));
        assertTrue(dependsOnList.contains(Dependency.createReferenceDependency(type.getQualifiedNameType(), pcType2.getQualifiedNameType())));
        assertTrue(dependsOnList.contains(Dependency.createReferenceDependency(type.getQualifiedNameType(), pcType3.getQualifiedNameType())));
    }
    
    public void testGetTestRuleCandidates() throws CoreException{
        IValidationRule[] testRuleParameters = type.getTestRuleCandidates();
        assertEquals(0, testRuleParameters.length);
        
        // create policy cmpts with validation rules 
        IPolicyCmptType policyCmptTypeBase = newPolicyCmptType(root, "PolicyCmptBase");
        IPolicyCmptType policyCmptType1 = newPolicyCmptType(root, "PolicyCmpt1");
        policyCmptType1.setSupertype(policyCmptTypeBase.getQualifiedName());
        IPolicyCmptType policyCmptType2 = newPolicyCmptType(root, "PolicyCmpt2");
        IValidationRule ruleBase = policyCmptTypeBase.newRule();
        ruleBase.setName("RuleBase");
        ruleBase.setMessageCode("RuleBase");
        IValidationRule rule1 = policyCmptType1.newRule();
        rule1.setName("Rule1");
        rule1.setMessageCode("Rule1");
        IValidationRule rule2 = policyCmptType2.newRule();
        rule2.setName("Rule2");
        rule2.setMessageCode("Rule2");
        
        type.newInputTestPolicyCmptTypeParameter(); // dummy with no rules
        ITestPolicyCmptTypeParameter param1 = type.newInputTestPolicyCmptTypeParameter();
        param1.setPolicyCmptType(policyCmptType1.getQualifiedName());
        param1.setName("PolicyCmpt1");
        ITestPolicyCmptTypeParameter childParam1 = param1.newTestPolicyCmptTypeParamChild();
        childParam1.setPolicyCmptType(policyCmptType2.getQualifiedName());
        childParam1.setRelation("Relation1");
        childParam1.setName("ChildPolicyCmpt2");
        testRuleParameters = type.getTestRuleCandidates();
        assertEquals(3, testRuleParameters.length);
        List testRuleParametersList = Arrays.asList(testRuleParameters);
        assertTrue(testRuleParametersList.contains(rule1));
        assertTrue(testRuleParametersList.contains(rule2));
        assertTrue(testRuleParametersList.contains(ruleBase));
    }
    
    public void testGetAllTestParameter() throws CoreException{
        ITestPolicyCmptTypeParameter testParameter1 = type.newInputTestPolicyCmptTypeParameter();
        ITestPolicyCmptTypeParameter testParameter1_1 = testParameter1.newTestPolicyCmptTypeParamChild();
        ITestPolicyCmptTypeParameter testParameter1_2 = testParameter1.newTestPolicyCmptTypeParamChild();
        ITestPolicyCmptTypeParameter testParameter1_1_1 = testParameter1_1.newTestPolicyCmptTypeParamChild();
        ITestPolicyCmptTypeParameter testParameter2 = type.newExpectedResultPolicyCmptTypeParameter();
        ITestPolicyCmptTypeParameter testParameter3 = type.newCombinedPolicyCmptTypeParameter();
        ITestRuleParameter ruleParameter = type.newExpectedResultRuleParameter();
        ITestValueParameter valueParameter = type.newInputTestValueParameter();
        
        ITestParameter[] params = ((TestCaseType)type).getAllTestParameter();
        List paramsList = Arrays.asList(params);
        assertEquals(""+8, ""+params.length);
        assertTrue(paramsList.contains(testParameter1));
        assertTrue(paramsList.contains(testParameter1_1));
        assertTrue(paramsList.contains(testParameter1_2));
        assertTrue(paramsList.contains(testParameter1_1_1));
        assertTrue(paramsList.contains(testParameter2));
        assertTrue(paramsList.contains(testParameter3));
        assertTrue(paramsList.contains(ruleParameter));
        assertTrue(paramsList.contains(valueParameter));
    }
}
