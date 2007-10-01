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
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.DeltaType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;

/**
 * 
 * @author Jan Ortmann
 */
public class ValueSetMismatchEntry extends AbstractDeltaEntry {

    private IAttribute attribute;
    private IConfigElement element;
    
    public ValueSetMismatchEntry(GenerationToTypeDelta delta, IAttribute attribute, IConfigElement element) {
        super(delta);
        this.attribute = attribute;
        this.element = element;
    }

    /**
     * {@inheritDoc}
     */
    public ProdDefPropertyType getPropertyType() {
        return ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET;
    }
    
    /**
     * {@inheritDoc}
     */
    public void fix() {
        element.setValueSetCopy(attribute.getValueSet());
    }

    /**
     * {@inheritDoc}
     */
    public String getPropertyName() {
        return element.getName();
    }

    /**
     * {@inheritDoc}
     */
    public DeltaType getDeltaType() {
        return DeltaType.VALUE_SET_MISMATCH;
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return getPropertyName() + ": Expected " + attribute.getValueSet().getValueSetType().getName()
             + ", actual is " + element.getValueSet().getValueSetType().getName();
    }

}
