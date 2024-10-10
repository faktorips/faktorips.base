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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.StringUtil;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 *
 * @author Joerg Ortmann
 */
public class TestPolicyCmptLinkTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private ITestCase testCase;
    private ITestPolicyCmptLink testPolicyCmptLink;

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
        testPolicyCmptLink = tpc.newTestPolicyCmptLink();
        testPolicyCmptLink.setTestPolicyCmptTypeParameter("childParam");
    }

    @Test
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getFirstElement(docEl);
        testPolicyCmptLink.initFromXml(paramEl);
        assertEquals("association1", testPolicyCmptLink.getTestPolicyCmptTypeParameter());
        assertEquals("base.target1", testPolicyCmptLink.getTarget());
    }

    @Test
    public void testToXml() {
        testPolicyCmptLink.setTestPolicyCmptTypeParameter("association2");
        testPolicyCmptLink.setTarget("base.target2");
        Element el = testPolicyCmptLink.toXml(newDocument());
        testPolicyCmptLink.setTestPolicyCmptTypeParameter("test1");
        testPolicyCmptLink.setTarget("test2");
        testPolicyCmptLink.initFromXml(el);
        assertEquals("association2", testPolicyCmptLink.getTestPolicyCmptTypeParameter());
        assertEquals("base.target2", testPolicyCmptLink.getTarget());
    }

    @Test
    public void testDirectChangesToTheCorrespondingFile_TestPolicyCmptLink() throws Exception {
        IIpsSrcFile ipsFile = testCase.getIpsSrcFile();
        ITestPolicyCmpt testPolicyCmptChild = testPolicyCmptLink.newTargetTestPolicyCmptChild();
        testPolicyCmptChild.setProductCmpt("targetProduct");
        ITestAttributeValue testAttributeValue = testPolicyCmptChild.newTestAttributeValue();
        testAttributeValue.setTestAttribute("testAttr");
        testAttributeValue.setValue("foo");
        ITestPolicyCmpt testPolicyCmpt = testCase.getTestPolicyCmpts()[0];
        ipsFile.save(null);
        String encoding = testCase.getIpsProject().getXmlFileCharset();
        AFile file = ipsFile.getCorrespondingFile();
        String content = StringUtil.readFromInputStream(file.getContents(), encoding);
        content = content.replace("foo", "bar");
        file.setContents(StringUtil.getInputStreamForString(content, encoding), false, null);

        testCase = (ITestCase)ipsFile.getIpsObject(); // forces a reload

        assertThat(testCase.getTestPolicyCmpts()[0], is(sameInstance(testPolicyCmpt)));
        assertThat(testCase.getTestPolicyCmpts()[0].getTestPolicyCmptLinks()[0],
                is(sameInstance(testPolicyCmptLink)));
        assertThat(testPolicyCmptLink.findTarget().getProductCmpt(), is("targetProduct"));
        assertThat(testPolicyCmptLink.findTarget().getTestAttributeValue("testAttr").getValue(), is("bar"));
    }

    @Test
    public void testDirectChangesToTheCorrespondingFile_MultipleTestPolicyCmptLinks() throws Exception {
        IIpsSrcFile ipsFile = testCase.getIpsSrcFile();
        ITestPolicyCmpt testPolicyCmptChild = testPolicyCmptLink.newTargetTestPolicyCmptChild();
        testPolicyCmptChild.setProductCmpt("targetProduct");
        ITestAttributeValue testAttributeValue = testPolicyCmptChild.newTestAttributeValue();
        testAttributeValue.setTestAttribute("testAttr");
        testAttributeValue.setValue("foo");
        ITestPolicyCmpt testPolicyCmpt = testCase.getTestPolicyCmpts()[0];
        ITestPolicyCmptLink testPolicyCmptLink2 = testPolicyCmpt.newTestPolicyCmptLink();
        testPolicyCmptLink2.setTestPolicyCmptTypeParameter("childParam");
        ITestPolicyCmpt testPolicyCmptChild2 = testPolicyCmptLink2.newTargetTestPolicyCmptChild();
        testPolicyCmptChild2.setProductCmpt("targetProduct2");
        ITestAttributeValue testAttributeValue2 = testPolicyCmptChild2.newTestAttributeValue();
        testAttributeValue2.setTestAttribute("testAttr");
        testAttributeValue2.setValue("foo2");
        ipsFile.save(null);
        String encoding = testCase.getIpsProject().getXmlFileCharset();
        AFile file = ipsFile.getCorrespondingFile();
        String content = StringUtil.readFromInputStream(file.getContents(), encoding);
        content = content.replace("foo", "bar");
        file.setContents(StringUtil.getInputStreamForString(content, encoding), false, null);

        testCase = (ITestCase)ipsFile.getIpsObject(); // forces a reload

        assertThat(testCase.getTestPolicyCmpts()[0], is(sameInstance(testPolicyCmpt)));
        assertThat(testCase.getTestPolicyCmpts()[0].getTestPolicyCmptLinks()[0],
                is(sameInstance(testPolicyCmptLink)));
        assertThat(testPolicyCmptLink.findTarget().getProductCmpt(), is("targetProduct"));
        assertThat(testPolicyCmptLink.findTarget().getTestAttributeValue("testAttr").getValue(), is("bar"));
        assertThat(testCase.getTestPolicyCmpts()[0].getTestPolicyCmptLinks()[1],
                is(sameInstance(testPolicyCmptLink2)));
        assertThat(testPolicyCmptLink2.findTarget().getProductCmpt(), is("targetProduct2"));
        assertThat(testPolicyCmptLink2.findTarget().getTestAttributeValue("testAttr").getValue(), is("bar2"));
    }

    @Test
    public void testValidateTestCaseTypeParamNotFound() throws Exception {
        MessageList ml = testPolicyCmptLink.validate(project);
        assertThat(ml, lacksMessageCode(ITestPolicyCmptLink.MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND));

        testPolicyCmptLink.setTestPolicyCmptTypeParameter("x");
        ml = testPolicyCmptLink.validate(project);
        assertThat(ml, hasMessageCode(ITestPolicyCmptLink.MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND));
    }

    @Test
    public void testValidateAssoziationTargetNotInTestCase() throws Exception {
        testCase.newTestPolicyCmpt().setName("testPolicyCmptTarget");

        MessageList ml = testPolicyCmptLink.validate(project);
        assertThat(ml, lacksMessageCode(ITestPolicyCmptLink.MSGCODE_ASSOZIATION_TARGET_NOT_IN_TEST_CASE));

        testPolicyCmptLink.setTarget("x");
        ml = testPolicyCmptLink.validate(project);
        assertThat(ml, hasMessageCode(ITestPolicyCmptLink.MSGCODE_ASSOZIATION_TARGET_NOT_IN_TEST_CASE));
    }

    @Test
    public void testValidateModelAssociationNotFound() throws Exception {
        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmptType");
        policyCmptType.newPolicyCmptTypeAssociation().setTargetRoleSingular("modelAssociation");
        ITestPolicyCmptTypeParameter param = ((ITestPolicyCmpt)testPolicyCmptLink.getParent())
                .findTestPolicyCmptTypeParameter(project);
        param.setPolicyCmptType("policyCmptType");
        ITestPolicyCmptTypeParameter paramChild = testPolicyCmptLink.findTestPolicyCmptTypeParameter(project);
        paramChild.setAssociation("modelAssociation");

        MessageList ml = testPolicyCmptLink.validate(project);
        assertThat(ml, lacksMessageCode(ITestPolicyCmptLink.MSGCODE_MODEL_LINK_NOT_FOUND));

        paramChild.setAssociation("x");
        ml = testPolicyCmptLink.validate(project);
        assertThat(ml, hasMessageCode(ITestPolicyCmptLink.MSGCODE_MODEL_LINK_NOT_FOUND));
    }
}
