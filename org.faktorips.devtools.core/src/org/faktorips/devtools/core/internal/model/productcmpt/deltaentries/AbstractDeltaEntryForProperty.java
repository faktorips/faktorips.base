/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
