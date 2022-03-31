/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.testcase;

import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.lacksMessageCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestPcTypeLinkTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private ITestCase testCase;
    private ITestPolicyCmptLink testPcTypeAssociation;

    @Override
    @Before
    public void setUp() throws Exception {
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

    @Test
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getFirstElement(docEl);
        testPcTypeAssociation.initFromXml(paramEl);
        assertEquals("association1", testPcTypeAssociation.getTestPolicyCmptTypeParameter());
        assertEquals("base.target1", testPcTypeAssociation.getTarget());
    }

    @Test
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

    @Test
    public void testValidateTestCaseTypeParamNotFound() throws Exception {
        MessageList ml = testPcTypeAssociation.validate(project);
        assertThat(ml, lacksMessageCode(ITestPolicyCmptLink.MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND));

        testPcTypeAssociation.setTestPolicyCmptTypeParameter("x");
        ml = testPcTypeAssociation.validate(project);
        assertThat(ml, hasMessageCode(ITestPolicyCmptLink.MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND));
    }

    @Test
    public void testValidateAssoziationTargetNotInTestCase() throws Exception {
        testCase.newTestPolicyCmpt().setName("testPolicyCmptTarget");

        MessageList ml = testPcTypeAssociation.validate(project);
        assertThat(ml, lacksMessageCode(ITestPolicyCmptLink.MSGCODE_ASSOZIATION_TARGET_NOT_IN_TEST_CASE));

        testPcTypeAssociation.setTarget("x");
        ml = testPcTypeAssociation.validate(project);
        assertThat(ml, hasMessageCode(ITestPolicyCmptLink.MSGCODE_ASSOZIATION_TARGET_NOT_IN_TEST_CASE));
    }

    @Test
    public void testValidateModelAssociationNotFound() throws Exception {
        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmptType");
        policyCmptType.newPolicyCmptTypeAssociation().setTargetRoleSingular("modelAssociation");
        ITestPolicyCmptTypeParameter param = ((ITestPolicyCmpt)testPcTypeAssociation.getParent())
                .findTestPolicyCmptTypeParameter(project);
        param.setPolicyCmptType("policyCmptType");
        ITestPolicyCmptTypeParameter paramChild = testPcTypeAssociation.findTestPolicyCmptTypeParameter(project);
        paramChild.setAssociation("modelAssociation");

        MessageList ml = testPcTypeAssociation.validate(project);
        assertThat(ml, lacksMessageCode(ITestPolicyCmptLink.MSGCODE_MODEL_LINK_NOT_FOUND));

        paramChild.setAssociation("x");
        ml = testPcTypeAssociation.validate(project);
        assertThat(ml, hasMessageCode(ITestPolicyCmptLink.MSGCODE_MODEL_LINK_NOT_FOUND));
    }
}
