/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.validation;

import static java.util.Objects.requireNonNull;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.type.PolicyAttribute;
import org.faktorips.values.NullObject;
import org.faktorips.values.ObjectUtil;
import org.faktorips.valueset.ValueSet;

/**
 * Class for validating the attribute relevance and value range.
 */
public class GenericRelevanceValidation {

    private final IModelObject modelObject;
    private final PolicyAttribute policyAttribute;
    private final IGenericAttributeValidationConfiguration config;

    public GenericRelevanceValidation(IModelObject modelObject, PolicyAttribute policyAttribute,
            IGenericAttributeValidationConfiguration config) {
        this.modelObject = requireNonNull(modelObject, "modelObject must not be null");
        this.policyAttribute = requireNonNull(policyAttribute, "policyAttribute must not be null");
        this.config = requireNonNull(config, "config must not be null");
    }

    /**
     * Checks the attribute for the following violations:
     * <ul>
     * <li>Mandatory violation: The attribute is a {@link Relevance#MANDATORY} field and the value
     * is missing.
     * <li>Irrelevant violation: The value set is empty({@link Relevance#IRRELEVANT}), so the
     * attribute cannot have a value.
     * <li>Value set violation: The attribute contains a value that is not part of the value set.
     * </ul>
     *
     * @return A {@linkplain MessageList} with the validation results
     */
    public MessageList validate() {
        MessageList messages = new MessageList();
        messages.add(validateValuePresentIfMandatory());
        messages.add(validateValueNullIfIrrelevant());
        messages.add(validateValueContainedIfPresent());
        return messages;
    }

    private Message validateValuePresentIfMandatory() {
        if (isInvalidMandatory()) {
            return config.createMessageForMissingMandatoryValue(policyAttribute, modelObject);
        }
        return null;
    }

    private Message validateValueNullIfIrrelevant() {
        if (isInvalidIrrelevance()) {
            return config.createMessageForValuePresentForIgnoredAttribute(policyAttribute, modelObject);
        }
        return null;
    }

    private Message validateValueContainedIfPresent() {
        if (isInvalidNotContained()) {
            return config.createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject);
        }
        return null;
    }

    /**
     * Indicates whether the attribute has a mandatory field violation: If the attribute is a
     * mandatory field, the value must be != {@code null}.
     */
    private boolean isInvalidMandatory() {
        return !isValuePresent() && Relevance.isMandatory(modelObject, policyAttribute);
    }

    /**
     * Indicates whether the attribute has a violation of irrelevance: If the attribute is
     * irrelevant, the value must be {@code null}.
     */
    private boolean isInvalidIrrelevance() {
        return isValuePresent() && Relevance.isIrrelevant(modelObject, policyAttribute);
    }

    /**
     * Indicates whether there is a violation of the value range for the attribute: If the attribute
     * is relevant, the value must be in the value range.
     */
    private boolean isInvalidNotContained() {
        ValueSet<Object> valueSet = getValueSet();
        return Relevance.isRelevant(modelObject, policyAttribute) && isValuePresent() && valueSet != null
                && !valueSet.contains(getValue());
    }

    /**
     * Indicates whether the value of the attribute in the {@link IModelObject} is present. A value
     * is considered not present if it is {@code null} or a {@link NullObject}. If the attribute is
     * a {@link String}, the value is also considered not present if the {@link String} is empty or
     * only contains whitespace.
     */
    private boolean isValuePresent() {
        Object value = policyAttribute.getValue(modelObject);
        return !(ObjectUtil.isNull(value)
                || (value instanceof CharSequence && IpsStringUtils.isBlank(((CharSequence)value).toString())));
    }

    private Object getValue() {
        return policyAttribute.getValue(modelObject);
    }

    @SuppressWarnings("unchecked")
    private <T> ValueSet<T> getValueSet() {
        return (ValueSet<T>)policyAttribute.getValueSet(modelObject);
    }
}
