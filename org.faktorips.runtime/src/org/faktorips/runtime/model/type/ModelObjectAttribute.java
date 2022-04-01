/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import static java.util.Objects.requireNonNull;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.validation.Relevance;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.faktorips.values.NullObject;
import org.faktorips.values.NullObjectSupport;
import org.faktorips.valueset.OrderedValueSet;
import org.faktorips.valueset.ValueSet;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A {@link ModelObjectAttribute} combines a {@link PolicyAttribute} with a specific
 * {@link IModelObject} of the {@link PolicyCmptType} the {@link PolicyAttribute} belongs to and
 * provides convenience methods to query and modify that combination.
 *
 * @since 21.6
 */
public class ModelObjectAttribute {
    private final IModelObject modelObject;
    private final PolicyAttribute policyAttribute;

    private ModelObjectAttribute(IModelObject modelObject, PolicyAttribute policyAttribute) {
        this.modelObject = modelObject;
        this.policyAttribute = policyAttribute;
    }

    /**
     * Creates a {@link ModelObjectAttribute} for the given model object and policy attribute.
     *
     * @param modelObject a model object instance of the {@link PolicyCmptType} the
     *            {@link PolicyAttribute} belongs to
     * @param policyAttribute a policy attribute
     * @throws IllegalArgumentException if the {@link PolicyAttribute} belongs to a
     *             {@link PolicyCmptType} the {@link IModelObject} is not an instance of
     */
    public static final ModelObjectAttribute of(@NonNull IModelObject modelObject,
            @NonNull PolicyAttribute policyAttribute) {
        Class<?> policyCmptTypeClassForAttribute = requireNonNull(policyAttribute, "policyAttribute must not be null")
                .getType().getDeclarationClass();
        if (!policyCmptTypeClassForAttribute.isInstance(requireNonNull(modelObject, "modelObject must not be null"))) {
            throw new IllegalArgumentException("The model object " + modelObject + " is a "
                    + IpsModel.getPolicyCmptType(modelObject) + " but the attribute " + policyAttribute.getName()
                    + " belongs to " + policyCmptTypeClassForAttribute);
        }
        return new ModelObjectAttribute(modelObject, policyAttribute);
    }

    /**
     * Creates a {@link ModelObjectAttribute} for the given model object and policy attribute.
     *
     * @param modelObject a model object
     * @param attributeName the name of a {@link PolicyAttribute} of the model object's
     *            {@link PolicyCmptType}
     */
    public static final ModelObjectAttribute of(@NonNull IModelObject modelObject, @NonNull String attributeName) {
        return ModelObjectAttribute.of(modelObject,
                IpsModel.getPolicyCmptType(modelObject).getAttribute(attributeName));
    }

    public IModelObject getModelObject() {
        return modelObject;
    }

    public PolicyAttribute getPolicyAttribute() {
        return policyAttribute;
    }

    /**
     * Returns the value of the this model object attribute.
     *
     * @throws IllegalArgumentException if the attribute is not accessible for any reason
     * @see PolicyAttribute#getValue(IModelObject)
     */
    public Object getValue() {
        return policyAttribute.getValue(modelObject);
    }

    /**
     * Checks whether this model object attribute has a non-empty value.
     *
     * @see PolicyAttribute#isValuePresent(IModelObject)
     */
    public boolean isValuePresent() {
        return policyAttribute.isValuePresent(modelObject);
    }

    /**
     * Checks whether this model object attribute is empty.
     *
     * @return {@code true} if empty
     * @see PolicyAttribute#isEmpty(IModelObject)
     */
    public boolean isEmpty() {
        return policyAttribute.isEmpty(modelObject);
    }

    /**
     * Sets the model object attribute to its null-value ({@code null} for most datatypes, a
     * {@link NullObject} for for the {@link NullObjectSupport}-datatypes {@link Decimal} and
     * {@link Money}, and an empty string for the datatype {@link String}) This only works for
     * changeable attributes.
     * 
     * @return this {@link ModelObjectAttribute}, to allow method chaining.
     * @throws IllegalArgumentException if the model object attribute is not accessible for any
     *             reason.
     * @throws UnsupportedOperationException if invoked on a
     *             {@link org.faktorips.runtime.model.type.AttributeKind#CONSTANT} attribute.
     * @see PolicyAttribute#removeValue(IModelObject)
     */
    public ModelObjectAttribute removeValue() {
        policyAttribute.removeValue(modelObject);
        return this;
    }

    /**
     * Returns the set of values allowed for this model object attribute.
     *
     * @throws UnsupportedOperationException if invoked on a
     *             {@link org.faktorips.runtime.model.type.AttributeKind#CONSTANT} attribute.
     * @throws IllegalStateException if the method that should return a value set for this attribute
     *             has too many arguments
     * @throws IllegalArgumentException if the invocation of the method that should return a value
     *             set for this attribute fails for any reason
     * @see PolicyAttribute#getValueSet(IModelObject)
     */
    public ValueSet<?> getValueSet() {
        return policyAttribute.getValueSet(modelObject);
    }

