/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import java.util.List;

import org.faktorips.devtools.model.ipsobject.IFixDifferencesToModelSupport;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueContainer;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IProductCmptProperty;

public interface IPropertyValueContainer extends IProductPartsContainer, ITemplatedValueContainer {

    /**
     * Returns the property value for the given property or <code>null</code> if no value is defined
     * for this container. In this case
     * {@link IFixDifferencesToModelSupport#computeDeltaToModel(IIpsProject)} returns a delta
     * containing an entry for the missing property value.
     * <p>
     * Returns <code>null</code> if property is <code>null</code>.
     * <p>
     * Note that this method searches only the property values that have the same property type as
     * the indicated property. If you want to search only by name, use
     * {@link #getPropertyValue(String, Class)}.
     */
    <T extends IPropertyValue> T getPropertyValue(IProductCmptProperty property, Class<T> type);

    /**
     * Returns the property values for the given property or an empty list if no value is defined
     * for this container. In this case
     * {@link IFixDifferencesToModelSupport#computeDeltaToModel(IIpsProject)} returns a delta
     * containing an entry for the missing property values.
     * <p>
     * Returns an empty list if property is <code>null</code>.
     * <p>
     * Note that this method searches only the property values that have the same property type as
     * the indicated property. If you want to search only by name, use
     * {@link #getPropertyValues(String)}.
     */
    List<IPropertyValue> getPropertyValues(IProductCmptProperty property);

    /**
     * Returns whether this {@link IPropertyValueContainer} contains an {@link IPropertyValue} for
     * the indicated {@link IProductCmptProperty}.
     */
    boolean hasPropertyValue(IProductCmptProperty property, PropertyValueType type);

    /**
     * Returns the property values for the given property name or an empty list if no value is
     * defined for this container. In this case
     * {@link IFixDifferencesToModelSupport#computeDeltaToModel(IIpsProject)} returns a delta
     * containing an entry for the missing property values.
     * <p>
     * Returns an empty list if propertyName is <code>null</code>.
     */
    List<IPropertyValue> getPropertyValues(String propertyName);

    /**
     * Returns the property value for the given property name and type or {@code null} if no value
     * is defined for this container. In this case
     * {@link IFixDifferencesToModelSupport#computeDeltaToModel(IIpsProject)} returns a delta
     * containing an entry for the missing property value.
     * <p>
     * Returns {@code null} if propertyName is {@code null}.
     */
    <T extends IPropertyValue> T getPropertyValue(String propertyName, Class<T> type);

    /**
     * Returns all property values for the given type. Returns an empty array if type is
     * <code>null</code> or no property values were found for the given type.
     */
    <T extends IPropertyValue> List<T> getPropertyValues(Class<T> type);

    /**
     * Returns all property values in this container or an empty list, if no property values are
     * defined.
     */
    List<IPropertyValue> getAllPropertyValues();

    /**
     * Creates a new property value for the given property and {@link PropertyValueType}.
     * 
     * @param property the property for that a new value should be created
     * @param type the interface type of property value that should be created
     * 
     * @return Returns the newly created part
     * 
     * @throws NullPointerException if property is <code>null</code>.
     */
    <T extends IPropertyValue> T newPropertyValue(IProductCmptProperty property, Class<T> type);

    /**
     * Creates new property values for the given property.
     * 
     * @throws NullPointerException if property is <code>null</code>.
     */
    List<IPropertyValue> newPropertyValues(IProductCmptProperty property);

    /**
     * Returns true if this container is responsible for the given property. For example a
     * {@link IProductCmpt} is only responsible for attributes that cannot change over time but not
     * for changing attributes.
     * <p>
     * The container could use the property {@link IProductCmptProperty#isChangingOverTime()} to
     * decide whether it is responsible for the given attribute or not.
     * 
     * @param property The property that should be checked by the container
     * 
     * @return true if the property could resist in this container, false otherwise.
     */
    boolean isContainerFor(IProductCmptProperty property);

    @Override
    IProductCmpt getProductCmpt();

    @Override
    String getProductCmptType();

    @Override
    IProductCmptType findProductCmptType(IIpsProject ipsProject);

    /**
     * Finds the {@link IPolicyCmptType} this this property value container configures or returns
     * <code>null</code> if the {@link IPolicyCmptType} could not be found or this container does
     * not configure a {@link IPolicyCmptType}.
     * 
     * @param ipsProject The {@link IIpsProject} used as base project to search
     * @return the {@link IPolicyCmptType} or null if no one was found or this container does not
     *             configure a {@link IPolicyCmptType}
     */
    IPolicyCmptType findPolicyCmptType(IIpsProject ipsProject);

    @Override
    IPropertyValueContainer findTemplate(IIpsProject ipsProject);

}
