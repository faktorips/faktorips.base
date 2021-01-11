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

import java.util.Calendar;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.ValidationContext;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.valueset.OrderedValueSet;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;

/**
 * An attribute of a {@link PolicyCmptType}.
 */
public abstract class PolicyAttribute extends Attribute {

    public PolicyAttribute(Type type, IpsAttribute attributeAnnotation, IpsExtensionProperties extensionProperties,
            Class<?> datatype, boolean changingOverTime) {
        super(type, attributeAnnotation, extensionProperties, datatype, changingOverTime);
    }

    /**
     * Returns the {@link PolicyCmptType} this attribute belongs to.
     */
    @Override
    public PolicyCmptType getType() {
        return (PolicyCmptType)super.getType();
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated Use {@link #getType()}
     */
    @Deprecated
    @Override
    public PolicyCmptType getModelType() {
        return getType();
    }

    @Override
    public PolicyAttribute getSuperAttribute() {
        return (PolicyAttribute)super.getSuperAttribute();
    }

    /**
     * Returns the value of the given model object's attribute identified by this model type
     * attribute.
     * 
     * @param modelObject a model object corresponding to the {@link Type} this attribute belongs to
     * @return the value of the given model object's attribute identified by this model type
     *         attribute
     * @throws IllegalArgumentException if the model object does not have an attribute fitting this
     *             model type attribute or that attribute is not accessible for any reason
     */
    public abstract Object getValue(IModelObject modelObject);

    /**
     * Sets the given model object's attribute identified by this model type attribute to the given
     * value. This only works for changeable attributes.
     * 
     * @param modelObject a model object corresponding to the {@link Type} this attribute belongs to
     * @param value an object of this model type attribute's datatype
     * @throws IllegalArgumentException if the model object does not have a changeable attribute
     *             fitting this model type attribute or that attribute is not accessible for any
     *             reason or the value does not fit the attribute's datatype.
     * @throws UnsupportedOperationException if invoked on a
     *             {@link org.faktorips.runtime.model.type.AttributeKind#CONSTANT} attribute.
     */
    public abstract void setValue(IModelObject modelObject, Object value);

    /**
     * Returns the product configured default value of the attribute identified by this model type
     * attribute. Throws an {@link IllegalStateException} if the model object has no
     * getDefaultValue() method for this attribute. This also occurs if the corresponding policy
     * class is not configured by a product class.
     * 
     * @param modelObject the configurable model object from which product component and (if
     *            necessary) effective date can be retrieved
     * @see #getDefaultValue(IProductComponent, Calendar)
     * @throws IllegalStateException if the model object has no getter method for this attribute's
     *             default value. This also occurs if the corresponding policy class is not
     *             configured by a product class.
     * @throws IllegalArgumentException if the invocation of the method that should get the default
     *             value for this attribute fails for any reason
     */
    public abstract Object getDefaultValue(IConfigurableModelObject modelObject);

    /**
     * Returns the product configured default value of the attribute identified by this model type
     * attribute.
     * 
     * @param source the product component to read the attribute default value from.
     * @param effectiveDate the date to determine the product component generation. If
     *            <code>null</code> the latest generation is used. Is ignored if the attribute's
     *            configuration is not changing over time.
     * @throws UnsupportedOperationException if invoked on a
     *             {@link org.faktorips.runtime.model.type.AttributeKind#CONSTANT} attribute.
     * @throws IllegalStateException if the model object has no getter method for this attribute's
     *             default value. This also occurs if the corresponding policy class is not
     *             configured by a product class.
     * @throws IllegalArgumentException if the invocation of the method that should get the default
     *             value for this attribute fails for any reason
     */
    public abstract Object getDefaultValue(IProductComponent source, Calendar effectiveDate);

    /**
     * Sets the product configured default value of the attribute identified by this model type
     * attribute.
     * 
     * @param modelObject the configurable model object from which product component and (if
     *            necessary) effective date can be retrieved
     * @param defaultValue the new default value
     * @throws UnsupportedOperationException if invoked on a
     *             {@link org.faktorips.runtime.model.type.AttributeKind#CONSTANT} attribute.
     * @throws IllegalStateException if the model object has no setter method for this attribute's
     *             default value. This also occurs if the corresponding policy class is not
     *             configured by a product class.
     * @throws IllegalArgumentException if the invocation of the method that should set the default
     *             value for this attribute fails for any reason
     * @since 20.6
     */
    public abstract void setDefaultValue(IConfigurableModelObject modelObject, Object defaultValue);

    /**
     * Sets the product configured default value of the attribute identified by this model type
     * attribute.
     * 
     * @param target the product component to write the attribute default value to
     * @param effectiveDate the date to determine the product component generation. If
     *            <code>null</code> the latest generation is used. Is ignored if the attribute's
     *            configuration is not changing over time.
     * @param defaultValue the new default value
     * @throws UnsupportedOperationException if invoked on a
     *             {@link org.faktorips.runtime.model.type.AttributeKind#CONSTANT} attribute.
     * @throws IllegalStateException if the model object has no setter method for this attribute's
     *             default value. This also occurs if the corresponding policy class is not
     *             configured by a product class.
     * @throws IllegalArgumentException if the invocation of the method that should set the default
     *             value for this attribute fails for any reason
     * @since 20.6
     */
    public abstract void setDefaultValue(IProductComponent target, Calendar effectiveDate, Object defaultValue);

    /**
     * Returns the value set of the given model object's attribute identified by this model type
     * attribute.
     * <p>
     * <em>Note:</em> If the {@link PolicyCmptType} this {@link PolicyAttribute} belongs to defines
     * an {@link UnrestrictedValueSet} including <code>null</code>, no method is generated by
     * Faktor-IPS. Therefore, a value set method in a subclass is not found even if the
     * {@link IModelObject} passed to this method is an instance of that subclass. To make sure to
     * get the correct value set, always use {@link IpsModel#getPolicyCmptType(IModelObject)} on the
     * actual {@link IModelObject} instead of {@link IpsModel#getPolicyCmptType(Class)} on the super
     * class.
     * </p>
     *
     * @param modelObject a model object
     * @throws IllegalStateException if the method that should return a value set for this attribute
     *             has too many arguments
     * @throws IllegalArgumentException if the invocation of the method that should return a value
     *             set for this attribute fails for any reason
     */
    public abstract ValueSet<?> getValueSet(IModelObject modelObject, IValidationContext context);

    /**
     * Returns the value set of the given model object's attribute identified by this model type
     * attribute.
     * <p>
     * <em>Note:</em> If the {@link PolicyCmptType} this {@link PolicyAttribute} belongs to defines
     * an {@link UnrestrictedValueSet} including <code>null</code>, no method is generated by
     * Faktor-IPS. Therefore, a value set method in a subclass is not found even if the
     * {@link IModelObject} passed to this method is an instance of that subclass. To make sure to
     * get the correct value set, always use {@link IpsModel#getPolicyCmptType(IModelObject)} on the
     * actual {@link IModelObject} instead of {@link IpsModel#getPolicyCmptType(Class)} on the super
     * class.
     * </p>
     * This method uses a default {@link IValidationContext}.
     *
     * @param modelObject a model object
     * @throws UnsupportedOperationException if invoked on a
     *             {@link org.faktorips.runtime.model.type.AttributeKind#CONSTANT} attribute.
     * @throws IllegalStateException if the method that should return a value set for this attribute
     *             has too many arguments
     * @throws IllegalArgumentException if the invocation of the method that should return a value
     *             set for this attribute fails for any reason
     */
    public ValueSet<?> getValueSet(IModelObject modelObject) {
        return getValueSet(modelObject, new ValidationContext());
    }

    /**
     * Returns the value set of the given model object's attribute identified by this model type
     * attribute. Returns an {@link UnrestrictedValueSet} if there is no method that returns a value
     * set for this attribute.
     *
     * @param source the product component to read an attribute value set from. Must correspond to
     *            the {@link Type} this attribute belongs to.
     * @param effectiveDate the date to determine the product component generation. If
     *            <code>null</code> the latest generation is used. Is ignored if the attribute's
     *            configuration is not changing over time.
     * @throws UnsupportedOperationException if invoked on a
     *             {@link org.faktorips.runtime.model.type.AttributeKind#CONSTANT} attribute.
     * @throws IllegalStateException if the method that should return a value set for this attribute
     *             has too many arguments
     * @throws IllegalArgumentException if the invocation of the method that should return a value
     *             set for this attribute fails for any reason
     */
    public abstract ValueSet<?> getValueSet(IProductComponent source,
            Calendar effectiveDate,
            IValidationContext context);

    /**
     * Returns the value set of the given model object's attribute identified by this model type
     * attribute. Returns an {@link UnrestrictedValueSet} if there is no method that returns a value
     * set for this attribute.
     * <p>
     * This method uses a default {@link IValidationContext}.
     *
     * @param source the product component to read an attribute value set from. Must correspond to
     *            the {@link Type} this attribute belongs to.
     * @param effectiveDate the date to determine the product component generation. If
     *            <code>null</code> the latest generation is used. Is ignored if the attribute's
     *            configuration is not changing over time.
     * @throws UnsupportedOperationException if invoked on a
     *             {@link org.faktorips.runtime.model.type.AttributeKind#CONSTANT} attribute.
     * @throws IllegalStateException if the method that should return a value set for this attribute
     *             has too many arguments
     * @throws IllegalArgumentException if the invocation of the method that should return a value
     *             set for this attribute fails for any reason
     */
    public ValueSet<?> getValueSet(IProductComponent source, Calendar effectiveDate) {
        return getValueSet(source, effectiveDate, new ValidationContext());
    }

    /**
     * Sets the product configured set of allowed values of the attribute identified by this model
     * type attribute.
     * <p>
     * <em>Caution:</em> as generics are erased at runtime, it is possible to set a {@link ValueSet}
     * of a mismatched type with this method, for example an {@link OrderedValueSet
     * OrderedValueSet&lt;String&gt;} for an attribute with {@link #getDatatype()} {@link Integer},
     * which will result in a {@link ClassCastException} on later method calls.
     * 
     * @param modelObject the configurable model object from which product component and (if
     *            necessary) effective date can be retrieved
     * @param valueSet the new value set
     * @throws UnsupportedOperationException if invoked on a
     *             {@link org.faktorips.runtime.model.type.AttributeKind#CONSTANT} attribute.
     * @throws ClassCastException if the type of value set does not match the property's
     *             configuration
     * @throws IllegalStateException if the model object has no setter method for this attribute's
     *             value set. This also occurs if the corresponding policy class is not configured
     *             by a product class.
     * @throws IllegalArgumentException if the invocation of the method that should set the value
     *             set for this attribute fails for any reason
     * @since 20.6
     */
    public abstract void setValueSet(IConfigurableModelObject modelObject, ValueSet<?> valueSet);

    /**
     * Sets the product configured set of allowed values of the attribute identified by this model
     * type attribute.
     * <p>
     * <em>Caution:</em> as generics are erased at runtime, it is possible to set a {@link ValueSet}
     * of a mismatched type with this method, for example an {@link OrderedValueSet
     * OrderedValueSet&lt;String&gt;} for an attribute with {@link #getDatatype()} {@link Integer},
     * which will result in a {@link ClassCastException} on later method calls.
     * 
     * @param target the product component to write the attribute value set to. Must correspond to
     *            the {@link Type} this attribute belongs to.
     * @param effectiveDate the date to determine the product component generation. If
     *            <code>null</code> the latest generation is used. Is ignored if the attribute's
     *            configuration is not changing over time.
     * @param valueSet the new value set
     * @throws UnsupportedOperationException if invoked on a
     *             {@link org.faktorips.runtime.model.type.AttributeKind#CONSTANT} attribute.
     * @throws ClassCastException if the type of value set does not match the property's
     *             configuration
     * @throws IllegalStateException if the model object has no setter method for this attribute's
     *             value set. This also occurs if the corresponding policy class is not configured
     *             by a product class.
     * @throws IllegalArgumentException if the invocation of the method that should set the value
     *             set for this attribute fails for any reason
     * @since 20.6
     */
    public abstract void setValueSet(IProductComponent target, Calendar effectiveDate, ValueSet<?> valueSet);

}
