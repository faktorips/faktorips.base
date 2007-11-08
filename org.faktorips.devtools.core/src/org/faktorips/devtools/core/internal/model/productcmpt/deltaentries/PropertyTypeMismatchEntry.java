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
import org.faktorips.devtools.core.internal.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpttype.IProdDefProperty;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;

/**
 * 
 * @author Jan Ortmann
 */
public class PropertyTypeMismatchEntry extends AbstractDeltaEntryForProperty {

    private IProdDefProperty property;
    private IPropertyValue value;
    
    public PropertyTypeMismatchEntry(GenerationToTypeDelta delta, IProdDefProperty property, IPropertyValue value) {
        super(delta);
        this.property = property;
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
        return property.getPropertyName();
    }
    
    /**
     * {@inheritDoc}
     */
    public DeltaType getDeltaType() {
        return DeltaType.PROPERTY_TYPE_MISMATCH;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return NLS.bind(Messages.getString("PropertyTypeMismatchEntry.description"), new Object[]{getPropertyName(), property.getProdDefPropertyType().getName(), value.getPropertyType().getName()}); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    public void fix() {
        value.delete();
        generation.newPropertyValue(property);
    }

}
