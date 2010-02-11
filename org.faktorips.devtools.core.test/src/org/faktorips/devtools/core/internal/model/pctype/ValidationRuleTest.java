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

package org.faktorips.devtools.core.internal.model.pctype;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.MessageSeverity;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class ValidationRuleTest extends AbstractIpsPluginTest {

    private PolicyCmptType pcType;
    private IIpsSrcFile ipsSrcFile;
    private IValidationRule rule;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject("TestProject");
        pcType = newPolicyCmptType(project, "Policy");
        ipsSrcFile = pcType.getIpsSrcFile();
        rule = pcType.newRule();
        ipsSrcFile.save(true, null);
        assertFalse(ipsSrcFile.isDirty());
    }

    public void testRemove() {
        rule.delete();
        assertEquals(0, pcType.getRules().length);
        assertTrue(ipsSrcFile.isDirty());
    }

    public void testSetName() {
        rule.setName("newName");
        assertEquals("newName", rule.getName());
        assertTrue(ipsSrcFile.isDirty());
    }

    public void testSetMessageText() {
        rule.setMessageText("newText");
        assertEquals("newText", rule.getMessageText());
        assertTrue(ipsSrcFile.isDirty());
    }

    public void testAddValidatedAttribute() {
        rule.addValidatedAttribute("a");
        rule.addValidatedAttribute("b");
        assertEquals("a", rule.getValidatedAttributes()[0]);
        assertEquals("b", rule.getValidatedAttributes()[1]);
        assertTrue(ipsSrcFile.isDirty());
    }

    public void testSetValidatedAttributeAt() {
        rule.addValidatedAttribute("a");
        rule.addValidatedAttribute("b");
        rule.setValidatedAttributeAt(1, "c");
        assertEquals("c", rule.getValidatedAttributes()[1]);
        assertTrue(ipsSrcFile.isDirty());
    }

    public void testGetValidatedAttributeAt() {
        rule.addValidatedAttribute("a");
        rule.addValidatedAttribute("b");
        assertEquals("a", rule.getValidatedAttributeAt(0));
        assertEquals("b", rule.getValidatedAttributeAt(1));
    }

    public void testRemoveValidatedAttribute() {
        rule.addValidatedAttribute("a");
        rule.addValidatedAttribute("b");
        rule.removeValidatedAttribute(0);
        assertEquals("b", rule.getValidatedAttributeAt(0));
    }

    public void testValidatedAttrSpecifiedInSrc() {
        assertFalse(ipsSrcFile.isDirty());
        rule.setValidatedAttrSpecifiedInSrc(true);
        assertTrue(ipsSrcFile.isDirty());
        assertEquals(true, rule.isValidatedAttrSpecifiedInSrc());
    }

    public void testInitFromXml() {
        Document doc = getTestDocument();
        rule.setAppliedForAllBusinessFunctions(true);
        rule.initFromXml(doc.getDocumentElement());
        assertEquals("42", rule.getId());
        assertEquals("checkAge", rule.getName());
        assertEquals("blabla", rule.getDescription());
        assertEquals("ageMissing", rule.getMessageCode());
        assertEquals("messageText", rule.getMessageText());
        assertEquals(MessageSeverity.WARNING, rule.getMessageSeverity());
        assertFalse(rule.isAppliedForAllBusinessFunctions());
        String[] functions = rule.getBusinessFunctions();
        assertEquals(2, functions.length);
        assertEquals("NewOffer", functions[0]);
        assertEquals("Renewal", functions[1]);
        String[] validatedAttributes = rule.getValidatedAttributes();
        assertEquals("a", validatedAttributes[0]);
        assertEquals("b", validatedAttributes[1]);
    }

    /*
     * Class under test for Element toXml(Document)
     */
    public void testToXmlDocument() {
        rule = pcType.newRule(); // => id=1 because it's the second rule
        rule.setName("checkAge");
        rule.setAppliedForAllBusinessFunctions(true);
        rule.setDescription("blabla");
        rule.setMessageCode("ageMissing");
        rule.setMessageText("messageText");
        rule.setMessageSeverity(MessageSeverity.WARNING);
        rule.setBusinessFunctions(new String[] { "NewOffer", "Renewal" });
        rule.addValidatedAttribute("a");
        rule.setCheckValueAgainstValueSetRule(true);

        Element element = rule.toXml(newDocument());

        ValidationRule copy = new ValidationRule();
        copy.initFromXml(element);
        assertEquals(rule.getId(), copy.getId());
        assertEquals("checkAge", copy.getName());
        assertEquals("blabla", copy.getDescription());
        assertEquals("ageMissing", copy.getMessageCode());
        assertEquals("messageText", copy.getMessageText());
        assertEquals(MessageSeverity.WARNING, copy.getMessageSeverity());
        assertTrue(copy.isAppliedForAllBusinessFunctions());
        String[] functions = copy.getBusinessFunctions();
        assertEquals(2, functions.length);
        assertEquals("NewOffer", functions[0]);
        assertEquals("Renewal", functions[1]);
        String[] validationAttributes = copy.getValidatedAttributes();
        assertEquals("a", validationAttributes[0]);
        assertTrue(copy.isCheckValueAgainstValueSetRule());
    }

    public void testAddBusinessFunction() {
        rule.addBusinessFunction("f1");
        assertEquals(1, rule.getNumOfBusinessFunctions());
        assertEquals("f1", rule.getBusinessFunction(0));

        rule.addBusinessFunction("f2");
        assertEquals(2, rule.getNumOfBusinessFunctions());
        assertEquals("f2", rule.getBusinessFunction(1));
    }

    public void testSetBusinessFunction() {
        rule.addBusinessFunction("f1");
        rule.addBusinessFunction("f2");

        rule.setBusinessFunctions(1, "changed");
        assertEquals("changed", rule.getBusinessFunction(1));
    }

    public void testRemoveBusinessFunction() {
        rule.addBusinessFunction("f1");
        rule.addBusinessFunction("f2");
        rule.addBusinessFunction("f3");
        rule.addBusinessFunction("f4");

        rule.removeBusinessFunction(3);
        rule.removeBusinessFunction(1);
        assertEquals(2, rule.getNumOfBusinessFunctions());
        assertEquals("f1", rule.getBusinessFunction(0));
        assertEquals("f3", rule.getBusinessFunction(1));
    }

    public void testValidate() throws Exception {
        rule.addValidatedAttribute("a");

        // validation is expected to fail because the specified attribute doesn't exist for the
        // PolicyCmptType
        MessageList messageList = rule.validate(ipsSrcFile.getIpsProject()).getMessagesFor(rule, "validatedAttributes");
        assertEquals(1, messageList.getNoOfMessages());

        IPolicyCmptTypeAttribute attr = pcType.newPolicyCmptTypeAttribute();
        attr.setName("a");
        attr.setAttributeType(AttributeType.CHANGEABLE);
        attr.setDatatype("String");

        messageList = rule.validate(ipsSrcFile.getIpsProject()).getMessagesFor(rule, "validatedAttributes");
        assertEquals(0, messageList.getNoOfMessages());

        // validation is expected to fail because of duplicate attribute entries
        rule.addValidatedAttribute("a");
        messageList = rule.validate(ipsSrcFile.getIpsProject()).getMessagesFor(rule, "validatedAttributes");
        assertEquals(1, messageList.getNoOfMessages());
    }

    public void testValidateBusinessFunctions() throws CoreException {
        rule.setAppliedForAllBusinessFunctions(true);
        MessageList msgList = rule.validate(ipsSrcFile.getIpsProject());
        msgList = msgList.getMessagesFor(rule, IValidationRule.PROPERTY_APPLIED_FOR_ALL_BUSINESS_FUNCTIONS);
        assertTrue(msgList.isEmpty());

        rule.setAppliedForAllBusinessFunctions(false);
        msgList = rule.validate(ipsSrcFile.getIpsProject());
        msgList = msgList.getMessagesFor(rule, IValidationRule.PROPERTY_APPLIED_FOR_ALL_BUSINESS_FUNCTIONS);
        assertFalse(msgList.isEmpty());

        rule.setAppliedForAllBusinessFunctions(false);
        rule.addBusinessFunction("function");
        msgList = rule.validate(ipsSrcFile.getIpsProject());
        msgList = msgList.getMessagesFor(rule, IValidationRule.PROPERTY_APPLIED_FOR_ALL_BUSINESS_FUNCTIONS);
        assertTrue(msgList.isEmpty());
    }

    public void testValidateMsgCodeShouldntBeNull() throws CoreException {
        rule.setMessageCode(null);
        MessageList list = rule.validate(ipsSrcFile.getIpsProject());
        assertNotNull(list.getMessageByCode(IValidationRule.MSGCODE_MSGCODE_SHOULDNT_BE_EMPTY));
        rule.setMessageCode("");
        list = rule.validate(ipsSrcFile.getIpsProject());
        assertNotNull(list.getMessageByCode(IValidationRule.MSGCODE_MSGCODE_SHOULDNT_BE_EMPTY));

        rule.setMessageCode("code");
        list = rule.validate(ipsSrcFile.getIpsProject());
        assertNull(list.getMessageByCode(IValidationRule.MSGCODE_MSGCODE_SHOULDNT_BE_EMPTY));
    }

    /**
     * Tests for the correct type of excetion to be thrown - no part of any type could ever be
     * created.
     */
    public void testNewPart() {
        try {
            rule.newPart(PolicyCmptTypeAttribute.class);
            fail();
        } catch (IllegalArgumentException e) {
            // nothing to do :-)
        }
    }

    public void testValidateMessageText() throws Exception {
        rule.setMessageText("Messagetext " + SystemUtils.LINE_SEPARATOR + " bla bla");
        MessageList ml = rule.validate(ipsSrcFile.getIpsProject());
        assertNotNull(ml.getMessageByCode(IValidationRule.MSGCODE_NO_NEWLINE));

        rule.setMessageText("Messagetext  bla bla");
        ml = rule.validate(ipsSrcFile.getIpsProject());
        assertNull(ml.getMessageByCode(IValidationRule.MSGCODE_NO_NEWLINE));
    }

    public void testConstantAttributesCantBeValidated() throws CoreException {
        IPolicyCmptTypeAttribute a = pcType.newPolicyCmptTypeAttribute();
        a.setName("a1");
        a.setAttributeType(AttributeType.CONSTANT);
        rule.addValidatedAttribute("a1");
        assertNotNull(rule.validate(ipsSrcFile.getIpsProject()).getMessageByCode(
                IValidationRule.MSGCODE_CONSTANT_ATTRIBUTES_CANT_BE_VALIDATED));

        a.setAttributeType(AttributeType.CHANGEABLE);
        assertNull(rule.validate(ipsSrcFile.getIpsProject()).getMessageByCode(
                IValidationRule.MSGCODE_CONSTANT_ATTRIBUTES_CANT_BE_VALIDATED));
    }
}
