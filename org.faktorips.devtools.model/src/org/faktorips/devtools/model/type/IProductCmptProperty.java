/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.type;

import java.util.List;

import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;

/**
 * An abstraction of properties (defined by a type) that are configured by product components. Such
 * properties have one or more value-instance ({@link IPropertyValue}). For example a
 * {@link IPolicyCmptTypeAttribute} has two {@linkplain IPropertyValue IPropertyValues} namely
 * {@link IConfiguredValueSet} and {@link IConfiguredDefault}.
 * 
 * @author Jan Ortmann
 * @author Stefan Widmaier
 */
public interface IProductCmptProperty extends IChangingOverTimeProperty {

    /**
     * Returns the type of the property. The different types of product definition properties are
     * defined by {@link ProductCmptPropertyType}. The type represents the different elements in the
     * model that implement this interface. Each type corresponds to one element.
     * <p>
     * See {@link ProductCmptPropertyType} for safe casts to a specific model element.
     * 
     * @see ProductCmptPropertyType
     */
    ProductCmptPropertyType getProductCmptPropertyType();

    /**
     * Returns the list of value types that are supported by this property value.
     * 
     * @return a list {@link PropertyValueType} which are supported by this
     *             {@link IProductCmptProperty}
     */
    List<PropertyValueType> getPropertyValueTypes();

    /**
     * Returns the name of the property. That name is unique in the corresponding
     * {@link IProductCmpt}.
     */
    String getPropertyName();

    /**
     * Returns this property's data type.
     */
    String getPropertyDatatype();

    /**
     * Returns whether this property's parent is a policy component type instead of a product
     * component type.
     */
    boolean isPolicyCmptTypeProperty();

    /**
     * Returns whether this {@link IProductCmptProperty} corresponds to the indicated
     * {@link IPropertyValue}.
     * 
     * @param propertyValue the {@link IPropertyValue} to check for correspondence
     */
    boolean isPropertyFor(IPropertyValue propertyValue);

    /**
     * Returns the name of the {@link IProductCmptCategory} this {@link IProductCmptProperty} is
     * assigned to.
     * <p>
     * <strong>Important:</strong> The returned string is always the name that is stored in the
     * {@link IProductCmptProperty} itself. However, this does not always reflect the property's
     * real {@link IProductCmptCategory}.
     * <ul>
     * <li>If the string is empty or the indicated {@link IProductCmptCategory} cannot be found, the
     * {@link IProductCmptProperty} is automatically assigned to the default
     * {@link IProductCmptCategory} corresponding to this property's
     * {@link ProductCmptPropertyType}.
     * <li>If this {@link IProductCmptProperty} belongs to an {@link IPolicyCmptType} and the
     * category assignment is changed using
     * {@link IProductCmptType#changeCategoryAndDeferPolicyChange(IProductCmptProperty, String)},
     * this change is not immediately reflected by this getter as the method defers saving the
     * {@link IPolicyCmptType} until the {@link IProductCmptType} is saved.
     * </ul>
     */
    String getCategory();

    /**
     * Sets the name of the {@link IProductCmptCategory} this {@link IProductCmptProperty} is
     * assigned to.
     * 
     * @see #getCategory()
     */
    void setCategory(String category);

    int getCategoryPosition();

    void setCategoryPosition(int categoryPosition);

}
