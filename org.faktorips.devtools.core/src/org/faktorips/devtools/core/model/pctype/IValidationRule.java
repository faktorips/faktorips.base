/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.pctype;

import java.util.List;

import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;

/**
 * Represents a validation rule. Instances of this interface just say that a rule with a specified
 * name, message e.g. exist. The actual rule condition is part of the concept of this interface. It
 * has to be implemented within the generated source code.
 */
public interface IValidationRule extends IProductCmptProperty {

    public static final String PROPERTY_BUSINESS_FUNCTIONS = "businessFunctions"; //$NON-NLS-1$

    public static final String PROPERTY_APPLIED_FOR_ALL_BUSINESS_FUNCTIONS = "appliedForAllBusinessFunctions"; //$NON-NLS-1$

    public static final String PROPERTY_MESSAGE_TEXT = "messageText"; //$NON-NLS-1$

    public static final String PROPERTY_MESSAGE_CODE = "messageCode"; //$NON-NLS-1$

    public static final String PROPERTY_MESSAGE_SEVERITY = "messageSeverity"; //$NON-NLS-1$

    public static final String PROPERTY_VALIDATED_ATTRIBUTES = "validatedAttributes"; //$NON-NLS-1$

    public static final String PROPERTY_VALIDATIED_ATTR_SPECIFIED_IN_SRC = "validatedAttrSpecifiedInSrc"; //$NON-NLS-1$

    public static final String PROPERTY_CHECK_AGAINST_VALUE_SET_RULE = "checkValueAgainstValueSetRule"; //$NON-NLS-1$

    public static final String PROPERTY_CONFIGURABLE_BY_PRODUCT_COMPONENT = "configurableByProductComponent"; //$NON-NLS-1$

    public static final String PROPERTY_ACTIVATED_BY_DEFAULT = "activatedByDefault"; //$NON-NLS-1$

    public static final String PROPERTY_MARKERS = "markers"; //$NON-NLS-1$

    public static final String PROPERTY_CHANGING_OVER_TIME = "changingOverTime"; //$NON-NLS-1$

    /**
     * The separator to concatenate the key. We use the minus character because this character is
     * not allowed in names.
     */
    public static final String QNAME_SEPARATOR = "-"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "VALIDATIONRULE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the message text contains a newline.
     */
    public static final String MSGCODE_NO_NEWLINE = MSGCODE_PREFIX + "NoNewlineAllowed"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the message text contains a newline.
     */
    public static final String MSGCODE_MESSAGE_TEXT_PARAMETER_INVALID = MSGCODE_PREFIX + "InvalidTextParameter"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the message code shouldn't be empty.
     */
    public static final String MSGCODE_MSGCODE_SHOULDNT_BE_EMPTY = MSGCODE_PREFIX + "MsgCodeShouldntBeEmpty"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an attribute referenced by the rule does not exist.
     */
    public static final String MSGCODE_UNDEFINED_ATTRIBUTE = MSGCODE_PREFIX + "UndefinedAttribute"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that constant attributes can't be validated.
     */
    public static final String MSGCODE_CONSTANT_ATTRIBUTES_CANT_BE_VALIDATED = MSGCODE_PREFIX
            + "ConstantAttributesCantBeValidated"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of the rule already exists.
     */
    public static final String MSGCODE_DUPLICATE_RULE_NAME = MSGCODE_PREFIX + "DuplicateRuleName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of the rule is already used as method name
     * within the type.
     */
    public static final String MSGCODE_VALIDATION_RULE_METHOD_NAME_CONFLICT = MSGCODE_PREFIX
            + "ValidationRuleMethodNameConflict"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the marker id does not exist in the marker enum.
     */
    public static final String MSGCODE_INVALID_MARKER_ID = MSGCODE_PREFIX + "MsgInvalidMarkerID"; //$NON-NLS-1$

    /**
     * Sets the rules name.
     */
    public void setName(String newName);

    /**
     * Returns the message text that is presented to the user, if the rule determines that a given
     * policy is invalid.
     */
    public IValidationRuleMessageText getMessageText();

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

    /**
     * returns whether this rule can be configured by a product component.
     * 
     * @return <code>true</code> if this rule and its containing {@link PolicyCmptType} are both
     *         configurable, <code>false</code> else.
     */
    public boolean isConfigurableByProductComponent();

    /**
     * Sets a flag indicating whether this rule can be configured by a product component.
     * 
     */
    public void setConfigurableByProductComponent(boolean configurable);

    /**
     * returns whether this rule is activated for newly created product components.
     * 
     * @return <code>true</code> if this rule is activated by default, <code>false</code> else.
     */
    public boolean isActivatedByDefault();

    /**
     * Sets a flag indicating whether this rule is activated for newly created product components.
     * 
     */
    public void setActivatedByDefault(boolean activated);

    /**
     * Returns the qualified name of the rule. The qualified name of the rule contains the qualified
     * name of the parent object ({@link IPolicyCmptType}) and the name of the rule. The qualified
     * name of a rule is unique within a project and all dependent projects.
     * 
     * @return The qualified name of the rule.
     */
    public String getQualifiedRuleName();

    /**
     * Returns a list of markers that are applied to this rule. The markers are represented by the
     * id of an enum value.
     */
    public List<String> getMarkers();

    /**
     * Sets the list of markers.
     */
    public void setMarkers(List<String> markers);

    /**
     * Set the given value to changingOverTime property
     */
    public void setChangingOverTime(boolean changingOverTime);

}
