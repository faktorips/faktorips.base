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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.MessageSeverity;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ValidationRule extends AtomicIpsObjectPart implements IValidationRule {

    final static String TAG_NAME = "ValidationRuleDef"; //$NON-NLS-1$

    private String msgText = ""; //$NON-NLS-1$

    private String msgCode = ""; //$NON-NLS-1$

    private List<String> validatedAttributes = new ArrayList<String>();

    private MessageSeverity msgSeverity = MessageSeverity.ERROR;

    // the qualified name of the business functions this rule is used in
    private ArrayList<String> functions = new ArrayList<String>(0);

    private boolean appliedForAllBusinessFunction = true;

    private boolean validatedAttrSpecifiedInSrc = false;

    /**
     * Flag which is <code>true</code> if this rule is a default rule for validating the value of an
     * attribute against the value set defined for the attribute. Default means, that the rule is
     * not a manually build rule - it is an automatically created rule. The creation of this rule
     * has to be allowed by the user.
     */
    private boolean checkValueAgainstValueSetRule = false;

    /**
     * Creates a new validation rule definition.
     * 
     * @param pcType The type the rule belongs to.
     * @param id The rule's unique id within the type.
     */
    public ValidationRule(IPolicyCmptType pcType, String id) {
        super(pcType, id);
    }

    /**
     * Constructor for testing purposes.
     */
    public ValidationRule() {

    }

    @Override
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        valueChanged(oldName, newName);
    }

    public String[] getBusinessFunctions() {
        return functions.toArray(new String[functions.size()]);
    }

    public void setBusinessFunctions(String[] functionNames) {
        functions.clear();
        for (int i = 0; i < functionNames.length; i++) {
            functions.add(functionNames[i]);
        }
        objectHasChanged();
    }

    public int getNumOfBusinessFunctions() {
        return functions.size();
    }

    public void addBusinessFunction(String functionName) {
        ArgumentCheck.notNull(functionName);
        functions.add(functionName);
        objectHasChanged();
    }

    public void removeBusinessFunction(int index) {
        functions.remove(index);
        objectHasChanged();
    }

    public String getBusinessFunction(int index) {
        return functions.get(index);
    }

    public void setBusinessFunctions(int index, String functionName) {
        ArgumentCheck.notNull(functionName);
        String oldName = getBusinessFunction(index);
        functions.set(index, functionName);
        valueChanged(oldName, functionName);
    }

    public boolean isAppliedForAllBusinessFunctions() {
        return appliedForAllBusinessFunction;
    }

    public void setAppliedForAllBusinessFunctions(boolean newValue) {
        boolean oldValue = appliedForAllBusinessFunction;
        appliedForAllBusinessFunction = newValue;
        valueChanged(oldValue, newValue);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        ValidationUtils.checkStringPropertyNotEmpty(name, "name", this, //$NON-NLS-1$
                PROPERTY_NAME, "", list); //$NON-NLS-1$
        if (StringUtils.isEmpty(msgCode)) {
            String text = Messages.ValidationRule_msgCodeShouldBeProvided;
            Message msg = new Message(IValidationRule.MSGCODE_MSGCODE_SHOULDNT_BE_EMPTY, text, Message.ERROR, this,
                    PROPERTY_MESSAGE_CODE);
            list.add(msg);
        }

        IIpsProject project = getIpsProject();
        validateBusinessFunctions(list, project);

        if (SystemUtils.LINE_SEPARATOR != null && SystemUtils.LINE_SEPARATOR.length() > 0
                && msgText.indexOf(SystemUtils.LINE_SEPARATOR) != -1) {
            list.add(new Message(IValidationRule.MSGCODE_NO_NEWLINE, Messages.ValidationRule_msgNoNewlineAllowed,
                    Message.ERROR, this, IValidationRule.PROPERTY_MESSAGE_TEXT));
        }

        validateValidatedAttribute(list, ipsProject);
        validateCheckValueAgainstValueSet(list);
    }

    private void validateBusinessFunctions(MessageList list, IIpsProject ipsProject) throws CoreException {
        for (int i = 0; i < functions.size(); i++) {
            String function = functions.get(i);
            if (StringUtils.isNotEmpty(function)) {
                if (ipsProject.findIpsObject(IpsObjectType.BUSINESS_FUNCTION, function) == null) {
                    String text = NLS.bind(Messages.ValidationRule_msgFunctionNotExists, function);
                    list.add(new Message("", text, Message.ERROR, //$NON-NLS-1$
                            new ObjectProperty(this, IValidationRule.PROPERTY_BUSINESS_FUNCTIONS, i)));
                } else {
                    if (isAppliedForAllBusinessFunctions()) {
                        String text = Messages.ValidationRule_msgIgnored;
                        list.add(new Message("", text, Message.WARNING, //$NON-NLS-1$
                                new ObjectProperty(this, IValidationRule.PROPERTY_BUSINESS_FUNCTIONS, i)));
                    }
                }
            }
        }
        if (!isAppliedForAllBusinessFunctions() && functions.isEmpty()) {
            String text = Messages.ValidationRule_msgOneBusinessFunction;
            list.add(new Message(
                    "", text, Message.ERROR, this, IValidationRule.PROPERTY_APPLIED_FOR_ALL_BUSINESS_FUNCTIONS)); //$NON-NLS-1$
        }
    }

    private void validateCheckValueAgainstValueSet(MessageList msgList) {

        if (isCheckValueAgainstValueSetRule()) {
            String attributeName = getValidatedAttributeAt(0);
            IPolicyCmptTypeAttribute attribute = getPolicyCmptType().getPolicyCmptTypeAttribute(attributeName);
            if (attribute == null) {
                return;
            }
            if (ValueSetType.UNRESTRICTED.equals(attribute.getValueSet().getValueSetType())) {
                String text = Messages.ValidationRule_msgValueSetRule;
                msgList.add(new Message(
                        "", text, Message.ERROR, this, IValidationRule.PROPERTY_CHECK_AGAINST_VALUE_SET_RULE)); //$NON-NLS-1$
            }
        }
    }

    private PolicyCmptType getPolicyCmptType() {
        return (PolicyCmptType)getIpsObject();
    }

    private void validateValidatedAttribute(MessageList list, IIpsProject ipsProject) throws CoreException {

        IPolicyCmptTypeAttribute[] attributes = getPolicyCmptType().getSupertypeHierarchy().getAllAttributes(
                getPolicyCmptType());
        Set<String> attributeNames = new HashSet<String>(attributes.length);
        for (int i = 0; i < attributes.length; i++) {
            attributeNames.add(attributes[i].getName());
        }
        for (int i = 0; i < validatedAttributes.size(); i++) {
            String validatedAttribute = validatedAttributes.get(i);
            if (!attributeNames.contains(validatedAttribute)) {
                String text = Messages.ValidationRule_msgUndefinedAttribute;
                list.add(new Message(MSGCODE_CONSTANT_ATTRIBUTES_CANT_BE_VALIDATED, text, Message.ERROR,
                        new ObjectProperty(this, "validatedAttributes", i))); //$NON-NLS-1$
            } else {
                IPolicyCmptTypeAttribute attribute = getPolicyCmptType().findPolicyCmptTypeAttribute(
                        validatedAttribute, ipsProject);
                if (attribute.getAttributeType() == AttributeType.CONSTANT) {
                    list.add(new Message(IValidationRule.MSGCODE_CONSTANT_ATTRIBUTES_CANT_BE_VALIDATED,
                            Messages.ValidationRule_ConstantAttributesCantBeValidated, Message.ERROR,
                            new ObjectProperty(this, "validatedAttributes", i))); //$NON-NLS-1$
                }
            }
        }

        for (int i = 0; i < validatedAttributes.size() - 1; i++) {

            for (int r = i + 1; r < validatedAttributes.size(); r++) {
                if (validatedAttributes.get(i).equals(validatedAttributes.get(r))) {
                    String text = Messages.ValidationRule_msgDuplicateEntries;
                    list.add(new Message("", text, Message.WARNING, //$NON-NLS-1$
                            new ObjectProperty[] { new ObjectProperty(this, "validatedAttributes", i), //$NON-NLS-1$
                                    new ObjectProperty(this, "validatedAttributes", r) })); //$NON-NLS-1$
                }
            }
        }
    }

    public String getMessageText() {
        return msgText;
    }

    public void setMessageText(String newText) {
        String oldText = msgText;
        msgText = newText;
        valueChanged(oldText, msgText);
    }

    public String getMessageCode() {
        return msgCode;
    }

    public void setMessageCode(String newCode) {
        String oldCode = msgCode;
        msgCode = newCode;
        valueChanged(oldCode, msgCode);
    }

    public MessageSeverity getMessageSeverity() {
        return msgSeverity;
    }

    public void setMessageSeverity(MessageSeverity newSeverity) {
        MessageSeverity oldSeverity = msgSeverity;
        msgSeverity = newSeverity;
        valueChanged(oldSeverity, msgSeverity);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
        appliedForAllBusinessFunction = Boolean.valueOf(
                element.getAttribute(PROPERTY_APPLIED_FOR_ALL_BUSINESS_FUNCTIONS)).booleanValue();
        msgCode = element.getAttribute(PROPERTY_MESSAGE_CODE);
        msgText = element.getAttribute(PROPERTY_MESSAGE_TEXT);
        msgSeverity = MessageSeverity.getMessageSeverity(element.getAttribute(PROPERTY_MESSAGE_SEVERITY));
        checkValueAgainstValueSetRule = Boolean.valueOf(element.getAttribute(PROPERTY_CHECK_AGAINST_VALUE_SET_RULE))
                .booleanValue();
        validatedAttrSpecifiedInSrc = Boolean.valueOf(element.getAttribute(PROPERTY_VALIDATIED_ATTR_SPECIFIED_IN_SRC))
                .booleanValue();

        NodeList nl = element.getChildNodes();
        functions.clear();
        validatedAttributes.clear();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element subElement = (Element)nl.item(i);
                if (subElement.getNodeName().equals("BusinessFunction")) { //$NON-NLS-1$
                    functions.add(subElement.getAttribute("name"));
                }
                if (subElement.getNodeName().equals("ValidatedAttribute")) { //$NON-NLS-1$
                    validatedAttributes.add(subElement.getAttribute("name"));
                }
            }
        }
        functions.trimToSize();
    }

    @Override
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_NAME, name);
        newElement.setAttribute(PROPERTY_APPLIED_FOR_ALL_BUSINESS_FUNCTIONS, String
                .valueOf(appliedForAllBusinessFunction));
        newElement.setAttribute(PROPERTY_MESSAGE_CODE, msgCode);
        newElement.setAttribute(PROPERTY_MESSAGE_TEXT, msgText);
        newElement.setAttribute(PROPERTY_MESSAGE_SEVERITY, msgSeverity.getId());
        newElement.setAttribute(PROPERTY_VALIDATIED_ATTR_SPECIFIED_IN_SRC, String.valueOf(validatedAttrSpecifiedInSrc));
        newElement.setAttribute(PROPERTY_CHECK_AGAINST_VALUE_SET_RULE, String.valueOf(checkValueAgainstValueSetRule));
        Document doc = newElement.getOwnerDocument();
        for (int i = 0; i < functions.size(); i++) {
            Element fctElement = doc.createElement("BusinessFunction"); //$NON-NLS-1$
            fctElement.setAttribute("name", functions.get(i));
            newElement.appendChild(fctElement);
        }
        for (int i = 0; i < validatedAttributes.size(); i++) {
            Element attrElement = doc.createElement("ValidatedAttribute"); //$NON-NLS-1$
            attrElement.setAttribute("name", validatedAttributes //$NON-NLS-1$
                    .get(i));
            newElement.appendChild(attrElement);
        }
    }

    public String addValidatedAttribute(String attributeName) {
        ArgumentCheck.notNull(this, attributeName);
        validatedAttributes.add(attributeName);
        objectHasChanged();
        return attributeName;
    }

    public String[] getValidatedAttributes() {
        return validatedAttributes.toArray(new String[validatedAttributes.size()]);
    }

    public void removeValidatedAttribute(int index) {
        validatedAttributes.remove(index);
        objectHasChanged();
    }

    public String getValidatedAttributeAt(int index) {
        return validatedAttributes.get(index);
    }

    public void setValidatedAttributeAt(int index, String attributeName) {
        String oldValue = getValidatedAttributeAt(index);
        validatedAttributes.set(index, attributeName);
        valueChanged(oldValue, attributeName);
    }

    public boolean isValidatedAttrSpecifiedInSrc() {
        return validatedAttrSpecifiedInSrc;
    }

    public void setValidatedAttrSpecifiedInSrc(boolean validatedAttrSpecifiedInSrc) {
        boolean oldValue = this.validatedAttrSpecifiedInSrc;
        this.validatedAttrSpecifiedInSrc = validatedAttrSpecifiedInSrc;
        valueChanged(oldValue, validatedAttrSpecifiedInSrc);
    }

    public boolean isCheckValueAgainstValueSetRule() {
        return checkValueAgainstValueSetRule;
    }

    public void setCheckValueAgainstValueSetRule(boolean isAttributeValueValidationRule) {
        boolean oldValue = isCheckValueAgainstValueSetRule();
        checkValueAgainstValueSetRule = isAttributeValueValidationRule;
        valueChanged(oldValue, isAttributeValueValidationRule);

    }

}
