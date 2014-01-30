/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IFixDifferencesToModelSupport;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;

public interface IPropertyValueContainer extends IProductPartsContainer {

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
     * {@link #getPropertyValue(String)}.
     */
    public IPropertyValue getPropertyValue(IProductCmptProperty property);

    /**
     * Returns whether this {@link IPropertyValueContainer} contains an {@link IPropertyValue} for
     * the indicated {@link IProductCmptProperty}.
     */
    public boolean hasPropertyValue(IProductCmptProperty property);

    /**
     * Returns the property values for the given property name or <code>null</code> if no value is
     * defined for this container. In this case
     * {@link IFixDifferencesToModelSupport#computeDeltaToModel(IIpsProject)} returns a delta
     * containing an entry for the missing property value.
     * <p>
     * Returns <code>null</code> if propertyName is <code>null</code>.
     */
    public IPropertyValue getPropertyValue(String propertyName);

    /**
     * Returns all property values for the given type. Returns an empty array if type is
     * <code>null</code> or no property values were found for the given type.
     */
    public <T extends IPropertyValue> List<T> getPropertyValues(Class<T> type);

    /**
     * Returns all property values in this container or an empty list, if no property values are
     * defined.
     */
    public List<IPropertyValue> getAllPropertyValues();

    /**
     * Creates a new property value for the given property.
     * 
     * @throws NullPointerException if property is <code>null</code>.
     */
    public IPropertyValue newPropertyValue(IProductCmptProperty property);

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
    public boolean isContainerFor(IProductCmptProperty property);

    @Override
    public IProductCmpt getProductCmpt();

    @Override
    public String getProductCmptType();

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException;

    /**
     * Finds the {@link IPolicyCmptType} this this property value container configures or returns
     * <code>null</code> if the {@link IPolicyCmptType} could not be found or this container does
     * not configure a {@link IPolicyCmptType}.
     * 
     * @param ipsProject The {@link IIpsProject} used as base project to search
     * @return the {@link IPolicyCmptType} or null if no one was found or this container does not
     *         configure a {@link IPolicyCmptType}
     * @throws CoreException in case of getting a core exception while searching the model
     */
    public IPolicyCmptType findPolicyCmptType(IIpsProject ipsProject) throws CoreException;

}