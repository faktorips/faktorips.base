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

package org.faktorips.devtools.core.internal.model.testcase;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestAttributeValueTest  extends AbstractIpsPluginTest {

    private ITestAttributeValue testAttributeValue;
    private IIpsProject project;
    
    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        ITestCaseType testCaseType = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculation");
        ITestPolicyCmptTypeParameter param1 = testCaseType.newInputTestPolicyCmptTypeParameter();
        param1.setName("inputTestPcCmptParam1");
        param1.newInputTestAttribute().setName("inputAttribute1");
        
        ITestCase testCase = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "PremiumCalculation");
        testCase.setTestCaseType(testCaseType.getName());
        ITestPolicyCmpt tpc = testCase.newTestPolicyCmpt();
        tpc.setTestPolicyCmptTypeParameter("inputTestPcCmptParam1");
        testAttributeValue = tpc.newTestAttributeValue();
        testAttributeValue.setTestAttribute("inputAttribute1");
    }
    
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getFirstElement(docEl);
        testAttributeValue.initFromXml(paramEl);
        assertEquals("attribute1", testAttributeValue.getTestAttribute());
        assertEquals("500", testAttributeValue.getValue());
    }

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
    
    public void testValidateTestAttributeNotFound() throws Exception{
        MessageList ml = testAttributeValue.validate(project);
        assertNull(ml.getMessageByCode(ITestAttributeValue.MSGCODE_TESTATTRIBUTE_NOT_FOUND));

        testAttributeValue.setTestAttribute("x"); // the value must be set, otherwise the attribute will be ignored
        ml = testAttributeValue.validate(project);
        assertNotNull(ml.getMessageByCode(ITestAttributeValue.MSGCODE_TESTATTRIBUTE_NOT_FOUND));
    }

    public void testValidateAttributeNotFound() throws Exception{
        IPolicyCmptType pct = newPolicyCmptType(project, "policyCmptType");
        IPolicyCmptTypeAttribute attr = pct.newPolicyCmptTypeAttribute();
        attr.setName("attribute1");
        ITestAttribute testAttribute = testAttributeValue.findTestAttribute();
        ITestPolicyCmptTypeParameter param = (ITestPolicyCmptTypeParameter) testAttribute.getParent();
        param.setPolicyCmptType(pct.getQualifiedName());

        testAttribute.setAttribute(attr.getName());
        MessageList ml = testAttributeValue.validate(project);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_ATTRIBUTE_NOT_FOUND));

        attr.setName("x");
        testAttributeValue.setValue("x");
        ml = testAttributeValue.validate(project);
        assertEquals(ITestAttribute.MSGCODE_ATTRIBUTE_NOT_FOUND, ml.getFirstMessage(Message.WARNING).getCode());
    }
    
    public void testValidateWrongType() throws Exception{
        MessageList ml = testAttributeValue.validate(project);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_WRONG_TYPE));
        
        // remark the test if the message will be set couldn't be tested here because setting
        // a wrong type of the parameter is not possible without getting an argument exception
        // see TestValueParameter#setTestParameterType
    }
    
    /**
     * Define a base policy cmpt in the test case type and put a product cmpt based on a subclass of the base policy cmpt
     * in the test case. Assert that the find method on the test case type side doesn't find an attribute of the subclass.
     * This feature is implemented on test test case side see TestPolicyCmpt.findProductCmptAttribute()
     */
    public void testValidateAttributeInSuperType() throws CoreException{
        IPolicyCmptType pctSuper = newPolicyAndProductCmptType(project, "Policy", "Product");
        IPolicyCmptType pct = newPolicyAndProductCmptType(project, "MotorPolicy", "MotorProduct");
        pct.setSupertype(pctSuper.getQualifiedName());
        IPolicyCmptTypeAttribute attr = pct.newPolicyCmptTypeAttribute();
        attr.setName("attribute1");
        
        ProductCmpt pc = newProductCmpt(pct.findProductCmptType(project), "productA");
        
        ITestAttribute testAttribute = testAttributeValue.findTestAttribute();
        ITestPolicyCmptTypeParameter param = (ITestPolicyCmptTypeParameter) testAttribute.getParent();
        param.setPolicyCmptType(pctSuper.getQualifiedName());
        
        testAttribute.setAttribute(attr.getName());
        ((TestPolicyCmpt)testAttributeValue.getParent()).setProductCmpt(pc.getQualifiedName());
        
        // assert that the attribute will not be found by searching via the test attribute
        // because the test attribute has no information about the usage of the subclass in the 
        // test case
        assertNull(testAttribute.findAttribute());
        
        // check that the attribute will be found by searching via the test attribute value,
        // because the testattributes parent defines the product cmpt, wich uses a sublass, which
        // defines the attribute
        MessageList ml = testAttributeValue.validate(project);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_ATTRIBUTE_NOT_FOUND));
        

        assertNotNull(((ITestPolicyCmpt)testAttributeValue.getParent()).findProductCmptAttribute(testAttribute.getAttribute()));
        
        
        // negative test 
        
        attr.setName("attribute2");
        testAttributeValue.setValue("x"); // the value must be set, otherwise the attribute will be ignored
        ml = testAttributeValue.validate(project);
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_ATTRIBUTE_NOT_FOUND));
        
        testAttributeValue.setValue(null); // the value is null, thus the attribute will be ignored
        ml = testAttributeValue.validate(project);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_ATTRIBUTE_NOT_FOUND));
        
        pctSuper.newPolicyCmptTypeAttribute().setName("attributeSuper");
        testAttribute.setAttribute("attributeSuper");
        assertNotNull(testAttribute.findAttribute());
    }
}
