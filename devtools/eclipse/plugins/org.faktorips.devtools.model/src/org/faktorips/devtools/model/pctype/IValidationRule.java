/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.pctype;

import java.util.List;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IOverridableLabeledElement;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.runtime.model.type.PolicyCmptType;

/**
 * Represents a validation rule. Instances of this interface just say that a rule with a specified
 * name, message e.g. exist. The actual rule condition is part of the concept of this interface. It
 * has to be implemented within the generated source code.
 */
public interface IValidationRule extends IProductCmptProperty, IOverridableLabeledElement {

    String TAG_NAME = "ValidationRuleDef"; //$NON-NLS-1$

    String XML_TAG_MSG_TXT = "MessageText"; //$NON-NLS-1$

    String PROPERTY_MESSAGE_TEXT = "messageText"; //$NON-NLS-1$

    String PROPERTY_MESSAGE_CODE = "messageCode"; //$NON-NLS-1$

    String PROPERTY_MESSAGE_SEVERITY = "messageSeverity"; //$NON-NLS-1$

    String PROPERTY_VALIDATED_ATTRIBUTES = "validatedAttributes"; //$NON-NLS-1$

    String PROPERTY_VALIDATIED_ATTR_SPECIFIED_IN_SRC = "validatedAttrSpecifiedInSrc"; //$NON-NLS-1$

    String PROPERTY_CHECK_AGAINST_VALUE_SET_RULE = "checkValueAgainstValueSetRule"; //$NON-NLS-1$

    String PROPERTY_CONFIGURABLE_BY_PRODUCT_COMPONENT = "configurableByProductComponent"; //$NON-NLS-1$

    String PROPERTY_ACTIVATED_BY_DEFAULT = "activatedByDefault"; //$NON-NLS-1$

    String PROPERTY_MARKERS = "markers"; //$NON-NLS-1$

    String PROPERTY_CHANGING_OVER_TIME = "changingOverTime"; //$NON-NLS-1$

    String PROPERTY_OVERRIDING = "overriding"; //$NON-NLS-1$
    /**
     * The separator to concatenate the key. We use the minus character because this character is
     * not allowed in names.
     */
    String QNAME_SEPARATOR = "-"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "VALIDATIONRULE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the message text contains a newline.
     */
    String MSGCODE_NO_NEWLINE = MSGCODE_PREFIX + "NoNewlineAllowed"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the message text contains a newline.
     */
    String MSGCODE_MESSAGE_TEXT_PARAMETER_INVALID = MSGCODE_PREFIX + "InvalidTextParameter"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the message code shouldn't be empty.
     */
    String MSGCODE_MSGCODE_SHOULDNT_BE_EMPTY = MSGCODE_PREFIX + "MsgCodeShouldntBeEmpty"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an attribute referenced by the rule does not exist.
     */
    String MSGCODE_UNDEFINED_ATTRIBUTE = MSGCODE_PREFIX + "UndefinedAttribute"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the validation rule is marked overwriting a
     * validation rule in the super type hierarchy, but there is no such rule.
     */
    String MSGCODE_NOTHING_TO_OVERWRITE = MSGCODE_PREFIX + "NothingToOverwrite"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a rule overwrites another but change over time
     * configuration differs
     */
    String MSGCODE_OVERWRITTEN_RULE_HAS_DIFFERENT_CHANGE_OVER_TIME = MSGCODE_PREFIX
            + "OverwrittenRuleDifferentChangeOverTime"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that constant attributes can't be validated.
     */
    String MSGCODE_CONSTANT_ATTRIBUTES_CANT_BE_VALIDATED = MSGCODE_PREFIX
            + "ConstantAttributesCantBeValidated"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of the rule already exists.
     */
    String MSGCODE_DUPLICATE_RULE_NAME = MSGCODE_PREFIX + "DuplicateRuleName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of the rule is already used as method name
     * within the type.
     */
    String MSGCODE_VALIDATION_RULE_METHOD_NAME_CONFLICT = MSGCODE_PREFIX
            + "ValidationRuleMethodNameConflict"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the marker id does not exist in the marker enum.
     */
    String MSGCODE_INVALID_MARKER_ID = MSGCODE_PREFIX + "MsgInvalidMarkerID"; //$NON-NLS-1$

    /**
     * Sets the rules name.
     */
    void setName(String newName);

    /**
     * Returns the message text that is presented to the user, if the rule determines that a given
     * policy is invalid.
     */
    IValidationRuleMessageText getMessageText();

    /**
     * Returns the code of the message that is generated, if the rule determines that a given policy
     * is invalid.
     */
    String getMessageCode();

    /**
     * Sets the code of the message that is generated, if the rule determines that a given policy is
     * invalid.
     */
    void setMessageCode(String newCode);