    /**
     * Sets the product configured set of allowed values of this model object attribute.
     * <p>
     * <em>Caution:</em> as generics are erased at runtime, it is possible to set a {@link ValueSet}
     * of a mismatched type with this method, for example an {@link OrderedValueSet
     * OrderedValueSet&lt;String&gt;} for an attribute with datatype {@link Integer}, which will
     * result in a {@link ClassCastException} on later method calls.
     * 
     * @param valueSet the new value set
     * @return this {@link ModelObjectAttribute}, to allow method chaining.
     * @throws UnsupportedOperationException if invoked on a
     *             {@link org.faktorips.runtime.model.type.AttributeKind#CONSTANT} attribute.
     * @throws ClassCastException if the type of value set does not match the property's
     *             configuration or the model object is no {@link IConfigurableModelObject}.
     * @throws IllegalStateException if the model object has no setter method for this attribute's
     *             value set. This also occurs if the corresponding policy class is not configured
     *             by a product class.
     * @throws IllegalArgumentException if the invocation of the method that should set the value
     *             set for this attribute fails for any reason
     * @see PolicyAttribute#setValueSet(IConfigurableModelObject, ValueSet)
     */
    public ModelObjectAttribute setValueSet(ValueSet<?> valueSet) {
        policyAttribute.setValueSet((IConfigurableModelObject)modelObject, valueSet);
        return this;
    }

    /**
     * Returns the product configured default value of the attribute identified by this model object
     * attribute. Throws an {@link IllegalStateException} if the model object has no
     * getDefaultValue() method for this attribute. This also occurs if the corresponding policy
     * class is not configured by a product class.
     * 
     * @throws IllegalStateException if the model object has no getter method for this attribute's
     *             default value. This also occurs if the corresponding policy class is not
     *             configured by a product class.
     * @throws IllegalArgumentException if the invocation of the method that should get the default
     *             value for this attribute fails for any reason
     * @throws ClassCastException if the model object is no {@link IConfigurableModelObject}.
     * @see PolicyAttribute#getDefaultValue(IModelObject)
     */
    public Object getDefaultValue() {
        return policyAttribute.getDefaultValue(modelObject);
    }

    /**
     * Sets the product configured default value of this attribute.
     * 
     * necessary) effective date can be retrieved
     *
     * @param defaultValue the new default value
     * @return this {@link ModelObjectAttribute}, to allow method chaining.
     * @throws UnsupportedOperationException if invoked on a
     *             {@link org.faktorips.runtime.model.type.AttributeKind#CONSTANT} attribute.
     * @throws IllegalStateException if the model object has no setter method for this attribute's
     *             default value. This also occurs if the corresponding policy class is not
     *             configured by a product class.
     * @throws IllegalArgumentException if the invocation of the method that should set the default
     *             value for this attribute fails for any reason
     * @throws ClassCastException if the model object is no {@link IConfigurableModelObject}.
     * @see PolicyAttribute#setDefaultValue(IConfigurableModelObject, Object)
     */
    public ModelObjectAttribute setDefaultValue(Object defaultValue) {
        policyAttribute.setDefaultValue((IConfigurableModelObject)modelObject, defaultValue);
        return this;
    }

    /**
     * Returns whether this attribute is considered {@link Relevance#IRRELEVANT}.
     *
     * @see Relevance#isIrrelevant(IModelObject, PolicyAttribute)
     */
    public boolean isIrrelevant() {
        return Relevance.isIrrelevant(modelObject, policyAttribute);
    }

    /**
     * Returns whether this attribute is considered relevant.
     *
     * @see Relevance#isRelevant(IModelObject, PolicyAttribute)
     */
    public boolean isRelevant() {
        return Relevance.isRelevant(modelObject, policyAttribute);
    }

    /**
     * Returns whether this attribute is considered {@link Relevance#MANDATORY}.
     *
     * @see Relevance#isMandatory(IModelObject, PolicyAttribute)
     */
    public boolean isMandatory() {
        return Relevance.isMandatory(modelObject, policyAttribute);
    }

    /**
     * Returns whether this attribute is considered {@link Relevance#OPTIONAL}.
     *
     * @see Relevance#isOptional(IModelObject, PolicyAttribute)
     */
    public boolean isOptional() {
        return Relevance.isOptional(modelObject, policyAttribute);
    }

    /**
     * Returns an {@link ObjectProperty} for this model object attribute.
     */
    public ObjectProperty toObjectProperty() {
        return new ObjectProperty(modelObject, policyAttribute.getName());
    }
}
