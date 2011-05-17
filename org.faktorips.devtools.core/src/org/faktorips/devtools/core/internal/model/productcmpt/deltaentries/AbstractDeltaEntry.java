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

import org.faktorips.devtools.core.internal.model.productcmpt.GenerationToTypeDelta;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractDeltaEntry implements IDeltaEntry {

    private IPropertyValueContainer propertyValueContainer;

    public AbstractDeltaEntry(GenerationToTypeDelta delta) {
        delta.addEntry(this);
        this.propertyValueContainer = delta.getProductCmptGeneration();
    }

    protected IPropertyValueContainer getGeneration() {
        return propertyValueContainer;
    }

}
