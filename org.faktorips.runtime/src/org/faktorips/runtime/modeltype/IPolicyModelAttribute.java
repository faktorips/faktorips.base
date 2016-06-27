/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.modeltype;

import java.util.Calendar;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.valueset.ValueSet;

public interface IPolicyModelAttribute extends IModelTypeAttribute {

    /**
     * Returns the value of the given model object's attribute identified by this model type
     * attribute.
     * 
     * @param modelObject a model object corresponding to the {@link IModelType} this attribute
     *            belongs to
     * @return the value of the given model object's attribute identified by this model type
     *         attribute
     * @throws IllegalArgumentException if the model object does not have an attribute fitting this
     *             model type attribute or that attribute is not accessible for any reason
     */
    public Object getValue(IModelObject modelObject);

    /**
     * Sets the given model object's attribute identified by this model type attribute to the given
     * value. This only works for changeable attributes.
     * 
     * @param modelObject a model object corresponding to the {@link IModelType} this attribute
     *            belongs to
     * @param value an object of this model type attribute's datatype
     * @throws IllegalArgumentException if the model object does not have a changeable attribute
     *             fitting this model type attribute or that attribute is not accessible for any
     *             reason or the value does not fit the attribute's datatype.
     * @throw UnsupportedOperationException if invoked on a
     *        {@link org.faktorips.runtime.modeltype.IModelTypeAttribute.AttributeType#CONSTANT}
     *        attribute.
     */
    public void setValue(IModelObject modelObject, Object value);

    /**
     * Returns the product configured default value of the attribute identified by this model type
     * attribute. Throws an {@link IllegalArgumentException} if the model object has no
     * getDefaultValue() method for this attribute. This also occurs if the corresponding policy
     * class is not configured by a product class.
     * 
     * @param modelObject the configurable model object from which product component and (if
     *            necessary) effective date can be retrieved
     * @see #getDefaultValue(IProductComponent, Calendar)
     * @throw UnsupportedOperationException if invoked on a
     *        {@link org.faktorips.runtime.modeltype.IModelTypeAttribute.AttributeType#CONSTANT}
     *        attribute.
     */
    Object getDefaultValue(IConfigurableModelObject modelObject);

    /**
     * Returns the product configured default value of the attribute identified by this model type
     * attribute. Throws an {@link IllegalArgumentException} if the model object has no
     * getDefaultValue() method for this attribute. This also occurs if the corresponding policy
     * class is not configured by a product class.
     * 
     * @param source the product component to read the attribute default value from.
     * @param effectiveDate the date to determine the product component generation. If
     *            <code>null</code> the latest generation is used. Is ignored if the attribute's
     *            configuration is not changing over time.
     * @throw UnsupportedOperationException if invoked on a
     *        {@link org.faktorips.runtime.modeltype.IModelTypeAttribute.AttributeType#CONSTANT}
     *        attribute.
     */
    Object getDefaultValue(IProductComponent source, Calendar effectiveDate);

    /**
     * Returns the value set of the given model object's attribute identified by this model type
     * attribute. Throws an {@link IllegalArgumentException} if the model object has no
     * getAllowedValues() method for this attribute. This also occurs if the corresponding policy
     * class is not configured by a product class.
     *
     * @param modelObject a model object
     * @throw UnsupportedOperationException if invoked on a
     *        {@link org.faktorips.runtime.modeltype.IModelTypeAttribute.AttributeType#CONSTANT}
     *        attribute.
     */
    ValueSet<?> getValueSet(IModelObject modelObject, IValidationContext context);

    /**
     * Returns the value set of the given model object's attribute identified by this model type
     * attribute. Throws an {@link IllegalArgumentException} if the model object has no
     * getAllowedValues() method for this attribute. This also occurs if the corresponding policy
     * class is not configured by a product class.
     *
     * @param source the product component to read an attribute value set from. Must correspond to
     *            the {@link IModelType} this attribute belongs to.
     * @param effectiveDate the date to determine the product component generation. If
     *            <code>null</code> the latest generation is used. Is ignored if the attribute's
     *            configuration is not changing over time.
     * @throw UnsupportedOperationException if invoked on a
     *        {@link org.faktorips.runtime.modeltype.IModelTypeAttribute.AttributeType#CONSTANT}
     *        attribute.
     */
    ValueSet<?> getValueSet(IProductComponent source, Calendar effectiveDate, IValidationContext context);

}
