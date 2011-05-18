/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import org.faktorips.devtools.core.internal.model.productcmpt.PropertyValueContainerToTypeDelta;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractDeltaEntry implements IDeltaEntry {

    private IPropertyValueContainer propertyValueContainer;

    public AbstractDeltaEntry(PropertyValueContainerToTypeDelta delta) {
        delta.addEntry(this);
        this.propertyValueContainer = delta.getPropertyValueContainer();
    }

    protected IPropertyValueContainer getGeneration() {
        return propertyValueContainer;
    }

}
