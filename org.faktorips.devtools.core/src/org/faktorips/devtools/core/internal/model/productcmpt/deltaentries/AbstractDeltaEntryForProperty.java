/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import org.faktorips.devtools.core.model.productcmpt.IDeltaEntryForProperty;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;

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
        return getDeltaType() + ": " + getPropertyName() + "(" + getPropertyType().getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
    public ProductCmptPropertyType getPropertyType() {
        return propertyValue.getPropertyType();
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

}
