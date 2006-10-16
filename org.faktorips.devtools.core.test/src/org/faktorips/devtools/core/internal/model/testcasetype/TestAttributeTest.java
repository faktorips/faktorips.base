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
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestAttributeTest extends AbstractIpsPluginTest {

    private ITestAttribute testAttribute;
    private IIpsProject project;
    
    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        ITestCaseType type = (ITestCaseType )newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculation");
        testAttribute = type.newExpectedResultPolicyCmptTypeParameter().newExpectedResultTestAttribute();
    }
    
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element attributeEl = XmlUtil.getFirstElement(docEl);
        testAttribute.initFromXml(attributeEl);
        assertEquals("attribute1", testAttribute.getAttribute());
        assertEquals("attribute1Name", testAttribute.getName());
        assertTrue(testAttribute.isInputAttribute());
        assertFalse(testAttribute.isExpextedResultAttribute());
        
        attributeEl = XmlUtil.getElement(docEl, 1);
        testAttribute.initFromXml(attributeEl);
        assertEquals("attribute2", testAttribute.getAttribute());
        assertEquals("attribute2Name", testAttribute.getName());
        assertFalse(testAttribute.isInputAttribute());
        assertTrue(testAttribute.isExpextedResultAttribute());

        attributeEl = XmlUtil.getElement(docEl, 2);
        testAttribute.initFromXml(attributeEl);
        assertEquals("attribute3", testAttribute.getAttribute());
        assertEquals("attribute3Name", testAttribute.getName());
        assertFalse(testAttribute.isInputAttribute());
        assertFalse(testAttribute.isExpextedResultAttribute());
        
        attributeEl = XmlUtil.getElement(docEl, 3);
        testAttribute.initFromXml(attributeEl);
        assertEquals("attribute4", testAttribute.getAttribute());
        assertEquals("attribute4Name", testAttribute.getName());
        assertFalse(testAttribute.isInputAttribute());
        assertFalse(testAttribute.isExpextedResultAttribute());
        
        boolean exceptionOccored = false;
        try {
            // test unsupported test attribute type
            ((TestAttribute)testAttribute).setTestAttributeType(TestParameterType.COMBINED);
        } catch (Exception e) {
            exceptionOccored = true;
        } finally{
            assertTrue(exceptionOccored);
        }
    }

    public void testToXml() {
        testAttribute.setAttribute("attribute2");
        testAttribute.setName("attribute2Name");
        ((TestAttribute)testAttribute).setTestAttributeType(TestParameterType.INPUT);
        Element el = testAttribute.toXml(newDocument());

        testAttribute.setAttribute("attributeName3");
        testAttribute.setName("attribute3Name");
        ((TestAttribute)testAttribute).setTestAttributeType(TestParameterType.EXPECTED_RESULT);
        
        testAttribute.initFromXml(el);
        assertEquals("attribute2", testAttribute.getAttribute());
        assertEquals("attribute2Name", testAttribute.getName());
        assertTrue(testAttribute.isInputAttribute());
        assertFalse(testAttribute.isExpextedResultAttribute());
    }
    
    public void testFindAttribute() throws Exception{
        IPolicyCmptType policyCmptTypeSuper = newPolicyCmptType(project, "policyCmptSuper");
        IAttribute attr1 = policyCmptTypeSuper.newAttribute();
        attr1.setName("attribute1");
        IAttribute attr2 = policyCmptTypeSuper.newAttribute();
        attr2.setName("attribute2");
        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmpt");
        IAttribute attr3 = policyCmptType.newAttribute();
        attr3.setName("attribute3");
        IAttribute attr4 = policyCmptType.newAttribute();
        attr4.setName("attribute4");
        policyCmptType.setSupertype(policyCmptTypeSuper.getQualifiedName());
        
        ((ITestPolicyCmptTypeParameter)testAttribute.getParent()).setPolicyCmptType("policyCmpt");
        testAttribute.setAttribute("attribute4");
        assertEquals(attr4, testAttribute.findAttribute());
        testAttribute.setAttribute("attribute3");
        assertEquals(attr3, testAttribute.findAttribute());
        testAttribute.setAttribute("attribute2");
        assertEquals(attr2, testAttribute.findAttribute());
        testAttribute.setAttribute("attribute1");
        assertEquals(attr1, testAttribute.findAttribute());
    }

    public void testValidateAttributeNotFound() throws Exception{
        IPolicyCmptType pct = newPolicyCmptType(project, "policyCmptType");
        IAttribute attr = pct.newAttribute();
        attr.setName("attribute1");
        
        ((ITestPolicyCmptTypeParameter)testAttribute.getParent()).setPolicyCmptType(pct.getQualifiedName());
        testAttribute.setAttribute(attr.getName());
        MessageList ml = testAttribute.validate();
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_ATTRIBUTE_NOT_FOUND));

        attr.setName("x");
        ml = testAttribute.validate();
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_ATTRIBUTE_NOT_FOUND));
    }
    
    public void testValidateWrongType() throws Exception{
        MessageList ml = testAttribute.validate();
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_WRONG_TYPE));
        
        Element docEl = getTestDocument().getDocumentElement();
        Element attributeEl = XmlUtil.getElement(docEl, "TestAttribute", 3);
        testAttribute.initFromXml(attributeEl);
        // force revalidation of object
        String attribute = testAttribute.getAttribute();
        testAttribute.setAttribute(attribute + "_new");
        testAttribute.setAttribute(attribute);
        ml = testAttribute.validate();
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_WRONG_TYPE));
    }

    public void testValidateTypeDoesNotMatchParentType() throws Exception{
        ITestPolicyCmptTypeParameter param = (ITestPolicyCmptTypeParameter) testAttribute.getParent();
        param.setTestParameterType(TestParameterType.COMBINED);
        testAttribute.setTestAttributeType(TestParameterType.EXPECTED_RESULT);
        MessageList ml = testAttribute.validate();
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE));
        testAttribute.setTestAttributeType(TestParameterType.INPUT);
        ml = testAttribute.validate();
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE));

        param = (ITestPolicyCmptTypeParameter) testAttribute.getParent();
        param.setTestParameterType(TestParameterType.EXPECTED_RESULT);
        testAttribute.setTestAttributeType(TestParameterType.EXPECTED_RESULT);
        ml = testAttribute.validate();
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE));
        testAttribute.setTestAttributeType(TestParameterType.INPUT);
        ml = testAttribute.validate();
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE));
        
        param = (ITestPolicyCmptTypeParameter) testAttribute.getParent();
        param.setTestParameterType(TestParameterType.INPUT);
        testAttribute.setTestAttributeType(TestParameterType.INPUT);
        ml = testAttribute.validate();
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE));
        testAttribute.setTestAttributeType(TestParameterType.EXPECTED_RESULT);
        ml = testAttribute.validate();
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE));
    }

    public void testValidateDuplicateTestAttributeName() throws Exception{
        MessageList ml = testAttribute.validate();
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DUPLICATE_TEST_ATTRIBUTE_NAME));

        ITestPolicyCmptTypeParameter param = (ITestPolicyCmptTypeParameter) testAttribute.getParent();
        param.newInputTestAttribute().setName(testAttribute.getName());
        ml = testAttribute.validate();
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DUPLICATE_TEST_ATTRIBUTE_NAME));

        testAttribute.setName("newName");
        ml = testAttribute.validate();
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DUPLICATE_TEST_ATTRIBUTE_NAME));
    }

    public void testValidateExpectedOrComputedButNotExpectedRes() throws Exception{
        IPolicyCmptType pct = newPolicyCmptType(project, "policyCmptType");
        IAttribute attr = pct.newAttribute();
        attr.setName("attribute1");
        
        ((ITestPolicyCmptTypeParameter)testAttribute.getParent()).setPolicyCmptType(pct.getQualifiedName());
        testAttribute.setAttribute(attr.getName());
        MessageList ml = testAttribute.validate();
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DERIVED_OR_COMPUTED_BUT_NOT_EXPECTED_RES));

        attr.setAttributeType(AttributeType.COMPUTED);
        testAttribute.setTestAttributeType(TestParameterType.EXPECTED_RESULT);
        ml = testAttribute.validate();
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DERIVED_OR_COMPUTED_BUT_NOT_EXPECTED_RES));
        testAttribute.setTestAttributeType(TestParameterType.INPUT);
        ml = testAttribute.validate();
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DERIVED_OR_COMPUTED_BUT_NOT_EXPECTED_RES));

        attr.setAttributeType(AttributeType.DERIVED);
        testAttribute.setTestAttributeType(TestParameterType.EXPECTED_RESULT);
        ml = testAttribute.validate();
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DERIVED_OR_COMPUTED_BUT_NOT_EXPECTED_RES));
        testAttribute.setTestAttributeType(TestParameterType.INPUT);
        ml = testAttribute.validate();
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DERIVED_OR_COMPUTED_BUT_NOT_EXPECTED_RES));

        attr.setAttributeType(AttributeType.CHANGEABLE);
        testAttribute.setTestAttributeType(TestParameterType.EXPECTED_RESULT);
        ml = testAttribute.validate();
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DERIVED_OR_COMPUTED_BUT_NOT_EXPECTED_RES));
        testAttribute.setTestAttributeType(TestParameterType.INPUT);
        ml = testAttribute.validate();
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DERIVED_OR_COMPUTED_BUT_NOT_EXPECTED_RES));
    
    }
}
