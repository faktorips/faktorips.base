/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
    @Override
    public String getDescription() {
        String desc = Messages.ValueSetMismatchEntry_desc;
        return NLS.bind(desc, new Object[] { getPropertyName(), attribute.getValueSet().getValueSetType().getName(),
                element.getValueSet().getValueSetType().getName() });
    }

}
