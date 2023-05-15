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

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.model.type.PolicyAttribute;
import org.faktorips.valueset.ValueSet;

/**
 * Configuration for the generic validation of {@link PolicyAttribute} values. Provides specific
 * messages for common validation errors.
 */
public interface IGenericAttributeValidationConfiguration {

    /**
     * Decides whether the given {@link PolicyAttribute} should be validated for the given
     * {@link IModelObject}.
     *
     * @implSpec Implementers should use this to prevent validation of fields that can not yet be
     *               valid, for example because another field must first be set to derive their
     *               value set.
     *
     * @param policyAttribute the model type reference for the validated attribute
     * @param modelObject the model object instance on which the attribute may be validated
     * @return whether validation for the given combination of {@link PolicyAttribute} and
     *             {@link IModelObject} should run
     */
    boolean shouldValidate(PolicyAttribute policyAttribute, IModelObject modelObject);

    /**
     * Creates a message to indicate that the given attribute's value on the given model object is
     * missing but the value set does not allow a {@code null} value.
     *
     * @param policyAttribute the model type reference for the validated attribute
     * @param modelObject the model object instance on which the attribute was validated
     * @param definingModelObjectClass the model object class where the generic validation is
     *            defined
     */
    Message createMessageForMissingMandatoryValue(PolicyAttribute policyAttribute,
            IModelObject modelObject,
            Class<? extends IModelObject> definingModelObjectClass);

    /**
     * Creates a message to indicate that the given attribute's value on the given model object is
     * set but the value set does not allow a value.
     *
     * @param policyAttribute the model type reference for the validated attribute
     * @param modelObject the model object instance on which the attribute was validated
     * @param definingModelObjectClass the model object class where the generic validation is
     *            defined
     */
    Message createMessageForValuePresentForIrrelevantAttribute(PolicyAttribute policyAttribute,
            IModelObject modelObject,
            Class<? extends IModelObject> definingModelObjectClass);

    /**
     * Creates a message to indicate that the given attribute's value on the given model object is
     * not allowed by the value set.
     *
     * @param policyAttribute the model type reference for the validated attribute
     * @param modelObject the model object instance on which the attribute was validated
     * @param definingModelObjectClass the model object class where the generic validation is
     *            defined
     *
     * @implSpec You may implement
     *               {@link #createMessageForValueNotInAllowedValueSet(PolicyAttribute, IModelObject, Class, ValueSet)}
     *               instead if you need to refer to the value set in your message.
     */
    Message createMessageForValueNotInAllowedValueSet(PolicyAttribute policyAttribute,
            IModelObject modelObject,
            Class<? extends IModelObject> definingModelObjectClass);

    /**
     * Creates a message to indicate that the given attribute's value on the given model object is
     * not allowed by the value set.
     *
     * @param policyAttribute the model type reference for the validated attribute
     * @param modelObject the model object instance on which the attribute was validated
     * @param definingModelObjectClass the model object class where the generic validation is
     *            defined
     * @param valueSet the actual value set, as retrieved by
     *            {@link #getValueSet(PolicyAttribute, IModelObject)} and used in the validation
     * 
     * @since 23.6
     */
    default Message createMessageForValueNotInAllowedValueSet(PolicyAttribute policyAttribute,
            IModelObject modelObject,
            Class<? extends IModelObject> definingModelObjectClass,
            ValueSet<?> valueSet) {
        return createMessageForValueNotInAllowedValueSet(policyAttribute, modelObject, definingModelObjectClass);
    }

    /**
     * Returns the value set configured for the given {@link PolicyAttribute} on the given
     * {@link IModelObject}.
     *
     * @param <T> the attribute's data type
     * @param policyAttribute the model type reference for the validated attribute
     * @param modelObject the model object instance on which the attribute is validated
     *
     * @implSpec This method may be overwritten, for example for value conversions. It is called
     *               once at the beginning of validation, afterwards the returned value set is
     *               passed to all relevant methods.
     *
     * @since 23.6
     */
    @SuppressWarnings("unchecked")
    default <T> ValueSet<T> getValueSet(PolicyAttribute policyAttribute, IModelObject modelObject) {
        return (ValueSet<T>)policyAttribute.getValueSet(modelObject);
    }

}
