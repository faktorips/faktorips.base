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

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.productcmpt.GenerationToTypeDelta;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;

/**
 * 
 * @author Jan Ortmann
 */
public class ValueSetMismatchEntry extends AbstractDeltaEntryForProperty {

    private IPolicyCmptTypeAttribute attribute;
    private IConfigElement element;
    
    public ValueSetMismatchEntry(GenerationToTypeDelta delta, IPolicyCmptTypeAttribute attribute, IConfigElement element) {
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
        return NLS.bind(Messages.getString("ValueSetMismatchEntry.description"), new Object[]{getPropertyName(), attribute.getValueSet().getValueSetType().getName(), element.getValueSet().getValueSetType().getName()}); //$NON-NLS-1$
    }

}
