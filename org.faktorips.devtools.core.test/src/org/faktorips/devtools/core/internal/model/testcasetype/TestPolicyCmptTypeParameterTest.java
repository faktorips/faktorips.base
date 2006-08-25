/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.internal.model.testcasetype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestPolicyCmptTypeParameterTest extends AbstractIpsPluginTest {

    private ITestPolicyCmptTypeParameter policyCmptTypeParameterInput;
    private ITestPolicyCmptTypeParameter policyCmptTypeParameterExpectedResult;
    private ITestPolicyCmptTypeParameter policyCmptTypeParameterCombined;

    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject("TestProject");
        ITestCaseType type = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculation");
        policyCmptTypeParameterInput = type.newInputTestPolicyCmptTypeParameter();
        policyCmptTypeParameterExpectedResult = type.newExpectedResultPolicyCmptTypeParameter();
        policyCmptTypeParameterCombined = type.newCombinedPolicyCmptTypeParameter();
    }

    public void testIsRootParameter() {
        assertTrue(policyCmptTypeParameterInput.isRoot());
        ITestPolicyCmptTypeParameter targetChild = policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();
        assertFalse(targetChild.isRoot());
    }

    public void testNewAttributesDependsOnRole() throws Exception{
        boolean exceptionThrown = false;
        try{
            policyCmptTypeParameterInput.newExpectedResultTestAttribute();
        }catch (Exception e){
            exceptionThrown = true;
            assertEquals(e.getClass(), IllegalArgumentException.class);
        }
        assertTrue(exceptionThrown);
        policyCmptTypeParameterInput.newInputTestAttribute();

        exceptionThrown = false;
        try{
            policyCmptTypeParameterExpectedResult.newInputTestAttribute();
        }catch (Exception e){
            exceptionThrown = true;
            assertEquals(e.getClass(), IllegalArgumentException.class);
        }
        assertTrue(exceptionThrown);
        policyCmptTypeParameterExpectedResult.newExpectedResultTestAttribute();

        exceptionThrown = false;
        try{
            policyCmptTypeParameterCombined.newInputTestAttribute();
            policyCmptTypeParameterCombined.newExpectedResultTestAttribute();
        }catch (CoreException e){
            exceptionThrown = true;
        }
        assertFalse(exceptionThrown);
        
        policyCmptTypeParameterInput.newInputTestAttribute();
    }
    
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl, "PolicyCmptTypeParameter", 0);
        policyCmptTypeParameterInput.initFromXml(paramEl);
        assertEquals(2, policyCmptTypeParameterInput.getTestPolicyCmptTypeParamChilds().length);
        assertEquals(3, policyCmptTypeParameterInput.getTestAttributes().length);
        assertTrue(policyCmptTypeParameterInput.isInputParameter());
        assertFalse(policyCmptTypeParameterInput.isExpextedResultParameter());
        assertFalse(policyCmptTypeParameterInput.isCombinedParameter());
        assertTargetTestPolicyCmptTypeParameter(policyCmptTypeParameterInput, "policyCmptType1", "base.Test1",
                "relation1", 2, 3, true, false, false);
        assertTargetTestPolicyCmptTypeParameter(policyCmptTypeParameterInput
                .getTestPolicyCmptTypeParamChild("policyCmptType2"), "policyCmptType2", "base.Test2", "relation2", 4,
                5, false, true, false);
        assertTargetTestPolicyCmptTypeParameter(policyCmptTypeParameterInput
                .getTestPolicyCmptTypeParamChild("policyCmptType3"), "policyCmptType3", "base.Test3", "relation3", 6,
                7, true, true, true);
        assertTrue(policyCmptTypeParameterInput.isRequiresProductCmpt());

        paramEl = XmlUtil.getElement(docEl, "PolicyCmptTypeParameter", 1);
        policyCmptTypeParameterInput.initFromXml(paramEl);
        assertFalse(policyCmptTypeParameterInput.isInputParameter());
        assertTrue(policyCmptTypeParameterInput.isExpextedResultParameter());
        assertFalse(policyCmptTypeParameterInput.isCombinedParameter());
        assertTargetTestPolicyCmptTypeParameter(policyCmptTypeParameterInput
                .getTestPolicyCmptTypeParamChild("policyCmptType4.1"), "policyCmptType4.1", "base.Test4.1",
                "relation4.1", 1, 1, true, false, false);

        paramEl = XmlUtil.getElement(docEl, "PolicyCmptTypeParameter", 2);
        policyCmptTypeParameterInput.initFromXml(paramEl);
        assertTrue(policyCmptTypeParameterInput.isInputParameter());
        assertTrue(policyCmptTypeParameterInput.isExpextedResultParameter());
        assertTrue(policyCmptTypeParameterInput.isCombinedParameter());
        assertTargetTestPolicyCmptTypeParameter(policyCmptTypeParameterInput
                .getTestPolicyCmptTypeParamChild("policyCmptType5.1"), "policyCmptType5.1", "base.Test5.1",
                "relation5.1", 2, 2, true, true, true);

        // wrong type role doesn't result in an exception,
        // the parameter will be parsed and stored as unknown parameter type role
        paramEl = XmlUtil.getElement(docEl, "PolicyCmptTypeParameter", 3);
        policyCmptTypeParameterInput.initFromXml(paramEl);
        assertFalse(policyCmptTypeParameterInput.isInputParameter());
        assertFalse(policyCmptTypeParameterInput.isExpextedResultParameter());
        assertFalse(policyCmptTypeParameterInput.isCombinedParameter());
    }

    public void testToXml() throws Exception {
        policyCmptTypeParameterInput.setName("Name1");
        policyCmptTypeParameterInput.setPolicyCmptType("base.Test2");
        policyCmptTypeParameterInput.setRelation("relation1");
        policyCmptTypeParameterInput.setMinInstances(7);
        policyCmptTypeParameterInput.setMaxInstances(8);
        policyCmptTypeParameterInput.setRequiresProductCmpt(true);
        policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();
        ((TestParameter)policyCmptTypeParameterInput).setTestParameterRole(TestParameterRole.INPUT);
        ITestPolicyCmptTypeParameter targetChild = policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();

        policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();
        targetChild.setRelation("relation1");
        targetChild.setPolicyCmptType("base.Test4");
        targetChild.setName("childpolicyCmptType1");
        targetChild.setMinInstances(7);
        targetChild.setMaxInstances(8);
        policyCmptTypeParameterInput.newInputTestAttribute();

        Element el = policyCmptTypeParameterInput.toXml(newDocument());

        // overwrite with wrong data
        policyCmptTypeParameterInput.setPolicyCmptType("base.Test3");
        policyCmptTypeParameterInput.setRelation("relation2");
        policyCmptTypeParameterInput.setMinInstances(9);
        policyCmptTypeParameterInput.setMaxInstances(10);
        policyCmptTypeParameterInput.setRequiresProductCmpt(false);
        policyCmptTypeParameterInput.newInputTestAttribute();
        policyCmptTypeParameterInput.newInputTestAttribute();
        ((TestParameter)policyCmptTypeParameterInput).setTestParameterRole(TestParameterRole.EXPECTED_RESULT);
        policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();

        // check the value stored before
        policyCmptTypeParameterInput.initFromXml(el);
        assertTargetTestPolicyCmptTypeParameter(policyCmptTypeParameterInput, "Name1", "base.Test2", "relation1", 7, 8,
                true, false, false);
        assertEquals(3, policyCmptTypeParameterInput.getTestPolicyCmptTypeParamChilds().length);
        assertEquals(1, policyCmptTypeParameterInput.getTestAttributes().length);
        assertTargetTestPolicyCmptTypeParameter(policyCmptTypeParameterInput, "Name1", "base.Test2", "relation1", 7, 8,
                true, false, false);
        assertTargetTestPolicyCmptTypeParameter(policyCmptTypeParameterInput
                .getTestPolicyCmptTypeParamChild("childpolicyCmptType1"), "childpolicyCmptType1", "base.Test4",
                "relation1", 7, 8, false, false, false);
        assertTrue(policyCmptTypeParameterInput.isRequiresProductCmpt());
    }
    
    private void assertTargetTestPolicyCmptTypeParameter(ITestPolicyCmptTypeParameter targetChild,
            String name,
            String policyCmptTypeName,
            String relationName,
            int min,
            int max,
            boolean isInput,
            boolean isExpected,
            boolean isCombined) {
        assertNotNull(targetChild);
        assertEquals(name, targetChild.getName());
        assertEquals(policyCmptTypeName, targetChild.getPolicyCmptType());
        assertEquals(relationName, targetChild.getRelation());
        assertEquals(min, targetChild.getMinInstances());
        assertEquals(max, targetChild.getMaxInstances());
        assertEquals(isInput, targetChild.isInputParameter());
        assertEquals(isExpected, targetChild.isExpextedResultParameter());
        assertEquals(isCombined, targetChild.isCombinedParameter());
    }
}
