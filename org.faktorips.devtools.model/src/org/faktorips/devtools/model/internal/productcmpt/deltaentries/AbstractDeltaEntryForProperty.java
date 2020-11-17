/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.deltaentries;

import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.productcmpt.IDeltaEntryForProperty;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractDeltaEntryForProperty implements IDeltaEntryForProperty {

    private final IPropertyValue propertyValue;

    /**
     * The {@link IPropertyValue} this entry is responsible for. May be null if the value does not
     * exists yet.
     * 
     * @param propertyValue The {@link IPropertyValue} this entry is responsible for.
     */
    public AbstractDeltaEntryForProperty(IPropertyValue propertyValue) {
        this.propertyValue = propertyValue;
    }

    @Override
    public String toString() {
        return getDeltaType() + ": " + getPropertyName() + "(" + getPropertyType() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * @return Returns the propertyValue.
     */
    public IPropertyValue getPropertyValue() {
        return propertyValue;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns the property type received by the property value.
     */
    @Override
    public PropertyValueType getPropertyType() {
        return propertyValue.getPropertyValueType();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns the name received by the property value.
     */
    @Override
    public String getPropertyName() {
        return getPropertyValue().getPropertyName();
    }

    @Override
    public Class<? extends IIpsObjectPart> getPartType() {
        return getPropertyType().getImplementationClass();
    }

}
