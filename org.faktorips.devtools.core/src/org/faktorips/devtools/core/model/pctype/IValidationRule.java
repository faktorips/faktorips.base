/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.pctype;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * Represents a validation rule. Instances of this interface just say that a rule with a specified
 * name, message e.g. exist. The actual rule condition is part of the concept of this interface. It
 * has to be implemented within the generated source code.
 */
public interface IValidationRule extends IIpsObjectPart {

    public final static String PROPERTY_BUSINESS_FUNCTIONS = "businessFunctions"; //$NON-NLS-1$

    public final static String PROPERTY_APPLIED_FOR_ALL_BUSINESS_FUNCTIONS = "appliedForAllBusinessFunctions"; //$NON-NLS-1$

    public final static String PROPERTY_MESSAGE_TEXT = "messageText"; //$NON-NLS-1$

    public final static String PROPERTY_MESSAGE_CODE = "messageCode"; //$NON-NLS-1$

    public final static String PROPERTY_MESSAGE_SEVERITY = "messageSeverity"; //$NON-NLS-1$

    public final static String PROPERTY_VALIDATED_ATTRIBUTES = "validatedAttributes"; //$NON-NLS-1$

    public final static String PROPERTY_VALIDATIED_ATTR_SPECIFIED_IN_SRC = "validatedAttrSpecifiedInSrc"; //$NON-NLS-1$

    public final static String PROPERTY_CHECK_AGAINST_VALUE_SET_RULE = "checkValueAgainstValueSetRule"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "VALIDATIONRULE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the message text contains a newline.
     */
    public final static String MSGCODE_NO_NEWLINE = MSGCODE_PREFIX + "NoNewlineAllowed"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the message code shouldn't be empty.
     */
    public final static String MSGCODE_MSGCODE_SHOULDNT_BE_EMPTY = MSGCODE_PREFIX + "MsgCodeShouldntBeEmpty"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an attribute referenced by the rule does not exist.
     */
    public final static String MSGCODE_UNDEFINED_ATTRIBUTE = MSGCODE_PREFIX + "UndefinedAttribute"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that constant attributes can't be validated.
     */
    public final static String MSGCODE_CONSTANT_ATTRIBUTES_CANT_BE_VALIDATED = MSGCODE_PREFIX
            + "ConstantAttributesCantBeValidated"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of the rule already exists.
     */
    public final static String MSGCODE_DUPLICATE_RULE_NAME = MSGCODE_PREFIX + "DuplicateRuleName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of the rule is already used as method name
     * within the type.
     */
    public final static String MSGCODE_VALIDATION_RULE_METHOD_NAME_COLLISION = MSGCODE_PREFIX
            + "ValidationRuleMethodNameCollision"; //$NON-NLS-1$

    /**
     * Sets the rules name.
     */
    public void setName(String newName);

    /**
     * Returns the message text that is presented to the user, if the rule determines that a given
     * policy is invalid.
     */
    public String getMessageText();

    /**
     * Sets the message text, that is presented to the user, if the rule determines that a given
     * policy is invalid.
     */
    public void setMessageText(String newText);

    /**
     * Returns the code of the message that is generated, if the rule determines that a given policy
     * is invalid.
     */
    public String getMessageCode();

    /**
     * Sets the code of the message that is generated, if the rule determines that a given policy is
     * invalid.
     */
    public void setMessageCode(String newCode);

    /**
     * Returns the message's severity.
     */
    public MessageSeverity getMessageSeverity();

    /**
     * Sets the message's severity.
     */
    public void setMessageSeverity(MessageSeverity newSeverity);

    /**
     * Returns the qualified name of the business functions in that the rule is applied.
     */
    public String[] getBusinessFunctions();

    /**
     * Returns the number of business functions in which the rule is applied.
     */
    public int getNumOfBusinessFunctions();

    /**
     * Sets the qualified name of the business functions in that the rule is applied.
     */
    public void setBusinessFunctions(String[] functionNames);

    /**
     * Returns the business function name at the given index.
     * 
     * @throws IndexOutOfBoundsException if the index is invalid.
     */
    public String getBusinessFunction(int index);

    /**
     * Sets the business function at the specified index.
     * 
     * @throws IndexOutOfBoundsException if the index is invalid.
     */
    public void setBusinessFunctions(int index, String functionName);

    /**
     * Adds a new business function with the given name.
     */
    public void addBusinessFunction(String functionName);

    /**
     * Removes the business functions at the specified indices.
     */
    public void removeBusinessFunction(int index);

    /**
     * Returns true if the rule is to be applied for all business functions.
     */
    public boolean isAppliedForAllBusinessFunctions();

    /**
     * Sets if the rule should be applied for all business functions.
     */
    public void setAppliedForAllBusinessFunctions(boolean newValue);

    /**
     * Adds the name of an attribute that is evaluated in the rules condition.
     * 
     * @param attributeName the name of the validated attribute
     * @return returns the name of the validated attribute
     */
    public String addValidatedAttribute(String attributeName);

    /**
     * Sets the name of a validated attribute at the indexed position. Throws a runtime exception is
     * the position does not exist.
     * 
     * @param index the position where the provided attribute name is stored
     * @param attributeName the validated attributes name
     */
    public void setValidatedAttributeAt(int index, String attributeName);

    /**
     * Removes the validated attribute name at the given position.
     * 
     * @param index the array position at which the attribute name is stored
     */
    public void removeValidatedAttribute(int index);

    /**
     * Returns the names of the attributes that are part of the rule's condition.
     */
    public String[] getValidatedAttributes();

    /**
     * Returns the validated attribute at the indexed position.
     * 
     * @param index the position of the requested validated attribute
     * @return the validated attribute name
     */
    public String getValidatedAttributeAt(int index);

    /**
     * Returns if the validated attributes are specified in the source code instead of this rule.
     */
    public boolean isValidatedAttrSpecifiedInSrc();

    /**
     * Sets the specifiedInSourceCode flag that indicates if the validated attributes are specified
     * in the source code instead of this rule.
     */
    public void setValidatedAttrSpecifiedInSrc(boolean validatedAttrSpecifiedInSrc);

    /**
     * Returns <code>true</code> if this rule is a default rule for validating the value of an
     * attribute against the value set defined for the attribute. Default means, that the rule is
     * not a manually build rule - it is an automatically created rule. The creation of this rule
     * has to be allowed by the user.
     */
    public boolean isCheckValueAgainstValueSetRule();

    /**
     * Sets the isAttributeValueValidationRule flag that indicates whether this is a default rule or
     * not.
     * 
     * @see #isCheckValueAgainstValueSetRule()
     */
    public void setCheckValueAgainstValueSetRule(boolean isCheckValueAgainstValueSetRule);

}
