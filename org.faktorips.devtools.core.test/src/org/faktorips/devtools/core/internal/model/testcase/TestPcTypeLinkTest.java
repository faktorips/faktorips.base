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

package org.faktorips.devtools.core.internal.model.testcase;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestPcTypeLinkTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private ITestCase testCase;
    private ITestPolicyCmptLink testPcTypeAssociation;

    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        ITestCaseType testCaseType = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE,
                "PremiumCalculation");
        ITestPolicyCmptTypeParameter param = testCaseType.newExpectedResultPolicyCmptTypeParameter();
        param.setName("expectedResultParam");
        ITestPolicyCmptTypeParameter paramChild = param.newTestPolicyCmptTypeParamChild();
        paramChild.setName("childParam");

        testCase = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "PremiumCalculation");
        testCase.setTestCaseType(testCaseType.getName());

        ITestPolicyCmpt tpc = testCase.newTestPolicyCmpt();
        tpc.setTestPolicyCmptTypeParameter("expectedResultParam");
        testPcTypeAssociation = tpc.newTestPolicyCmptLink();
        testPcTypeAssociation.setTestPolicyCmptTypeParameter("childParam");
    }

    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getFirstElement(docEl);
        testPcTypeAssociation.initFromXml(paramEl);
        assertEquals("association1", testPcTypeAssociation.getTestPolicyCmptTypeParameter());
        assertEquals("base.target1", testPcTypeAssociation.getTarget());
    }

    public void testToXml() {
        testPcTypeAssociation.setTestPolicyCmptTypeParameter("association2");
        testPcTypeAssociation.setTarget("base.target2");
        Element el = testPcTypeAssociation.toXml(newDocument());
        testPcTypeAssociation.setTestPolicyCmptTypeParameter("test1");
        testPcTypeAssociation.setTarget("test2");
        testPcTypeAssociation.initFromXml(el);
        assertEquals("association2", testPcTypeAssociation.getTestPolicyCmptTypeParameter());
        assertEquals("base.target2", testPcTypeAssociation.getTarget());
    }

    public void testValidateTestCaseTypeParamNotFound() throws Exception {
        MessageList ml = testPcTypeAssociation.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptLink.MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND));

        testPcTypeAssociation.setTestPolicyCmptTypeParameter("x");
        ml = testPcTypeAssociation.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptLink.MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND));
    }

    public void testValidateAssoziationTargetNotInTestCase() throws Exception {
        testCase.newTestPolicyCmpt().setName("testPolicyCmptTarget");

        MessageList ml = testPcTypeAssociation.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptLink.MSGCODE_ASSOZIATION_TARGET_NOT_IN_TEST_CASE));

        testPcTypeAssociation.setTarget("x");
        ml = testPcTypeAssociation.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptLink.MSGCODE_ASSOZIATION_TARGET_NOT_IN_TEST_CASE));
    }

    public void testValidateModelAssociationNotFound() throws Exception {
        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmptType");
        policyCmptType.newPolicyCmptTypeAssociation().setTargetRoleSingular("modelAssociation");
        ITestPolicyCmptTypeParameter param = ((ITestPolicyCmpt)testPcTypeAssociation.getParent())
                .findTestPolicyCmptTypeParameter(project);
        param.setPolicyCmptType("policyCmptType");
        ITestPolicyCmptTypeParameter paramChild = testPcTypeAssociation.findTestPolicyCmptTypeParameter(project);
        paramChild.setAssociation("modelAssociation");

        MessageList ml = testPcTypeAssociation.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptLink.MSGCODE_MODEL_LINK_NOT_FOUND));

        paramChild.setAssociation("x");
        ml = testPcTypeAssociation.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptLink.MSGCODE_MODEL_LINK_NOT_FOUND));
    }
}