    /**
     * Returns the message's severity.
     */
    MessageSeverity getMessageSeverity();

    /**
     * Sets the message's severity.
     */
    void setMessageSeverity(MessageSeverity newSeverity);

    /**
     * Adds the name of an attribute that is evaluated in the rules condition.
     *
     * @param attributeName the name of the validated attribute
     * @return returns the name of the validated attribute
     */
    String addValidatedAttribute(String attributeName);

    /**
     * Sets the name of a validated attribute at the indexed position. Throws a runtime exception is
     * the position does not exist.
     *
     * @param index the position where the provided attribute name is stored
     * @param attributeName the validated attributes name
     */
    void setValidatedAttributeAt(int index, String attributeName);

    /**
     * Removes the validated attribute name at the given position.
     *
     * @param index the array position at which the attribute name is stored
     */
    void removeValidatedAttribute(int index);

    /**
     * Returns the names of the attributes that are part of the rule's condition.
     */
    String[] getValidatedAttributes();

    /**
     * Returns the validated attribute at the indexed position.
     *
     * @param index the position of the requested validated attribute
     * @return the validated attribute name
     */
    String getValidatedAttributeAt(int index);

    /**
     * Returns if the validated attributes are specified in the source code instead of this rule.
     */
    boolean isValidatedAttrSpecifiedInSrc();

    /**
     * Sets the specifiedInSourceCode flag that indicates if the validated attributes are specified
     * in the source code instead of this rule.
     */
    void setValidatedAttrSpecifiedInSrc(boolean validatedAttrSpecifiedInSrc);

    /**
     * Returns <code>true</code> if this rule is a default rule for validating the value of an
     * attribute against the value set defined for the attribute. Default means, that the rule is
     * not a manually build rule - it is an automatically created rule. The creation of this rule
     * has to be allowed by the user.
     */
    boolean isCheckValueAgainstValueSetRule();

    /**
     * Sets the isAttributeValueValidationRule flag that indicates whether this is a default rule or
     * not.
     *
     * @see #isCheckValueAgainstValueSetRule()
     */
    void setCheckValueAgainstValueSetRule(boolean isCheckValueAgainstValueSetRule);

    /**
     * returns whether this rule can be configured by a product component.
     *
     * @return <code>true</code> if this rule and its containing {@link PolicyCmptType} are both
     *             configurable, <code>false</code> else.
     */
    boolean isConfigurableByProductComponent();

    /**
     * Sets a flag indicating whether this rule can be configured by a product component.
     *
     */
    void setConfigurableByProductComponent(boolean configurable);

    /**
     * returns whether this rule is activated for newly created product components.
     *
     * @return <code>true</code> if this rule is activated by default, <code>false</code> else.
     */
    boolean isActivatedByDefault();

    /**
     * Sets a flag indicating whether this rule is activated for newly created product components.
     *
     */
    void setActivatedByDefault(boolean activated);

    /**
     * Returns the qualified name of the rule. The qualified name of the rule contains the qualified
     * name of the parent object ({@link IPolicyCmptType}) and the name of the rule. The qualified
     * name of a rule is unique within a project and all dependent projects.
     *
     * @return The qualified name of the rule.
     */
    String getQualifiedRuleName();

    /**
     * Returns a list of markers that are applied to this rule. The markers are represented by the
     * id of an enum value.
     */
    List<String> getMarkers();

    /**
     * Sets the list of markers.
     */
    void setMarkers(List<String> markers);

    /**
     * Set the given value to changingOverTime property
     */
    void setChangingOverTime(boolean changingOverTime);

    /**
     * Returns <code>true</code> if this validation rule overrides the
     * <code>otherValidationRule</code>. This rule could override a rule of any super type of this
     * rule's type, so <code>this.getType</code> must be a sub type of
     * <code>otherValidationRule.getType</code>.
     *
     * @param otherValidationRule The validation rule that overrides this one.
     */
    boolean overrides(IValidationRule otherValidationRule);

    /**
     * Returns <code>true</code> if this validation rule is marked to override a validation rule
     * with the same name somewhere up the supertype hierarchy, <code>false</code> otherwise.
     */
    @Override
    boolean isOverriding();

    /**
     * <code>true</code> to indicate that this validation rule overwrites a validation rule with the
     * same name somewhere up the super type hierarchy or <code>false</code> to let this validation
     * rule be a new one.
     */
    void setOverriding(boolean overriding);

    /**
     * Returns the first validation rule found with the same name in the super types hierarchy or
     * <code>null</code> if no such validation rule exists.
     *
     * @param ipsProject The project which IPS object path is used to search.
     *
     * @throws IpsException if an error occurs while searching.
     */
    IValidationRule findOverwrittenValidationRule(IIpsProject ipsProject) throws IpsException;
}
