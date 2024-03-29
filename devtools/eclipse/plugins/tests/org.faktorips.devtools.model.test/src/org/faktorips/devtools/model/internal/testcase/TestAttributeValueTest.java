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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestAttributeValueTest extends AbstractIpsPluginTest {

    private ITestAttributeValue testAttributeValue;
    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        ITestCaseType testCaseType = (ITestCaseType)newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE,
                "PremiumCalculation");
        ITestPolicyCmptTypeParameter param1 = testCaseType.newInputTestPolicyCmptTypeParameter();
        param1.setName("inputTestPcCmptParam1");
        ITestAttribute testAttribute = param1.newInputTestAttribute();
        testAttribute.setName("inputAttribute1");
        testAttribute.setAttribute("Xyz");

        ITestCase testCase = (ITestCase)newIpsObject(ipsProject, IpsObjectType.TEST_CASE, "PremiumCalculation");
        testCase.setTestCaseType(testCaseType.getName());
        ITestPolicyCmpt tpc = testCase.newTestPolicyCmpt();
        tpc.setTestPolicyCmptTypeParameter("inputTestPcCmptParam1");
        testAttributeValue = tpc.newTestAttributeValue();
        testAttributeValue.setTestAttribute("inputAttribute1");
    }

    @Test
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getFirstElement(docEl);
        testAttributeValue.initFromXml(paramEl);
        assertEquals("attribute1", testAttributeValue.getTestAttribute());
        assertEquals("500", testAttributeValue.getValue());
    }

    @Test
    public void testToXml() {
        testAttributeValue.setTestAttribute("attribute2");
        testAttributeValue.setValue("500");
        Element el = testAttributeValue.toXml(newDocument());
        testAttributeValue.setTestAttribute("test");
        testAttributeValue.setValue("1000");
        testAttributeValue.initFromXml(el);
        assertEquals("attribute2", testAttributeValue.getTestAttribute());
        assertEquals("500", testAttributeValue.getValue());
    }

    @Test
    public void testValidateTestAttributeNotFound() throws Exception {
        MessageList ml = testAttributeValue.validate(ipsProject);
        assertThat(ml, lacksMessageCode(ITestAttributeValue.MSGCODE_TESTATTRIBUTE_NOT_FOUND));

        testAttributeValue.setTestAttribute("x"); // the value must be set, otherwise the attribute
        // will be ignored
        ml = testAttributeValue.validate(ipsProject);
        assertThat(ml, hasMessageCode(ITestAttributeValue.MSGCODE_TESTATTRIBUTE_NOT_FOUND));
    }

    @Test
    public void testValidateAttributeNotFound() throws Exception {
        IPolicyCmptType pct = newPolicyCmptType(ipsProject, "policyCmptType");
        IPolicyCmptTypeAttribute attr = pct.newPolicyCmptTypeAttribute();
        attr.setName("attribute1");
        ITestAttribute testAttribute = testAttributeValue.findTestAttribute(ipsProject);
        ITestPolicyCmptTypeParameter param = (ITestPolicyCmptTypeParameter)testAttribute.getParent();
        param.setPolicyCmptType(pct.getQualifiedName());

        testAttribute.setAttribute(attr.getName());
        MessageList ml = testAttributeValue.validate(ipsProject);
        assertThat(ml, lacksMessageCode(ITestAttribute.MSGCODE_ATTRIBUTE_NOT_FOUND));

        attr.setName("x");
        testAttributeValue.setValue("x");
        ml = testAttributeValue.validate(ipsProject);
        assertEquals(ITestAttribute.MSGCODE_ATTRIBUTE_NOT_FOUND, ml.getFirstMessage(Message.WARNING).getCode());
    }

    @Test
    public void testValidateWrongType() throws Exception {
        MessageList ml = testAttributeValue.validate(ipsProject);
        assertThat(ml, lacksMessageCode(ITestAttribute.MSGCODE_WRONG_TYPE));

        // remark the test if the message will be set couldn't be tested here because setting
        // a wrong type of the parameter is not possible without getting an argument exception
        // see TestValueParameter#setTestParameterType
    }

    /**
     * Define a base policy cmpt in the test case type and put a product cmpt based on a subclass of
     * the base policy cmpt in the test case. Assert that the find method on the test case type side
     * doesn't find an attribute of the subclass. This feature is implemented on test test case side
     * see TestPolicyCmpt.findProductCmptAttribute()
     */
    @Test
    public void testValidateAttributeInSuperType() {
        IPolicyCmptType pctSuper = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        IPolicyCmptType pct = newPolicyAndProductCmptType(ipsProject, "MotorPolicy", "MotorProduct");
        pct.setSupertype(pctSuper.getQualifiedName());
        IPolicyCmptTypeAttribute attr = pct.newPolicyCmptTypeAttribute();
        attr.setName("attribute1");

        ProductCmpt pc = newProductCmpt(pct.findProductCmptType(ipsProject), "productA");

        ITestAttribute testAttribute = testAttributeValue.findTestAttribute(ipsProject);
        ITestPolicyCmptTypeParameter param = (ITestPolicyCmptTypeParameter)testAttribute.getParent();
        param.setPolicyCmptType(pctSuper.getQualifiedName());

        testAttribute.setAttribute(attr.getName());
        ((TestPolicyCmpt)testAttributeValue.getParent()).setProductCmpt(pc.getQualifiedName());

        // assert that the attribute will not be found by searching via the test attribute
        // because the test attribute has no information about the usage of the subclass in the
        // test case
        assertNull(testAttribute.findAttribute(ipsProject));

        // check that the attribute will be found by searching via the test attribute value,
        // because the testattributes parent defines the product cmpt, wich uses a sublass, which
        // defines the attribute
        MessageList ml = testAttributeValue.validate(ipsProject);
        assertThat(ml, lacksMessageCode(ITestAttribute.MSGCODE_ATTRIBUTE_NOT_FOUND));
        assertNotNull(testAttributeValue.findAttribute(ipsProject));

        assertNotNull(((ITestPolicyCmpt)testAttributeValue.getParent()).findProductCmptTypeAttribute(
                testAttribute.getAttribute(), ipsProject));

        // negative test

        attr.setName("attribute2");
        testAttributeValue.setValue("x"); // the value must be set, otherwise the attribute will be
        // ignored
        ml = testAttributeValue.validate(ipsProject);
        assertThat(ml, hasMessageCode(ITestAttribute.MSGCODE_ATTRIBUTE_NOT_FOUND));
        assertNull(testAttributeValue.findAttribute(ipsProject));

        testAttributeValue.setValue(null); // the value is null, thus the attribute will be ignored
        ml = testAttributeValue.validate(ipsProject);
        assertThat(ml, lacksMessageCode(ITestAttribute.MSGCODE_ATTRIBUTE_NOT_FOUND));
        assertNull(testAttributeValue.findAttribute(ipsProject));

        pctSuper.newPolicyCmptTypeAttribute().setName("attributeSuper");
        testAttribute.setAttribute("attributeSuper");
        assertNotNull(testAttribute.findAttribute(ipsProject));
        assertNotNull(testAttributeValue.findAttribute(ipsProject));
    }

    @Test
    public void testFindAttribiute() {
        testAttributeValue.findAttribute(ipsProject);
    }

    @Test
    public void testTestAttributeNotBasedOnModelAttribute() {
        testAttributeValue.setValue("x");
        ITestAttribute testAttribute = testAttributeValue.findTestAttribute(ipsProject);
        MessageList ml = testAttributeValue.validate(ipsProject);
        // first check the correct test setup
        assertTrue(testAttribute.isBasedOnModelAttribute());
        assertThat(ml, hasMessageCode(ITestAttribute.MSGCODE_ATTRIBUTE_NOT_FOUND));

        testAttribute.setAttribute("");
        testAttribute.setDatatype("X");
        assertFalse(testAttribute.isBasedOnModelAttribute());
        ml = testAttributeValue.validate(ipsProject);
        assertThat(ml, lacksMessageCode(ITestAttribute.MSGCODE_ATTRIBUTE_NOT_FOUND));
        assertNotNull(ml
                .getMessageByCode(
                        IValidationMsgCodesForInvalidValues.MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_CANT_BE_FOUND));
        assertEquals(
                Message.ERROR,
                ml.getMessageByCode(
                        IValidationMsgCodesForInvalidValues.MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_CANT_BE_FOUND)
                        .getSeverity());

        testAttribute.setDatatype("String");
        assertFalse(testAttribute.isBasedOnModelAttribute());
        ml = testAttributeValue.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(
                        IValidationMsgCodesForInvalidValues.MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_CANT_BE_FOUND));
    }
}
