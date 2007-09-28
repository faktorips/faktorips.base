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
import org.faktorips.devtools.core.model.product.DeltaType;
import org.faktorips.devtools.core.model.productcmpttype2.IProdDefProperty;
import org.faktorips.devtools.core.model.productcmpttype2.ProdDefPropertyType;

/**
 * 
 * @author Jan Ortmann
 */
public class MissingPropertyValueEntry extends AbstractDeltaEntry {

    private IProdDefProperty property;
    
    public MissingPropertyValueEntry(GenerationToTypeDelta delta, IProdDefProperty property) {
        super(delta);
        this.property = property;
    }
    
    /**
     * {@inheritDoc}
     */
    public ProdDefPropertyType getPropertyType() {
        return property.getProdDefPropertyType();
    }

    /**
     * {@inheritDoc}
     */
    public String getPropertyName() {
        return property.getPropertyName();
    }
    
    /**
     * {@inheritDoc}
     */
    public DeltaType getDeltaType() {
        return DeltaType.MISSING_PROPERTY_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    public void fix() {
        generation.newPropertyValue(property);
    }


    
}
