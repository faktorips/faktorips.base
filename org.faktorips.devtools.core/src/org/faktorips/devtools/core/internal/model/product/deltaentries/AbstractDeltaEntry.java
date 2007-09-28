/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.product.deltaentries;

import org.faktorips.devtools.core.internal.model.product.GenerationToTypeDelta;
import org.faktorips.devtools.core.model.product.IDeltaEntry;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractDeltaEntry implements IDeltaEntry {

    protected IProductCmptGeneration generation;

    public AbstractDeltaEntry(GenerationToTypeDelta delta) {
        delta.addEntry(this);
        this.generation = delta.getProductCmptGeneration();
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return getPropertyName();
    }
    
    public String toString() {
        return getDeltaType() + ": " + getPropertyName() + "(" + getPropertyType().getName() + ")";
    }
}
