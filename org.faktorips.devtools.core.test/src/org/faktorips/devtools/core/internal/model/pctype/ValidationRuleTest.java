/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ChangingOverTimePropertyValidator;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.MessageSeverity;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.LocalizedString;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ValidationRuleTest extends AbstractIpsPluginTest {

    private PolicyCmptType policyCmptType;
    private IIpsSrcFile ipsSrcFile;
    private IValidationRule validationRule;
    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject("TestProject");
        policyCmptType = newPolicyCmptType(ipsProject, "Policy");
        ipsSrcFile = policyCmptType.getIpsSrcFile();
        validationRule = policyCmptType.newRule();
        ipsSrcFile.save(true, null);
    }

    @Test
    public void testRemove() {
        validationRule.delete();
        assertEquals(0, policyCmptType.getValidationRules().size());
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test
    public void testSetName() {
        validationRule.setName("newName");
        assertEquals("newName", validationRule.getName());
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test
    public void testAddValidatedAttribute() {
        validationRule.addValidatedAttribute("a");
        validationRule.addValidatedAttribute("b");
        assertEquals("a", validationRule.getValidatedAttributes()[0]);
        assertEquals("b", validationRule.getValidatedAttributes()[1]);
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test
    public void testSetValidatedAttributeAt() {
        validationRule.addValidatedAttribute("a");
        validationRule.addValidatedAttribute("b");
        validationRule.setValidatedAttributeAt(1, "c");
        assertEquals("c", validationRule.getValidatedAttributes()[1]);
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test
    public void testGetValidatedAttributeAt() {
        validationRule.addValidatedAttribute("a");
        validationRule.addValidatedAttribute("b");
        assertEquals("a", validationRule.getValidatedAttributeAt(0));
        assertEquals("b", validationRule.getValidatedAttributeAt(1));
    }

    @Test
    public void testRemoveValidatedAttribute() {
        validationRule.addValidatedAttribute("a");
        validationRule.addValidatedAttribute("b");
        validationRule.removeValidatedAttribute(0);
        assertEquals("b", validationRule.getValidatedAttributeAt(0));
    }

    @Test
    public void testValidatedAttrSpecifiedInSrc() {
        assertFalse(ipsSrcFile.isDirty());
        validationRule.setValidatedAttrSpecifiedInSrc(true);
        assertTrue(ipsSrcFile.isDirty());
        assertEquals(true, validationRule.isValidatedAttrSpecifiedInSrc());
    }

    @Test
    public void testInitFromXml() {
        Document doc = getTestDocument();
        validationRule.setAppliedForAllBusinessFunctions(true);
        validationRule.initFromXml(doc.getDocumentElement());
        assertEquals("42", validationRule.getId());
        assertEquals("checkAge", validationRule.getName());
        assertEquals("ageMissing", validationRule.getMessageCode());
        assertEquals("messageText", validationRule.getMessageText().get(Locale.GERMAN).getValue());
        assertEquals(MessageSeverity.WARNING, validationRule.getMessageSeverity());
        assertFalse(validationRule.isAppliedForAllBusinessFunctions());
        String[] functions = validationRule.getBusinessFunctions();
        assertEquals(2, functions.length);
        assertEquals("NewOffer", functions[0]);
        assertEquals("Renewal", functions[1]);
        String[] validatedAttributes = validationRule.getValidatedAttributes();
        assertEquals("a", validatedAttributes[0]);
        assertEquals("b", validatedAttributes[1]);
        List<String> markers = validationRule.getMarkers();
        assertEquals(2, markers.size());
        assertTrue(markers.contains("marker1"));
        assertTrue(markers.contains("marker2"));
    }

    @Test
    public void testToXmlDocument() {
        validationRule = policyCmptType.newRule(); // => id=1 because it's the second validationRule
        validationRule.setName("checkAge");
        validationRule.setAppliedForAllBusinessFunctions(true);
        validationRule.setMessageCode("ageMissing");
        validationRule.getMessageText().add(new LocalizedString(Locale.GERMAN, "messageText"));
        validationRule.setMessageSeverity(MessageSeverity.WARNING);
        validationRule.setBusinessFunctions(new String[] { "NewOffer", "Renewal" });
        validationRule.addValidatedAttribute("a");
        validationRule.setCheckValueAgainstValueSetRule(true);
        validationRule.setCategory("foo");
        validationRule.setMarkers(Arrays.asList(new String[] { "marker1", "marker2" }));

        Element element = validationRule.toXml(newDocument());

        ValidationRule copy = new ValidationRule(mock(IPolicyCmptType.class), "");
        copy.initFromXml(element);
        assertEquals(validationRule.getId(), copy.getId());
        assertEquals("checkAge", copy.getName());
        assertEquals("ageMissing", copy.getMessageCode());
        assertEquals("messageText", copy.getMessageText().get(Locale.GERMAN).getValue());
        assertEquals(MessageSeverity.WARNING, copy.getMessageSeverity());
        assertTrue(copy.isAppliedForAllBusinessFunctions());
        String[] functions = copy.getBusinessFunctions();
        assertEquals(2, functions.length);
        assertEquals("NewOffer", functions[0]);
        assertEquals("Renewal", functions[1]);
        String[] validationAttributes = copy.getValidatedAttributes();
        assertEquals("a", validationAttributes[0]);
        assertTrue(copy.isCheckValueAgainstValueSetRule());
        assertEquals("foo", copy.getCategory());
        List<String> markers = copy.getMarkers();
        assertEquals(2, markers.size());
        assertTrue(markers.contains("marker1"));
        assertTrue(markers.contains("marker2"));
    }

    @Test
    public void testAddBusinessFunction() {
        validationRule.addBusinessFunction("f1");
        assertEquals(1, validationRule.getNumOfBusinessFunctions());
        assertEquals("f1", validationRule.getBusinessFunction(0));

        validationRule.addBusinessFunction("f2");
        assertEquals(2, validationRule.getNumOfBusinessFunctions());
        assertEquals("f2", validationRule.getBusinessFunction(1));
    }

    @Test
    public void testSetBusinessFunction() {
        validationRule.addBusinessFunction("f1");
        validationRule.addBusinessFunction("f2");

        validationRule.setBusinessFunctions(1, "changed");
        assertEquals("changed", validationRule.getBusinessFunction(1));
    }

    @Test
    public void testRemoveBusinessFunction() {
        validationRule.addBusinessFunction("f1");
        validationRule.addBusinessFunction("f2");
        validationRule.addBusinessFunction("f3");
        validationRule.addBusinessFunction("f4");

        validationRule.removeBusinessFunction(3);
        validationRule.removeBusinessFunction(1);
        assertEquals(2, validationRule.getNumOfBusinessFunctions());
        assertEquals("f1", validationRule.getBusinessFunction(0));
        assertEquals("f3", validationRule.getBusinessFunction(1));
    }

    @Test
    public void testValidate() throws Exception {
        validationRule.addValidatedAttribute("a");

        // validation is expected to fail because the specified attribute doesn't exist for the
        // PolicyCmptType
        MessageList messageList = validationRule.validate(ipsSrcFile.getIpsProject()).getMessagesFor(validationRule,
                "validatedAttributes");
        assertEquals(1, messageList.size());

        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("a");
        attr.setAttributeType(AttributeType.CHANGEABLE);
        attr.setDatatype("String");

        messageList = validationRule.validate(ipsSrcFile.getIpsProject()).getMessagesFor(validationRule,
                "validatedAttributes");
        assertEquals(0, messageList.size());

        // validation is expected to fail because of duplicate attribute entries
        validationRule.addValidatedAttribute("a");
        messageList = validationRule.validate(ipsSrcFile.getIpsProject()).getMessagesFor(validationRule,
                "validatedAttributes");
        assertEquals(1, messageList.size());
    }

    @Test
    public void testValidateBusinessFunctions() throws CoreException {
        validationRule.setAppliedForAllBusinessFunctions(true);
        MessageList msgList = validationRule.validate(ipsSrcFile.getIpsProject());
        msgList = msgList.getMessagesFor(validationRule, IValidationRule.PROPERTY_APPLIED_FOR_ALL_BUSINESS_FUNCTIONS);
        assertTrue(msgList.isEmpty());

        validationRule.setAppliedForAllBusinessFunctions(false);
        msgList = validationRule.validate(ipsSrcFile.getIpsProject());
        msgList = msgList.getMessagesFor(validationRule, IValidationRule.PROPERTY_APPLIED_FOR_ALL_BUSINESS_FUNCTIONS);
        assertFalse(msgList.isEmpty());

        validationRule.setAppliedForAllBusinessFunctions(false);
        validationRule.addBusinessFunction("function");
        msgList = validationRule.validate(ipsSrcFile.getIpsProject());
        msgList = msgList.getMessagesFor(validationRule, IValidationRule.PROPERTY_APPLIED_FOR_ALL_BUSINESS_FUNCTIONS);
        assertTrue(msgList.isEmpty());
    }

    @Test
    public void testValidateMsgCodeShouldntBeNull() throws CoreException {
        validationRule.setMessageCode(null);
        MessageList list = validationRule.validate(ipsSrcFile.getIpsProject());
        assertNotNull(list.getMessageByCode(IValidationRule.MSGCODE_MSGCODE_SHOULDNT_BE_EMPTY));
        validationRule.setMessageCode("");
        list = validationRule.validate(ipsSrcFile.getIpsProject());
        assertNotNull(list.getMessageByCode(IValidationRule.MSGCODE_MSGCODE_SHOULDNT_BE_EMPTY));

        validationRule.setMessageCode("code");
        list = validationRule.validate(ipsSrcFile.getIpsProject());
        assertNull(list.getMessageByCode(IValidationRule.MSGCODE_MSGCODE_SHOULDNT_BE_EMPTY));
    }

    @Test
    public void testValidateMarker() throws CoreException {
        validationRule.setMarkers(Arrays.asList(new String[] { "marker1", "marker2" }));
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setMarkerEnumsEnabled(true);
        properties.addMarkerEnum("markerEnum");
        EnumType markerEnum = newEnumType(ipsProject, "markerEnum");
        IEnumAttribute enumAttribute = markerEnum.newEnumAttribute();
        enumAttribute.setIdentifier(true);
        IEnumValue enumValue = markerEnum.newEnumValue();
        enumValue.setEnumAttributeValue(enumAttribute, new StringValue("marker1"));
        ipsProject.setProperties(properties);

        MessageList msgList = validationRule.validate(ipsProject);

        assertFalse(msgList.isEmpty());
        assertNotNull(msgList.getMessageByCode(IValidationRule.MSGCODE_INVALID_MARKER_ID));
    }

    @Test
    public void testValidateMarker_InvalidMarkerEnum() throws CoreException {
        validationRule.setMarkers(Arrays.asList(new String[] { "marker1", "marker2" }));

        MessageList msgList = validationRule.validate(ipsProject);

        assertFalse(msgList.isEmpty());
        assertNotNull(msgList.getMessageByCode(IValidationRule.MSGCODE_INVALID_MARKER_ID));
    }

    @Test
    public void testConstantAttributesCantBeValidated() throws CoreException {
        IPolicyCmptTypeAttribute a = policyCmptType.newPolicyCmptTypeAttribute();
        a.setName("a1");
        a.setAttributeType(AttributeType.CONSTANT);
        validationRule.addValidatedAttribute("a1");
        assertNotNull(validationRule.validate(ipsSrcFile.getIpsProject()).getMessageByCode(
                IValidationRule.MSGCODE_CONSTANT_ATTRIBUTES_CANT_BE_VALIDATED));

        a.setAttributeType(AttributeType.CHANGEABLE);
        assertNull(validationRule.validate(ipsSrcFile.getIpsProject()).getMessageByCode(
                IValidationRule.MSGCODE_CONSTANT_ATTRIBUTES_CANT_BE_VALIDATED));
    }

    @Test
    public void testConfigurableByProductCompt() throws CoreException {
        ProductCmptType prodType = newProductCmptType(ipsProject, "ProdType");
        policyCmptType.setProductCmptType(prodType.getQualifiedName());

        policyCmptType.setConfigurableByProductCmptType(true);
        validationRule.setConfigurableByProductComponent(true);
        assertTrue("Rule is supposed to be configurable", validationRule.isConfigurableByProductComponent());

        validationRule.setConfigurableByProductComponent(false);
        assertFalse("Rule isn't supposed to be configurable", validationRule.isConfigurableByProductComponent());

        validationRule.setConfigurableByProductComponent(false);
        policyCmptType.setConfigurableByProductCmptType(false);
        assertFalse("Rule isn't supposed to be configurable", validationRule.isConfigurableByProductComponent());

        validationRule.setConfigurableByProductComponent(true);
        policyCmptType.setConfigurableByProductCmptType(false);
        assertFalse("Rule isn't supposed to be configurable", validationRule.isConfigurableByProductComponent());
    }

    @Test
    public void testIsPolicyCmptTypeProperty() {
        assertTrue(validationRule.isPolicyCmptTypeProperty());
    }

    @Test
    public void testIsPropertyFor() throws CoreException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        productCmptType.setConfigurationForPolicyCmptType(true);
        productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType(productCmptType.getQualifiedName());

        IProductCmpt productCmpt = newProductCmpt(productCmptType, "Product");
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();
        IPropertyValue propertyValue = generation.newValidationRuleConfig(validationRule);

        assertTrue(validationRule.isPropertyFor(propertyValue));
    }

    @Test
    public void testSetMarkers() {
        validationRule.setMarkers(Arrays.asList(new String[] { "marker1", "marker2" }));

        List<String> markers = validationRule.getMarkers();
        assertEquals(2, markers.size());
        assertTrue(markers.contains("marker1"));
        assertTrue(markers.contains("marker2"));

        validationRule.setMarkers(Arrays.asList(new String[] { "otherMarker", "marker2", "anotherMarker" }));
        markers = validationRule.getMarkers();
        assertEquals(3, markers.size());
        assertTrue(markers.contains("otherMarker"));
        assertTrue(markers.contains("marker2"));
        assertTrue(markers.contains("anotherMarker"));
    }

    @Test
    public void testGetMarkes() {
        validationRule.setMarkers(Arrays.asList(new String[] { "marker1", "marker2", "marker3" }));

        List<String> markers = validationRule.getMarkers();
        assertEquals(3, markers.size());
        assertTrue(markers.contains("marker1"));
        assertTrue(markers.contains("marker2"));
        assertTrue(markers.contains("marker3"));
    }

    @Test
    public void testValidateChangingOverTime_DoesNotReturnMessage_IfProductCmptTypeIsNull() throws CoreException {
        IPolicyCmptType policyWithoutProductCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject,
                "PolicyWithoutProductCmptType");
        policyWithoutProductCmptType.setConfigurableByProductCmptType(true);
        validationRule = policyWithoutProductCmptType.newRule();
        validationRule.setName("vRule");
        validationRule.setConfigurableByProductComponent(true);
        MessageList ml = validationRule.validate(validationRule.getIpsProject());

        assertNull(ml
                .getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testValidateChangingOverTime_DoesNotReturnMessage_ValidationRuleIsNotConfigurable()
            throws CoreException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        productCmptType.setChangingOverTime(true);
        policyCmptType.setProductCmptType("ProductType");
        validationRule.setName("name");
        validationRule.setConfigurableByProductComponent(false);

        MessageList ml = validationRule.validate(validationRule.getIpsProject());

        assertNull(ml
                .getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

    }

    @Test
    public void testValidateChangingOverTime_DoesNotReturnMessage_IfProductCmptTypeIsChangingOverTimeAndValidationRuleIsConfigurable()
            throws CoreException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        productCmptType.setChangingOverTime(true);
        policyCmptType.setProductCmptType("ProductType");
        validationRule.setName("name");
        validationRule.setConfigurableByProductComponent(true);

        MessageList ml = validationRule.validate(validationRule.getIpsProject());

        assertNull(ml
                .getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

    }

    @Test
    public void testValidateChangingOverTime_DoesNotReturnMessage_IfProductCmptTypeIsChangingOverTimeAndValidationRuleIsNotConfigurable()
            throws CoreException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        productCmptType.setChangingOverTime(true);
        policyCmptType.setProductCmptType("ProductType");
        validationRule.setName("name");
        validationRule.setConfigurableByProductComponent(false);

        MessageList ml = validationRule.validate(validationRule.getIpsProject());

        assertNull(ml
                .getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

    }

    @Test
    public void testValidateChangingOverTime_DoesNotReturnMessage_IfProductCmptTypeIsNotChangingOverTimeAndValidationRuleIsNotConfigurable()
            throws CoreException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        productCmptType.setChangingOverTime(false);
        policyCmptType.setProductCmptType("ProductType");
        validationRule.setName("name");
        validationRule.setConfigurableByProductComponent(false);

        MessageList ml = validationRule.validate(validationRule.getIpsProject());

        assertNull(ml
                .getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));

    }

    @Test
    public void testValidateChangingOverTime_ReturnMessage_IfProductCmptTypeIsNotChangingOverTimeAndValidationRuleIsNotConfigurable()
            throws CoreException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        productCmptType.setChangingOverTime(false);
        policyCmptType.setProductCmptType("ProductType");
        validationRule.setName("name");
        validationRule.setConfigurableByProductComponent(true);

        MessageList ml = validationRule.validate(validationRule.getIpsProject());

        assertNotNull(ml
                .getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

}
