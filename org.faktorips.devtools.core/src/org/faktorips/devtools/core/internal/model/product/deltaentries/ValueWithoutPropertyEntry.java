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

import org.faktorips.devtools.core.internal.model.product.IPropertyValue;
import org.faktorips.devtools.core.internal.model.product.GenerationToTypeDelta;
import org.faktorips.devtools.core.model.product.DeltaType;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;

/**
 * 
 * @author Jan Ortmann
 */
public class ValueWithoutPropertyEntry extends AbstractDeltaEntry {

    private IPropertyValue value;
    
    public ValueWithoutPropertyEntry(GenerationToTypeDelta delta, IPropertyValue value) {
        super(delta);
        this.value = value;
    }
    
    /**
     * {@inheritDoc}
     */
    public ProdDefPropertyType getPropertyType() {
        return value.getPropertyType();
    }
    
    /**
     * {@inheritDoc}
     */
    public String getPropertyName() {
        return value.getPropertyName();
    }
    
    /**
     * {@inheritDoc}
     */
    public DeltaType getDeltaType() {
        return DeltaType.VALUE_WITHOUT_PROPERTY;
    }

    /**
     * {@inheritDoc}
     */
    public void fix() {
        value.delete();
    }

}
