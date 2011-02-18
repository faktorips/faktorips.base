/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.testcasetype;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestPolicyCmptTypeParameterTest extends AbstractIpsPluginTest {

    private ITestPolicyCmptTypeParameter policyCmptTypeParameterInput;
    private IIpsProject project;
    private ITestCaseType testCaseType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        PolicyCmptType pct = newPolicyCmptType(project, "TestPolicy");
        testCaseType = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculation");
        policyCmptTypeParameterInput = testCaseType.newInputTestPolicyCmptTypeParameter();
        policyCmptTypeParameterInput.setName("Parameter1");
        policyCmptTypeParameterInput.setPolicyCmptType(pct.getQualifiedName());
    }

    @Test
    public void testIsRootParameter() {
        assertTrue(policyCmptTypeParameterInput.isRoot());
        ITestPolicyCmptTypeParameter targetChild = policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();
        assertFalse(targetChild.isRoot());
    }

    @Test
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getElement(docEl, "PolicyCmptTypeParameter", 0);
        policyCmptTypeParameterInput.initFromXml(paramEl);
        assertEquals(2, policyCmptTypeParameterInput.getTestPolicyCmptTypeParamChilds().length);
        assertEquals(3, policyCmptTypeParameterInput.getTestAttributes().length);
        assertTrue(policyCmptTypeParameterInput.isInputOrCombinedParameter());
        assertFalse(policyCmptTypeParameterInput.isExpextedResultOrCombinedParameter());
        assertFalse(policyCmptTypeParameterInput.isCombinedParameter());
        assertTargetTestPolicyCmptTypeParameter(policyCmptTypeParameterInput, "policyCmptType1", "base.Test1",
                "association1", 2, 3, true, false, false);
        assertTargetTestPolicyCmptTypeParameter(
                policyCmptTypeParameterInput.getTestPolicyCmptTypeParamChild("policyCmptType2"), "policyCmptType2",
                "base.Test2", "association2", 4, 5, false, true, false);
        assertTargetTestPolicyCmptTypeParameter(
                policyCmptTypeParameterInput.getTestPolicyCmptTypeParamChild("policyCmptType3"), "policyCmptType3",
                "base.Test3", "association3", 6, 7, true, true, true);
        assertTrue(policyCmptTypeParameterInput.isRequiresProductCmpt());

        paramEl = XmlUtil.getElement(docEl, "PolicyCmptTypeParameter", 1);
        policyCmptTypeParameterInput.initFromXml(paramEl);
        assertFalse(policyCmptTypeParameterInput.isInputOrCombinedParameter());
        assertTrue(policyCmptTypeParameterInput.isExpextedResultOrCombinedParameter());
        assertFalse(policyCmptTypeParameterInput.isCombinedParameter());
        assertTargetTestPolicyCmptTypeParameter(
                policyCmptTypeParameterInput.getTestPolicyCmptTypeParamChild("policyCmptType4.1"), "policyCmptType4.1",
                "base.Test4.1", "association4.1", 1, 1, true, false, false);

        paramEl = XmlUtil.getElement(docEl, "PolicyCmptTypeParameter", 2);
        policyCmptTypeParameterInput.initFromXml(paramEl);
        assertTrue(policyCmptTypeParameterInput.isInputOrCombinedParameter());
        assertTrue(policyCmptTypeParameterInput.isExpextedResultOrCombinedParameter());
        assertTrue(policyCmptTypeParameterInput.isCombinedParameter());
        assertTargetTestPolicyCmptTypeParameter(
                policyCmptTypeParameterInput.getTestPolicyCmptTypeParamChild("policyCmptType5.1"), "policyCmptType5.1",
                "base.Test5.1", "association5.1", 2, 2, true, true, true);

        // wrong type type doesn't result in an exception,
        // the parameter will be parsed and stored as unknown parameter type type
        paramEl = XmlUtil.getElement(docEl, "PolicyCmptTypeParameter", 3);
        policyCmptTypeParameterInput.initFromXml(paramEl);
        assertFalse(policyCmptTypeParameterInput.isInputOrCombinedParameter());
        assertFalse(policyCmptTypeParameterInput.isExpextedResultOrCombinedParameter());
        assertFalse(policyCmptTypeParameterInput.isCombinedParameter());
    }

    @Test
    public void testToXml() throws Exception {
        policyCmptTypeParameterInput.setName("Name1");
        policyCmptTypeParameterInput.setPolicyCmptType("base.Test2");
        policyCmptTypeParameterInput.setAssociation("association1");
        policyCmptTypeParameterInput.setMinInstances(7);
        policyCmptTypeParameterInput.setMaxInstances(8);
        policyCmptTypeParameterInput.setRequiresProductCmpt(true);
        policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();
        policyCmptTypeParameterInput.setTestParameterType(TestParameterType.INPUT);
        ITestPolicyCmptTypeParameter targetChild = policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();

        policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();
        targetChild.setAssociation("association1");
        targetChild.setPolicyCmptType("base.Test4");
        targetChild.setName("childpolicyCmptType1");
        targetChild.setMinInstances(7);
        targetChild.setMaxInstances(8);
        policyCmptTypeParameterInput.newInputTestAttribute();

        Element el = policyCmptTypeParameterInput.toXml(newDocument());

        // overwrite with wrong data
        policyCmptTypeParameterInput.setPolicyCmptType("base.Test3");
        policyCmptTypeParameterInput.setAssociation("association2");
        policyCmptTypeParameterInput.setMinInstances(9);
        policyCmptTypeParameterInput.setMaxInstances(10);
        policyCmptTypeParameterInput.setRequiresProductCmpt(false);
        policyCmptTypeParameterInput.newInputTestAttribute();
        policyCmptTypeParameterInput.newInputTestAttribute();
        policyCmptTypeParameterInput.setTestParameterType(TestParameterType.EXPECTED_RESULT);
        policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();

        // check the value stored before
        policyCmptTypeParameterInput.initFromXml(el);
        assertTargetTestPolicyCmptTypeParameter(policyCmptTypeParameterInput, "Name1", "base.Test2", "association1", 7,
                8, true, false, false);
        assertEquals(3, policyCmptTypeParameterInput.getTestPolicyCmptTypeParamChilds().length);
        assertEquals(1, policyCmptTypeParameterInput.getTestAttributes().length);
        assertTargetTestPolicyCmptTypeParameter(policyCmptTypeParameterInput, "Name1", "base.Test2", "association1", 7,
                8, true, false, false);
        // child type not specified therfor combinned is the default type
        assertTargetTestPolicyCmptTypeParameter(
                policyCmptTypeParameterInput.getTestPolicyCmptTypeParamChild("childpolicyCmptType1"),
                "childpolicyCmptType1", "base.Test4", "association1", 7, 8, true, true, true);
        assertTrue(policyCmptTypeParameterInput.isRequiresProductCmpt());
    }

    private void assertTargetTestPolicyCmptTypeParameter(ITestPolicyCmptTypeParameter targetChild,
            String name,
            String policyCmptTypeName,
            String associationName,
            int min,
            int max,
            boolean isInput,
            boolean isExpected,
            boolean isCombined) {

        assertNotNull(targetChild);
        assertEquals(name, targetChild.getName());
        assertEquals(policyCmptTypeName, targetChild.getPolicyCmptType());
        assertEquals(associationName, targetChild.getAssociation());
        assertEquals(min, targetChild.getMinInstances());
        assertEquals(max, targetChild.getMaxInstances());
        assertEquals(isInput, targetChild.isInputOrCombinedParameter());
        assertEquals(isExpected, targetChild.isExpextedResultOrCombinedParameter());
        assertEquals(isCombined, targetChild.isCombinedParameter());
    }

    @Test
    public void testRemoveTestAttribute() throws CoreException {
        ITestAttribute testAttribute1 = policyCmptTypeParameterInput.newInputTestAttribute();
        ITestAttribute testAttribute2 = policyCmptTypeParameterInput.newInputTestAttribute();
        ITestAttribute testAttribute3 = policyCmptTypeParameterInput.newExpectedResultTestAttribute();
        assertEquals(3, policyCmptTypeParameterInput.getTestAttributes().length);
        testAttribute2.delete();
        assertEquals(2, policyCmptTypeParameterInput.getTestAttributes().length);
        testAttribute1.delete();
        testAttribute3.delete();
        assertEquals(0, policyCmptTypeParameterInput.getTestAttributes().length);
    }

    @Test
    public void testFindAssociationTest() throws Exception {
        IPolicyCmptType policyCmptTypeSuper = newPolicyCmptType(project, "policyCmptSuper");
        IPolicyCmptTypeAssociation rel1 = policyCmptTypeSuper.newPolicyCmptTypeAssociation();
        rel1.setTargetRoleSingular("association1");
        IPolicyCmptTypeAssociation rel2 = policyCmptTypeSuper.newPolicyCmptTypeAssociation();
        rel2.setTargetRoleSingular("association2");
        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmpt");
        IPolicyCmptTypeAssociation rel3 = policyCmptType.newPolicyCmptTypeAssociation();
        rel3.setTargetRoleSingular("association3");
        IPolicyCmptTypeAssociation rel4 = policyCmptType.newPolicyCmptTypeAssociation();
        rel4.setTargetRoleSingular("association4");
        policyCmptType.setSupertype(policyCmptTypeSuper.getQualifiedName());

        ITestPolicyCmptTypeParameter child = policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();
        policyCmptTypeParameterInput.setPolicyCmptType("policyCmpt");

        child.setAssociation("association1");
        assertEquals(rel1, child.findAssociation(project));
        child.setAssociation("association2");
        assertEquals(rel2, child.findAssociation(project));
        child.setAssociation("association3");
        assertEquals(rel3, child.findAssociation(project));
        child.setAssociation("association4");
        assertEquals(rel4, child.findAssociation(project));
    }

    @Test
    public void testValidationWrongPolicyCmptTypeOfAssociation() throws CoreException {
        IPolicyCmptType targetPolicyCmptTypeSuperSuper = newPolicyCmptType(project, "targetPolicyCmptSuperSuper");
        IPolicyCmptType targetPolicyCmptTypeSuper = newPolicyCmptType(project, "targetPolicyCmptSuper");
        IPolicyCmptType targetPolicyCmptType = newPolicyCmptType(project, "targetPolicyCmpt");
        targetPolicyCmptTypeSuper.setSupertype(targetPolicyCmptTypeSuperSuper.getQualifiedName());
        targetPolicyCmptType.setSupertype(targetPolicyCmptTypeSuper.getQualifiedName());

        IPolicyCmptType sourcePolicyCmptType = newPolicyCmptType(project, "sourcePolicyCmpt");
        IPolicyCmptTypeAssociation association = sourcePolicyCmptType.newPolicyCmptTypeAssociation();
        association.setTargetRoleSingular("association");
        association.setTarget(targetPolicyCmptTypeSuperSuper.getQualifiedName());

        MessageList ml = policyCmptTypeParameterInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_WRONG_POLICY_CMPT_TYPE_OF_ASSOCIATION));

        policyCmptTypeParameterInput.setPolicyCmptType(sourcePolicyCmptType.getQualifiedName());
        ITestPolicyCmptTypeParameter child = policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();
        // no target candidate of association set, therefore msg couldn't be throws
        child.setAssociation(association.getName());
        ml = child.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_WRONG_POLICY_CMPT_TYPE_OF_ASSOCIATION));

        child.setPolicyCmptType(targetPolicyCmptTypeSuperSuper.getQualifiedName());
        ml = child.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_WRONG_POLICY_CMPT_TYPE_OF_ASSOCIATION));

        child.setPolicyCmptType(targetPolicyCmptTypeSuper.getQualifiedName());
        ml = child.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_WRONG_POLICY_CMPT_TYPE_OF_ASSOCIATION));

        child.setPolicyCmptType(targetPolicyCmptType.getQualifiedName());
        ml = child.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_WRONG_POLICY_CMPT_TYPE_OF_ASSOCIATION));

        association.setTarget(targetPolicyCmptTypeSuper.getQualifiedName());

        child.setPolicyCmptType(targetPolicyCmptTypeSuperSuper.getQualifiedName());
        ml = child.validate(project);
        // wrong target of association set
        // association specifies super but as possible target policy cmpt type super super is set
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_WRONG_POLICY_CMPT_TYPE_OF_ASSOCIATION));

        child.setPolicyCmptType(targetPolicyCmptType.getQualifiedName());
        ml = child.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_WRONG_POLICY_CMPT_TYPE_OF_ASSOCIATION));
    }

    @Test
    public void testValidatePolicyCmptTypeNotExists() throws Exception {
        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmptSuper");
        policyCmptTypeParameterInput.setPolicyCmptType(policyCmptType.getQualifiedName());
        MessageList ml = policyCmptTypeParameterInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS));

        policyCmptTypeParameterInput.setPolicyCmptType("x");
        ml = policyCmptTypeParameterInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS));
    }

    @Test
    public void testValidateWrongCountOfInstances() throws Exception {
        policyCmptTypeParameterInput.setMinInstances(0);
        policyCmptTypeParameterInput.setMaxInstances(1);
        MessageList ml = policyCmptTypeParameterInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_MIN_INSTANCES_IS_GREATER_THAN_MAX));
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_MAX_INSTANCES_IS_LESS_THAN_MIN));

        policyCmptTypeParameterInput.setMinInstances(2);
        policyCmptTypeParameterInput.setMaxInstances(1);
        ml = policyCmptTypeParameterInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_MIN_INSTANCES_IS_GREATER_THAN_MAX));
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_MAX_INSTANCES_IS_LESS_THAN_MIN));
    }

    @Test
    public void testValidateTypeDoesNotMatchParentType() throws Exception {
        policyCmptTypeParameterInput.setTestParameterType(TestParameterType.INPUT);
        ITestPolicyCmptTypeParameter paramChild = policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();
        paramChild.setTestParameterType(TestParameterType.INPUT);
        MessageList ml = policyCmptTypeParameterInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE));

        paramChild.setTestParameterType(TestParameterType.EXPECTED_RESULT);
        ml = policyCmptTypeParameterInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE));

        policyCmptTypeParameterInput.setTestParameterType(TestParameterType.EXPECTED_RESULT);
        ml = policyCmptTypeParameterInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE));

        paramChild.setTestParameterType(TestParameterType.INPUT);
        ml = policyCmptTypeParameterInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE));

        policyCmptTypeParameterInput.setTestParameterType(TestParameterType.COMBINED);
        paramChild.setTestParameterType(TestParameterType.EXPECTED_RESULT);
        ml = policyCmptTypeParameterInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE));

        paramChild.setTestParameterType(TestParameterType.INPUT);
        ml = policyCmptTypeParameterInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE));
    }

    @Test
    public void testValidateAssociationNotExists() throws Exception {
        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmpt");
        IPolicyCmptTypeAssociation rel1 = policyCmptType.newPolicyCmptTypeAssociation();
        rel1.setTargetRoleSingular("association1");

        policyCmptTypeParameterInput.setPolicyCmptType(policyCmptType.getQualifiedName());
        ITestPolicyCmptTypeParameter paramChild = policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();
        paramChild.setAssociation(rel1.getName());

        MessageList ml = policyCmptTypeParameterInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_ASSOCIATION_NOT_EXISTS));

        paramChild.setAssociation("x");
        ml = policyCmptTypeParameterInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_ASSOCIATION_NOT_EXISTS));
    }

    @Test
    public void testValidateTargetOfAssociationNotExists() throws Exception {
        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmpt");
        IPolicyCmptType policyCmptTypeTarget = newPolicyCmptType(project, "policyCmptTarget");
        IPolicyCmptTypeAssociation rel1 = policyCmptType.newPolicyCmptTypeAssociation();
        rel1.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        rel1.setTargetRoleSingular("association1");
        rel1.setTarget(policyCmptTypeTarget.getQualifiedName());

        policyCmptTypeParameterInput.setPolicyCmptType(policyCmptType.getQualifiedName());
        ITestPolicyCmptTypeParameter paramChild = policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();
        paramChild.setAssociation(rel1.getName());
        paramChild.setPolicyCmptType(policyCmptTypeTarget.getQualifiedName());

        MessageList ml = policyCmptTypeParameterInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_TARGET_OF_ASSOCIATION_NOT_EXISTS));

        rel1.setTarget("x");
        ml = policyCmptTypeParameterInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_TARGET_OF_ASSOCIATION_NOT_EXISTS));
    }

    @Test
    public void testValidateMustRequireProdIfRootAndAbstract() throws Exception {
        MessageList ml = policyCmptTypeParameterInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_MUST_REQUIRE_PROD_IF_ROOT_AND_ABSTRACT));

        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmpt");
        policyCmptTypeParameterInput.setPolicyCmptType(policyCmptType.getQualifiedName());

        policyCmptType.setAbstract(false);
        policyCmptTypeParameterInput.setRequiresProductCmpt(true);
        ml = policyCmptTypeParameterInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_MUST_REQUIRE_PROD_IF_ROOT_AND_ABSTRACT));

        policyCmptType.setAbstract(true);
        policyCmptTypeParameterInput.setRequiresProductCmpt(false);
        ml = policyCmptTypeParameterInput.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_MUST_REQUIRE_PROD_IF_ROOT_AND_ABSTRACT));

        policyCmptTypeParameterInput.setRequiresProductCmpt(true);
        ml = policyCmptTypeParameterInput.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_MUST_REQUIRE_PROD_IF_ROOT_AND_ABSTRACT));
    }

    @Test
    public void testValidateRequiresProdButPolicyCmptTypeIsNotConfByProd() throws Exception {
        MessageList ml = policyCmptTypeParameterInput.validate(project);
        assertNull(ml
                .getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_REQUIRES_PROD_BUT_POLICY_CMPT_TYPE_IS_NOT_CONF_BY_PROD));

        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmpt");
        policyCmptTypeParameterInput.setPolicyCmptType(policyCmptType.getQualifiedName());

        policyCmptType.setConfigurableByProductCmptType(false);
        policyCmptTypeParameterInput.setRequiresProductCmpt(true);
        ml = policyCmptTypeParameterInput.validate(project);
        assertNotNull(ml
                .getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_REQUIRES_PROD_BUT_POLICY_CMPT_TYPE_IS_NOT_CONF_BY_PROD));

        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptTypeParameterInput.setRequiresProductCmpt(false);
        ml = policyCmptTypeParameterInput.validate(project);
        assertNull(ml
                .getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_REQUIRES_PROD_BUT_POLICY_CMPT_TYPE_IS_NOT_CONF_BY_PROD));

        policyCmptType.setConfigurableByProductCmptType(false);
        policyCmptTypeParameterInput.setRequiresProductCmpt(false);
        ml = policyCmptTypeParameterInput.validate(project);
        assertNull(ml
                .getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_REQUIRES_PROD_BUT_POLICY_CMPT_TYPE_IS_NOT_CONF_BY_PROD));
    }

    @Test
    public void testValidateTargetOfAssociationNotExistsInTestCaseType() throws Exception {
        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmpt");
        IPolicyCmptType policyCmptTypeTarget = newPolicyCmptType(project, "policyCmptTarget");
        IPolicyCmptTypeAssociation rel1 = policyCmptType.newPolicyCmptTypeAssociation();
        rel1.setAssociationType(AssociationType.ASSOCIATION);
        rel1.setTargetRoleSingular("association1");
        rel1.setTarget(policyCmptTypeTarget.getQualifiedName());

        policyCmptTypeParameterInput.setPolicyCmptType(policyCmptType.getQualifiedName());
        ITestPolicyCmptTypeParameter paramChild = policyCmptTypeParameterInput.newTestPolicyCmptTypeParamChild();
        paramChild.setAssociation(rel1.getName());
        paramChild.setPolicyCmptType(policyCmptTypeTarget.getQualifiedName());
        paramChild.setTestParameterType(TestParameterType.INPUT);

        MessageList ml = policyCmptTypeParameterInput.validate(project);
        assertNotNull(ml
                .getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_TARGET_OF_ASSOCIATION_NOT_EXISTS_IN_TESTCASETYPE));
        assertEquals(ml.getFirstMessage(Message.WARNING).getCode(),
                ITestPolicyCmptTypeParameter.MSGCODE_TARGET_OF_ASSOCIATION_NOT_EXISTS_IN_TESTCASETYPE);

        ITestPolicyCmptTypeParameter targetOfAss = testCaseType.newInputTestPolicyCmptTypeParameter();
        paramChild.setTestParameterType(TestParameterType.INPUT);
        targetOfAss.setTestParameterType(TestParameterType.COMBINED);
        targetOfAss.setName("xyz");
        targetOfAss.setPolicyCmptType(policyCmptTypeTarget.getQualifiedName());
        ml = policyCmptTypeParameterInput.validate(project);
        assertNull(ml
                .getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_TARGET_OF_ASSOCIATION_NOT_EXISTS_IN_TESTCASETYPE));

        paramChild.setTestParameterType(TestParameterType.EXPECTED_RESULT);
        targetOfAss.setTestParameterType(TestParameterType.COMBINED);
        ml = policyCmptTypeParameterInput.validate(project);
        assertNull(ml
                .getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_TARGET_OF_ASSOCIATION_NOT_EXISTS_IN_TESTCASETYPE));

        paramChild.setTestParameterType(TestParameterType.EXPECTED_RESULT);
        targetOfAss.setTestParameterType(TestParameterType.INPUT);
        ml = policyCmptTypeParameterInput.validate(project);
        assertNotNull(ml
                .getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_TARGET_OF_ASSOCIATION_NOT_EXISTS_IN_TESTCASETYPE));

        paramChild.setTestParameterType(TestParameterType.INPUT);
        targetOfAss.setTestParameterType(TestParameterType.EXPECTED_RESULT);
        ml = policyCmptTypeParameterInput.validate(project);
        assertNotNull(ml
                .getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_TARGET_OF_ASSOCIATION_NOT_EXISTS_IN_TESTCASETYPE));

        paramChild.setTestParameterType(TestParameterType.COMBINED);
        targetOfAss.setTestParameterType(TestParameterType.EXPECTED_RESULT);
        ml = policyCmptTypeParameterInput.validate(project);
        assertNotNull(ml
                .getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_TARGET_OF_ASSOCIATION_NOT_EXISTS_IN_TESTCASETYPE));

        paramChild.setTestParameterType(TestParameterType.COMBINED);
        targetOfAss.setTestParameterType(TestParameterType.INPUT);
        ml = policyCmptTypeParameterInput.validate(project);
        assertNotNull(ml
                .getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_TARGET_OF_ASSOCIATION_NOT_EXISTS_IN_TESTCASETYPE));
    }

    @Test
    public void testGetAllowedProductCmptDependingTarget() throws CoreException {
        // two testPolicyCmptTypeParameter uses same associations (base class)
        // but different targets (subclass of base class).
        // This test case type make sense if the subclasses defines different
        // attributes which should be used in the test case
        TestContent testContent = new TestContent();
        testContent.init(project);

        // create second child test parameter (represents the association with a subset of allowed
        // products)
        // because subCoverage is specified as target
        ITestPolicyCmptTypeParameter childParameter2 = testContent.parameter.newTestPolicyCmptTypeParamChild();
        childParameter2.setPolicyCmptType(testContent.subCoverage.getQualifiedName());
        childParameter2.setAssociation(testContent.coverages.getName());
        childParameter2.setName("SubCoverageParam");

        // create the three links on the product cmpt
        IProductCmptGeneration generation = (IProductCmptGeneration)testContent.policyProduct
                .newGeneration(new GregorianCalendar());
        IProductCmptLink productCmptAssociationA = generation.newLink("Coverage");
        productCmptAssociationA.setTarget(testContent.coverageProductA.getQualifiedName());
        IProductCmptLink productCmptAssociationB = generation.newLink("Coverage");
        productCmptAssociationB.setTarget(testContent.coverageProductB.getQualifiedName());
        IProductCmptLink productCmptAssociationSub = generation.newLink("Coverage");
        productCmptAssociationSub.setTarget(testContent.subCoverageProduct.getQualifiedName());

        // assert that all product cmpt specified in the product cmpt are allowed for first test
        // param (association)
        IIpsSrcFile[] allowedProductCmpt = testContent.childParameter.getAllowedProductCmpt(project,
                testContent.policyProduct);
        assertEquals(3, allowedProductCmpt.length);

        // assert that only the subtype product cmpt is allowed for the second test param
        // (association)
        allowedProductCmpt = childParameter2.getAllowedProductCmpt(project, testContent.policyProduct);
        assertEquals(1, allowedProductCmpt.length);
        assertEquals(testContent.subCoverageProduct.getIpsSrcFile(), allowedProductCmpt[0]);

        // previous test again but now
        // remove the policy cmpt type from the product cmpt type to assert that the
        // product is not allowed, because no policy cmpt type is configured
        IProductCmptType subCoverageProductCmptType = testContent.subCoverageProduct.findProductCmptType(project);
        subCoverageProductCmptType.setPolicyCmptType(null);
        subCoverageProductCmptType.setConfigurationForPolicyCmptType(false);
        allowedProductCmpt = childParameter2.getAllowedProductCmpt(project, testContent.policyProduct);
        assertEquals(0, allowedProductCmpt.length);
    }

    @Test
    public void testGetAllowedProductCmpt() throws CoreException {
        TestContent testContent = new TestContent();
        testContent.init(project);

        // no association defined
        // => root 1, child no result
        IIpsSrcFile[] allowedProductCmpt = testContent.parameter.getAllowedProductCmpt(project,
                testContent.policyProduct);
        assertEquals(1, allowedProductCmpt.length);
        assertEquals(testContent.policyProduct.getIpsSrcFile(), allowedProductCmpt[0]);
        allowedProductCmpt = testContent.childParameter.getAllowedProductCmpt(project, testContent.policyProduct);
        assertEquals(0, allowedProductCmpt.length);

        // one association defined, but without target
        // => child no result
        IProductCmptGeneration generation = (IProductCmptGeneration)testContent.policyProduct
                .newGeneration(new GregorianCalendar());
        IProductCmptLink productCmptAssociation = generation.newLink("Coverage");
        allowedProductCmpt = testContent.childParameter.getAllowedProductCmpt(project, testContent.policyProduct);
        assertEquals(0, allowedProductCmpt.length);

        // one association with target
        productCmptAssociation.setTarget(testContent.coverageProductA.getQualifiedName());
        allowedProductCmpt = testContent.childParameter.getAllowedProductCmpt(project, testContent.policyProduct);

        assertEquals(1, allowedProductCmpt.length);
        assertEquals(testContent.coverageProductA.getIpsSrcFile(), allowedProductCmpt[0]);

        // association exists twice
        // find product cmpt only once
        generation = (IProductCmptGeneration)testContent.policyProduct.newGeneration(new GregorianCalendar());
        productCmptAssociation = generation.newLink("Coverage");
        productCmptAssociation.setTarget(testContent.coverageProductA.getQualifiedName());
        allowedProductCmpt = testContent.childParameter.getAllowedProductCmpt(project, testContent.policyProduct);
        assertEquals(1, allowedProductCmpt.length);
        assertEquals(testContent.coverageProductA.getIpsSrcFile(), allowedProductCmpt[0]);

        // if no parent product cmpt is given, return all product cmpt which matches the
        // association of the parameter
        allowedProductCmpt = testContent.childParameter.getAllowedProductCmpt(project, null);
        assertEquals(3, allowedProductCmpt.length);
        asserContains(allowedProductCmpt, testContent.coverageProductA);
        asserContains(allowedProductCmpt, testContent.coverageProductB);

        // test with two generations
        // coverageProductA specified in generation 1
        // coverageProductB specified in generation 2
        generation = (IProductCmptGeneration)testContent.policyProduct.newGeneration(new GregorianCalendar());
        productCmptAssociation = generation.newLink("Coverage");
        productCmptAssociation.setTarget(testContent.coverageProductB.getQualifiedName());
        allowedProductCmpt = testContent.childParameter.getAllowedProductCmpt(project, testContent.policyProduct);
        assertEquals(2, allowedProductCmpt.length);
        asserContains(allowedProductCmpt, testContent.coverageProductA);
        asserContains(allowedProductCmpt, testContent.coverageProductB);
    }

    private void asserContains(IIpsSrcFile[] allowedProductCmpt, IProductCmpt productCmpt) {
        List<IIpsSrcFile> list = Arrays.asList(allowedProductCmpt);
        assertTrue(list.contains(productCmpt.getIpsSrcFile()));
    }

    private class TestContent {
        private IPolicyCmptType policy;
        private IPolicyCmptTypeAssociation coverages;
        private IPolicyCmptType coverage;
        private IPolicyCmptType subCoverage;

        private IProductCmpt policyProduct;
        private IProductCmpt coverageProductA;
        private IProductCmpt coverageProductB;
        private IProductCmpt subCoverageProduct;

        private ITestPolicyCmptTypeParameter parameter;
        private ITestPolicyCmptTypeParameter childParameter;

        public void init(IIpsProject project) throws CoreException {
            policy = newPolicyCmptType(project, "Policy");
            policy.setConfigurableByProductCmptType(true);
            ProductCmptType productCmptTypePolicy = newProductCmptType(project, "PolicyType");
            policy.setProductCmptType(productCmptTypePolicy.getQualifiedName());
            productCmptTypePolicy.setPolicyCmptType(policy.getQualifiedName());

            coverage = newPolicyCmptType(project, "Coverage");
            coverage.setConfigurableByProductCmptType(true);
            ProductCmptType productCmptTypeCoverage = newProductCmptType(project, "CoverageType");
            coverage.setProductCmptType(productCmptTypeCoverage.getQualifiedName());
            productCmptTypeCoverage.setPolicyCmptType(coverage.getQualifiedName());

            subCoverage = newPolicyCmptType(project, "SubCoverage");
            coverage.setConfigurableByProductCmptType(true);
            ProductCmptType productCmptTypeSubCoverage = newProductCmptType(project, "SubCoverageType");
            subCoverage.setProductCmptType(productCmptTypeSubCoverage.getQualifiedName());
            productCmptTypeSubCoverage.setPolicyCmptType(subCoverage.getQualifiedName());
            subCoverage.setSupertype(coverage.getQualifiedName());
            productCmptTypeSubCoverage.setSupertype(productCmptTypeCoverage.getQualifiedName());

            coverages = policy.newPolicyCmptTypeAssociation();
            coverages.setTarget(coverage.getQualifiedName());
            coverages.setTargetRoleSingular("Coverage");
            coverages.setTargetRolePlural("Coverages");
            coverages.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

            IProductCmptTypeAssociation association = productCmptTypePolicy.newProductCmptTypeAssociation();
            association.setTarget(productCmptTypeCoverage.getQualifiedName());
            association.setTargetRoleSingular("Coverage");
            association.setTargetRolePlural("Coverages");

            // create products
            policyProduct = newProductCmpt(project, "PolicyA 2007-09");
            policyProduct.setProductCmptType(productCmptTypePolicy.getQualifiedName());
            coverageProductA = newProductCmpt(project, "CoverageA 2007-09");
            coverageProductA.setProductCmptType(productCmptTypeCoverage.getQualifiedName());
            coverageProductA.newGeneration(new GregorianCalendar());
            coverageProductB = newProductCmpt(project, "CoverageB 2007-09");
            coverageProductB.setProductCmptType(productCmptTypeCoverage.getQualifiedName());
            coverageProductB.newGeneration(new GregorianCalendar());
            subCoverageProduct = newProductCmpt(project, "SubCoverage 2008-02");
            subCoverageProduct.setProductCmptType(productCmptTypeSubCoverage.getQualifiedName());
            subCoverageProduct.newGeneration(new GregorianCalendar());

            parameter = testCaseType.newCombinedPolicyCmptTypeParameter();
            parameter.setPolicyCmptType(policy.getQualifiedName());
            parameter.setName("PolicyParam");
            childParameter = parameter.newTestPolicyCmptTypeParamChild();
            childParameter.setPolicyCmptType(coverage.getQualifiedName());
            childParameter.setAssociation(coverages.getName());
            childParameter.setName("CoverageParam");
        }
    }
}
