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
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.type.PolicyAttribute;
import org.faktorips.valueset.ValueSet;

/**
 * Defines the relevance of a {@link PolicyAttribute}, derived from its
 * {@link PolicyAttribute#getValueSet(IModelObject) value set}:
 * <ul>
 * <li>An attribute with an empty (or {@code null}) value set is considered {@link #IRRELEVANT} - no
 * value should be set</li>
 * <li>An attribute with value set {@link ValueSet#containsNull() containing} {@code null} is
 * considered {@link #OPTIONAL} - a value can be set but is not required</li>
 * <li>An attribute with a non-empty value set not {@link ValueSet#containsNull() containing}
 * {@code null} is considered {@link #MANDATORY} - a value must be set</li>
 * </ul>
 * An attribute that is not {@link #IRRELEVANT} ({@link #OPTIONAL} or {@link #MANDATORY}) is
 * considered relevant - its value should for example be checked against the value set.
 */
public enum Relevance {
    /**
     * An attribute with an empty (or {@code null}) value set - no value should be set.
     */
    IRRELEVANT,
    /**
     * An attribute with value set {@link ValueSet#containsNull() containing} {@code null} - a value
     * can be set but is not required.
     */
    OPTIONAL,
    /**
     * An attribute with a non-empty value set not {@link ValueSet#containsNull() containing}
     * {@code null} - a value must be set.
     */
    MANDATORY;

    /**
     * Returns whether the attribute with the given property name is considered {@link #IRRELEVANT}
     * for the given model object.
     */
    public static boolean isIrrelevant(IModelObject modelObject, String property) {
        return isIrrelevant(modelObject, IpsModel.getPolicyCmptType(modelObject).getAttribute(property));
    }

    /**
     * Returns whether the given attribute is considered {@link #IRRELEVANT} for the given model
     * object.
     */
    public static boolean isIrrelevant(IModelObject modelObject, PolicyAttribute policyAttribute) {
        return IRRELEVANT == Relevance.of(modelObject, policyAttribute);
    }

    /**
     * Returns whether the attribute with the given property name is considered {@link #MANDATORY}
     * for the given model object.
     */
    public static boolean isMandatory(IModelObject modelObject, String property) {
        return isMandatory(modelObject, IpsModel.getPolicyCmptType(modelObject).getAttribute(property));
    }

    /**
     * Returns whether the given attribute is considered {@link #MANDATORY} for the given model
     * object.
     */
    public static boolean isMandatory(IModelObject modelObject, PolicyAttribute policyAttribute) {
        return MANDATORY == Relevance.of(modelObject, policyAttribute);
    }

    /**
     * Returns whether the attribute with the given property name is considered {@link #OPTIONAL}
     * for the given model object.
     */
    public static boolean isOptional(IModelObject modelObject, String property) {
        return isOptional(modelObject, IpsModel.getPolicyCmptType(modelObject).getAttribute(property));
    }

    /**
     * Returns whether the given attribute is considered {@link #OPTIONAL} for the given model
     * object.
     */
    public static boolean isOptional(IModelObject modelObject, PolicyAttribute policyAttribute) {
        return OPTIONAL == Relevance.of(modelObject, policyAttribute);
    }

    /**
     * Returns whether the attribute with the given property name is considered relevant for the
     * given model object.
     */
    public static boolean isRelevant(IModelObject modelObject, String property) {
        return isRelevant(modelObject, IpsModel.getPolicyCmptType(modelObject).getAttribute(property));
    }

    /**
     * Returns whether the given attribute is considered relevant for the given model object.
     */
    public static boolean isRelevant(IModelObject modelObject, PolicyAttribute policyAttribute) {
        return !isIrrelevant(modelObject, policyAttribute);
    }

    /**
     * Returns the {@link Relevance} of the {@link PolicyAttribute} identified by the given property
     * name for the given model object.
     */
    public static Relevance of(IModelObject modelObject, String property) {
        return Relevance.of(modelObject, IpsModel.getPolicyCmptType(modelObject).getAttribute(property));
    }

    /**
     * Returns the {@link Relevance} of the given {@link PolicyAttribute} for the given model
     * object.
     */
    public static Relevance of(IModelObject modelObject, PolicyAttribute policyAttribute) {
        ValueSet<?> valueSet = policyAttribute.getValueSet(modelObject);
        if (valueSet == null || valueSet.isEmpty()) {
            return Relevance.IRRELEVANT;
        } else if (valueSet.containsNull()) {
            return Relevance.OPTIONAL;
        } else {
            return Relevance.MANDATORY;
        }
    }
}
