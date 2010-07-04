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

package org.faktorips.devtools.core.internal.model.testcasetype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestAttributeTest extends AbstractIpsPluginTest {

    private ITestAttribute testAttribute;
    private IIpsProject ipsProject;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        ITestCaseType type = (ITestCaseType)newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculation");
        testAttribute = type.newExpectedResultPolicyCmptTypeParameter().newExpectedResultTestAttribute();
    }

    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element attributeEl = XmlUtil.getFirstElement(docEl);
        testAttribute.initFromXml(attributeEl);
        assertEquals("attribute1", testAttribute.getAttribute());
        assertEquals("policyCmptType1", testAttribute.getPolicyCmptType());
        assertEquals("attribute1Name", testAttribute.getName());
        assertTrue(testAttribute.isInputAttribute());
        assertFalse(testAttribute.isExpextedResultAttribute());

        attributeEl = XmlUtil.getElement(docEl, 1);
        testAttribute.initFromXml(attributeEl);
        assertEquals("attribute2", testAttribute.getAttribute());
        assertEquals("", testAttribute.getPolicyCmptType());
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

        attributeEl = XmlUtil.getElement(docEl, 4);
        testAttribute.initFromXml(attributeEl);
        assertEquals("", testAttribute.getAttribute());
        assertEquals("attribute5Name", testAttribute.getName());
        assertEquals("String", testAttribute.getDatatype());
        assertFalse(testAttribute.isInputAttribute());
        assertFalse(testAttribute.isExpextedResultAttribute());

        boolean exceptionOccored = false;
        try {
            // test unsupported test attribute type
            ((TestAttribute)testAttribute).setTestAttributeType(TestParameterType.COMBINED);
        } catch (Exception e) {
            exceptionOccored = true;
        } finally {
            assertTrue(exceptionOccored);
        }
    }

    public void testToXml() {
        testAttribute.setAttribute("attribute2");
        testAttribute.setDatatype("Money");
        testAttribute.setPolicyCmptType("policyCmptTyp2");
        testAttribute.setName("attribute2Name");
        ((TestAttribute)testAttribute).setTestAttributeType(TestParameterType.INPUT);
        Element el = testAttribute.toXml(newDocument());

        testAttribute.setAttribute("attributeName3");
        testAttribute.setDatatype("String");
        testAttribute.setPolicyCmptType("policyCmptTyp3");
        testAttribute.setName("attribute3Name");
        ((TestAttribute)testAttribute).setTestAttributeType(TestParameterType.EXPECTED_RESULT);

        testAttribute.initFromXml(el);
        assertEquals("attribute2", testAttribute.getAttribute());
        assertEquals("Money", testAttribute.getDatatype());
        assertEquals("policyCmptTyp2", testAttribute.getPolicyCmptType());
        assertEquals("attribute2Name", testAttribute.getName());
        assertTrue(testAttribute.isInputAttribute());
        assertFalse(testAttribute.isExpextedResultAttribute());
    }

    public void testFindAttribute() throws Exception {
        IPolicyCmptType policyCmptTypeSuper = newPolicyCmptType(ipsProject, "policyCmptSuper");
        IPolicyCmptTypeAttribute attr1 = policyCmptTypeSuper.newPolicyCmptTypeAttribute();
        attr1.setName("attribute1");
        IPolicyCmptTypeAttribute attr2 = policyCmptTypeSuper.newPolicyCmptTypeAttribute();
        attr2.setName("attribute2");
        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "policyCmpt");
        IPolicyCmptTypeAttribute attr3 = policyCmptType.newPolicyCmptTypeAttribute();
        attr3.setName("attribute3");
        IPolicyCmptTypeAttribute attr4 = policyCmptType.newPolicyCmptTypeAttribute();
        attr4.setName("attribute4");
        policyCmptType.setSupertype(policyCmptTypeSuper.getQualifiedName());

        ((ITestPolicyCmptTypeParameter)testAttribute.getParent()).setPolicyCmptType("policyCmpt");
        testAttribute.setAttribute("attribute4");
        assertEquals(attr4, testAttribute.findAttribute(ipsProject));
        testAttribute.setAttribute("attribute3");
        assertEquals(attr3, testAttribute.findAttribute(ipsProject));
        testAttribute.setAttribute("attribute2");
        assertEquals(attr2, testAttribute.findAttribute(ipsProject));
        testAttribute.setAttribute("attribute1");
        assertEquals(attr1, testAttribute.findAttribute(ipsProject));
    }

    public void testValidateAttributeNotFound() throws Exception {
        IPolicyCmptType pct = newPolicyCmptType(ipsProject, "policyCmptType");
        IPolicyCmptTypeAttribute attr = pct.newPolicyCmptTypeAttribute();
        attr.setName("attribute1");

        ((ITestPolicyCmptTypeParameter)testAttribute.getParent()).setPolicyCmptType(pct.getQualifiedName());
        testAttribute.setAttribute(attr.getName());
        MessageList ml = testAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_ATTRIBUTE_NOT_FOUND));

        attr.setName("x");
        ml = testAttribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_ATTRIBUTE_NOT_FOUND));
    }

    public void testValidateWrongType() throws Exception {
        MessageList ml = testAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_WRONG_TYPE));

        Element docEl = getTestDocument().getDocumentElement();
        Element attributeEl = XmlUtil.getElement(docEl, "TestAttribute", 3);
        testAttribute.initFromXml(attributeEl);
        // force revalidation of object
        String attribute = testAttribute.getAttribute();
        testAttribute.setAttribute(attribute + "_new");
        testAttribute.setAttribute(attribute);
        ml = testAttribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_WRONG_TYPE));
    }

    public void testValidateTypeDoesNotMatchParentType() throws Exception {
        ITestPolicyCmptTypeParameter param = (ITestPolicyCmptTypeParameter)testAttribute.getParent();
        param.setTestParameterType(TestParameterType.COMBINED);
        testAttribute.setTestAttributeType(TestParameterType.EXPECTED_RESULT);
        MessageList ml = testAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE));
        testAttribute.setTestAttributeType(TestParameterType.INPUT);
        ml = testAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE));

        param = (ITestPolicyCmptTypeParameter)testAttribute.getParent();
        param.setTestParameterType(TestParameterType.EXPECTED_RESULT);
        testAttribute.setTestAttributeType(TestParameterType.EXPECTED_RESULT);
        ml = testAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE));
        testAttribute.setTestAttributeType(TestParameterType.INPUT);
        ml = testAttribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE));

        param = (ITestPolicyCmptTypeParameter)testAttribute.getParent();
        param.setTestParameterType(TestParameterType.INPUT);
        testAttribute.setTestAttributeType(TestParameterType.INPUT);
        ml = testAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE));
        testAttribute.setTestAttributeType(TestParameterType.EXPECTED_RESULT);
        ml = testAttribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE));
    }

    public void testValidateDuplicateTestAttributeName() throws Exception {
        testAttribute.setName("testAttribute");
        MessageList ml = testAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DUPLICATE_TEST_ATTRIBUTE_NAME));

        ITestPolicyCmptTypeParameter param = (ITestPolicyCmptTypeParameter)testAttribute.getParent();
        param.newInputTestAttribute().setName(testAttribute.getName());
        ml = testAttribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DUPLICATE_TEST_ATTRIBUTE_NAME));

        testAttribute.setName("newName");
        ml = testAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DUPLICATE_TEST_ATTRIBUTE_NAME));
    }

    public void testValidateExpectedOrComputedButNotExpectedRes() throws Exception {
        IPolicyCmptType pct = newPolicyCmptType(ipsProject, "policyCmptType");
        IPolicyCmptTypeAttribute attr = pct.newPolicyCmptTypeAttribute();
        attr.setName("attribute1");

        ((ITestPolicyCmptTypeParameter)testAttribute.getParent()).setPolicyCmptType(pct.getQualifiedName());
        testAttribute.setAttribute(attr.getName());
        MessageList ml = testAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DERIVED_ON_THE_FLY_ATTRIBUTES_NOT_SUPPORTED));

        attr.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
        testAttribute.setTestAttributeType(TestParameterType.EXPECTED_RESULT);
        ml = testAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DERIVED_ON_THE_FLY_ATTRIBUTES_NOT_SUPPORTED));
        testAttribute.setTestAttributeType(TestParameterType.INPUT);
        ml = testAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DERIVED_ON_THE_FLY_ATTRIBUTES_NOT_SUPPORTED));

        attr.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
        testAttribute.setTestAttributeType(TestParameterType.EXPECTED_RESULT);
        ml = testAttribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DERIVED_ON_THE_FLY_ATTRIBUTES_NOT_SUPPORTED));
        testAttribute.setTestAttributeType(TestParameterType.INPUT);
        ml = testAttribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DERIVED_ON_THE_FLY_ATTRIBUTES_NOT_SUPPORTED));

        attr.setAttributeType(AttributeType.CHANGEABLE);
        testAttribute.setTestAttributeType(TestParameterType.EXPECTED_RESULT);
        ml = testAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DERIVED_ON_THE_FLY_ATTRIBUTES_NOT_SUPPORTED));
        testAttribute.setTestAttributeType(TestParameterType.INPUT);
        ml = testAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DERIVED_ON_THE_FLY_ATTRIBUTES_NOT_SUPPORTED));
    }

    public void testValidateDuplicateAttributeType() throws Exception {
        testAttribute.setName("a");
        testAttribute.setAttribute("attribute1");

        ITestPolicyCmptTypeParameter param = (ITestPolicyCmptTypeParameter)testAttribute.getParent();
        ITestAttribute testAttribute2 = param.newExpectedResultTestAttribute();
        testAttribute2.setName("b");
        testAttribute2.setAttribute("attribute1");

        MessageList ml = testAttribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DUPLICATE_ATTRIBUTE_AND_TYPE));

        testAttribute2.setAttribute("attribute2");
        ml = testAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DUPLICATE_ATTRIBUTE_AND_TYPE));

        // test with attributes not based on model attributes
        ITestAttribute testAttribute3 = param.newExpectedResultTestAttribute();
        ITestAttribute testAttribute4 = param.newExpectedResultTestAttribute();
        testAttribute3.setName("String3");
        testAttribute3.setDatatype("String");
        testAttribute4.setName("String4");
        testAttribute4.setDatatype("String");
        ml = testAttribute3.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DUPLICATE_ATTRIBUTE_AND_TYPE));
        ml = testAttribute4.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DUPLICATE_ATTRIBUTE_AND_TYPE));
    }

    public void testValidateNameMustNotBeEmpty() throws Exception {
        testAttribute.setName("attribute1");
        MessageList ml = testAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_ATTRIBUTE_NAME_IS_EMPTY));

        testAttribute.setName("");
        ml = testAttribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_ATTRIBUTE_NAME_IS_EMPTY));

        testAttribute.setName(null);
        ml = testAttribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_ATTRIBUTE_NAME_IS_EMPTY));
    }

    public void testValidateDatatypeAndAttributeGiven() throws Exception {
        testAttribute.setAttribute("");
        testAttribute.setDatatype("Y");
        MessageList ml = testAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DATATYPE_AND_ATTRIBUTE_GIVEN));

        testAttribute.setAttribute("X");
        testAttribute.setDatatype("Y");
        ml = testAttribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DATATYPE_AND_ATTRIBUTE_GIVEN));
        assertEquals(Message.ERROR, ml.getMessageByCode(ITestAttribute.MSGCODE_DATATYPE_AND_ATTRIBUTE_GIVEN)
                .getSeverity());

        testAttribute.setAttribute("X");
        testAttribute.setDatatype("");
        ml = testAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DATATYPE_AND_ATTRIBUTE_GIVEN));
    }

    public void testValidateDatatypeNotFound() throws Exception {
        testAttribute.setAttribute("");
        testAttribute.setDatatype("String");
        MessageList ml = testAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DATATYPE_NOT_FOUND));

        testAttribute.setAttribute("");
        testAttribute.setDatatype("Y");
        ml = testAttribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_DATATYPE_NOT_FOUND));
        assertEquals(Message.ERROR, ml.getMessageByCode(ITestAttribute.MSGCODE_DATATYPE_NOT_FOUND).getSeverity());
    }

    public void testIsBasedOnModelAttribute() throws CoreException {
        testAttribute.setDatatype("");
        testAttribute.setAttribute("");
        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "SubPolicy1", "SubProduct1");
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        policyCmptTypeAttribute.setName("modelAttribute");
        assertFalse(testAttribute.isBasedOnModelAttribute());

        testAttribute.setAttribute(policyCmptTypeAttribute);
        assertTrue(testAttribute.isBasedOnModelAttribute());

        testAttribute.setAttribute("");
        assertFalse(testAttribute.isBasedOnModelAttribute());
    }

    public void testIsAttributeRelevantByProductCmpt() throws CoreException {
        ITestPolicyCmptTypeParameter param = (ITestPolicyCmptTypeParameter)testAttribute.getParent();
        param.setRequiresProductCmpt(true);

        IPolicyCmptType base = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        IPolicyCmptType sub1 = newPolicyAndProductCmptType(ipsProject, "SubPolicy1", "SubProduct1");
        IPolicyCmptType sub2 = newPolicyAndProductCmptType(ipsProject, "SubPolicy2", "SubProduct2");
        sub1.setSupertype(base.getQualifiedName());
        sub2.setSupertype(base.getQualifiedName());

        IProductCmpt productCmptSub1 = newProductCmpt(sub1.findProductCmptType(ipsProject), "productSub1");
        IProductCmpt productCmptSub2 = newProductCmpt(sub2.findProductCmptType(ipsProject), "productSub2");

        IPolicyCmptTypeAttribute attributeSub1 = sub1.newPolicyCmptTypeAttribute();
        attributeSub1.setName("attrSub1");
        attributeSub1.setProductRelevant(true);
        attributeSub1.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);

        IPolicyCmptTypeAttribute attributeSub2 = sub2.newPolicyCmptTypeAttribute();
        attributeSub2.setName("attrSub2");
        attributeSub2.setProductRelevant(true);
        attributeSub2.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);

        testAttribute.setName("test");
        testAttribute.setAttribute("attrSub1");
        assertTrue(testAttribute.isAttributeRelevantByProductCmpt(productCmptSub1, ipsProject));
        assertFalse(testAttribute.isAttributeRelevantByProductCmpt(productCmptSub2, ipsProject));

        param.setRequiresProductCmpt(false);
        // the parameter is not product relevant, threrefore there is no product cmpt
        // in this case the attribute is always relevant
        assertTrue(testAttribute.isAttributeRelevantByProductCmpt(null, ipsProject));
    }

    /**
     * Attributes of suptypes will never been found, because this feature is only available on the
     * test case side, see TestAttributeValue.validateSelf()
     */
    public void testFindAttributeInSubtype() throws Exception {
        IPolicyCmptType policyCmptTypeSuper = newPolicyCmptType(ipsProject, "policyCmptSuper");
        IPolicyCmptTypeAttribute attr1 = policyCmptTypeSuper.newPolicyCmptTypeAttribute();
        attr1.setName("attribute1");
        IPolicyCmptTypeAttribute attr2 = policyCmptTypeSuper.newPolicyCmptTypeAttribute();
        attr2.setName("attribute2");
        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "policyCmpt");
        IPolicyCmptTypeAttribute attr3 = policyCmptType.newPolicyCmptTypeAttribute();
        attr3.setName("attribute3");
        IPolicyCmptTypeAttribute attr4 = policyCmptType.newPolicyCmptTypeAttribute();
        attr4.setName("attribute4");
        policyCmptType.setSupertype(policyCmptTypeSuper.getQualifiedName());

        ITestPolicyCmptTypeParameter cmptTypeParameter = (ITestPolicyCmptTypeParameter)testAttribute.getParent();
        cmptTypeParameter.setPolicyCmptType(policyCmptTypeSuper.getQualifiedName());

        testAttribute.setAttribute("attribute4");
        testAttribute.setPolicyCmptType(policyCmptType.getQualifiedName());
        assertNotNull(testAttribute.findAttribute(ipsProject));

        testAttribute.setAttribute(attr1);
        assertEquals("", testAttribute.getPolicyCmptType());
        assertEquals(policyCmptTypeSuper.getQualifiedName(), testAttribute.getCorrespondingPolicyCmptType());

        testAttribute.setAttribute(attr3);
        assertEquals(policyCmptType.getQualifiedName(), testAttribute.getPolicyCmptType());
        assertEquals(policyCmptType.getQualifiedName(), testAttribute.getCorrespondingPolicyCmptType());
    }

    public void testValidateName() throws CoreException {
        MessageList ml;

        // test validate name for extension attribute
        // -> must be a valid java field identifier
        testAttribute.setName("validName");
        testAttribute.setDatatype("String");
        testAttribute.setAttribute("");
        assertFalse(testAttribute.isBasedOnModelAttribute());
        ml = testAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_INVALID_TEST_ATTRIBUTE_NAME));
        testAttribute.setName("invalid Name");
        ml = testAttribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(ITestAttribute.MSGCODE_INVALID_TEST_ATTRIBUTE_NAME));

        // test validate name for non extension attribute (test attributes based on model attribute)
        // -> must be not a valid java field identifier, for this kind of test attributes no name
        // validation exists
        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "SubPolicy1", "SubProduct1");
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        policyCmptTypeAttribute.setName("modelAttribute");
        testAttribute.setDatatype("");
        testAttribute.setAttribute(policyCmptTypeAttribute);
        assertTrue(testAttribute.isBasedOnModelAttribute());
        testAttribute.setName("no invalid Name");
        ml = testAttribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(ITestAttribute.MSGCODE_INVALID_TEST_ATTRIBUTE_NAME));
    }
}
