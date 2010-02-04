/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.core.model.testcase.TestCaseHierarchyPath;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestCaseAndTestCaseTypeTest extends AbstractIpsPluginTest {

    private IIpsProject project;

    private ITestCase testCase;
    private ITestCaseType testCaseType;

    private String pathToTestPolicyCmptInput;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        project = super.newIpsProject("TestProject");
        testCase = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "TestCase1");
        testCaseType = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "TestCaseType1");
        testCase.setTestCaseType(testCaseType.getQualifiedName());

        IPolicyCmptType pctContract = newPolicyCmptType(project, "Contract");
        IPolicyCmptType pctCoverage = newPolicyCmptType(project, "Coverage");

        IPolicyCmptTypeAssociation association = pctContract.newPolicyCmptTypeAssociation();
        association.setTargetRoleSingular("Coverage");
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setTarget(pctCoverage.getQualifiedName());

        // create test case type side
        ITestPolicyCmptTypeParameter tp = testCaseType.newInputTestPolicyCmptTypeParameter();
        tp.setName("inputTestPolicyCmptTypeParam1");
        tp.setPolicyCmptType(pctContract.getQualifiedName());
        tp.setName(StringUtil.unqualifiedName(pctContract.getName()));
        ITestPolicyCmptTypeParameter tpChild = tp.newTestPolicyCmptTypeParamChild();
        tpChild.setAssociation(association.getName());
        tpChild.setName(association.getName());
        testCaseType.newInputTestValueParameter().setName("inputValueParameter1");

        testCaseType.newExpectedResultPolicyCmptTypeParameter().setName("expectedResultTestPolicyCmptTypeParam1");
        testCaseType.newExpectedResultValueParameter().setName("expectedResultValueParameter1");
        testCaseType.newExpectedResultValueParameter().setName("expectedResultValueParameter2");
        testCaseType.newExpectedResultValueParameter().setName("expectedResultValueParameter3");
        testCaseType.newExpectedResultValueParameter().setName("expectedResultValueParameter4");

        // create test case side
        ITestPolicyCmpt pc = testCase.newTestPolicyCmpt();
        pc.setTestPolicyCmptTypeParameter("inputTestPolicyCmptTypeParam1");
        pc.setTestPolicyCmptTypeParameter(tp.getName());
        pc.setName(tp.getName());
        ITestPolicyCmptLink pcr = pc.addTestPcTypeLink(tpChild, "", "", "");
        ITestPolicyCmpt pcChild = pcr.findTarget();
        pathToTestPolicyCmptInput = new TestCaseHierarchyPath(pcChild).getHierarchyPath();
        testCase.newTestValue().setTestValueParameter("inputValueParameter1");

        testCase.newTestPolicyCmpt().setTestPolicyCmptTypeParameter("expectedResultTestPolicyCmptTypeParam1");

        testCase.newTestValue().setTestValueParameter("expectedResultValueParameter1");
        testCase.newTestValue().setTestValueParameter("expectedResultValueParameter2");
        testCase.newTestValue().setTestValueParameter("expectedResultValueParameter3");
        testCase.newTestValue().setTestValueParameter("expectedResultValueParameter4");
    }

    public void testContentProvider() throws CoreException {
        TestCaseContentProvider testCaseCntProviderIn = new TestCaseContentProvider(TestCaseContentProvider.INPUT,
                testCase);
        TestCaseContentProvider testCaseContentProviderExp = new TestCaseContentProvider(
                TestCaseContentProvider.EXPECTED_RESULT, testCase);

        assertEquals(2, testCaseCntProviderIn.getElements(testCase).length);
        assertEquals(5, testCaseContentProviderExp.getElements(testCase).length);
    }

    public void testValidateTestPolicyCmptAssociation() throws CoreException {
        ITestPolicyCmpt pc = testCase.findTestPolicyCmpt(pathToTestPolicyCmptInput);
        ITestPolicyCmptLink pcr = (ITestPolicyCmptLink)pc.getParent();
        MessageList ml = pcr.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptTypeParameter.MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS));

        ITestPolicyCmptTypeParameter param = pcr.findTestPolicyCmptTypeParameter(project);
        ITestPolicyCmpt pcParent = (ITestPolicyCmpt)pcr.getParent();
        param.setMinInstances(2);
        param.setMaxInstances(3);
        ml = pcParent.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MIN_INSTANCES_NOT_REACHED));

        ITestPolicyCmpt parent = (ITestPolicyCmpt)pcr.getParent();
        parent.addTestPcTypeLink(param, "", "", "");
        parent.addTestPcTypeLink(param, "", "", "");
        parent.addTestPcTypeLink(param, "", "", "");
        ml = pcParent.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_MAX_INSTANCES_REACHED));

        String prevAssociation = param.getAssociation();
        param.setAssociation("none");
        ml = pcr.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptLink.MSGCODE_MODEL_LINK_NOT_FOUND));

        param.setAssociation(prevAssociation);

        pcr.setTestPolicyCmptTypeParameter("none");
        ml = pcr.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptLink.MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND));
    }
}
