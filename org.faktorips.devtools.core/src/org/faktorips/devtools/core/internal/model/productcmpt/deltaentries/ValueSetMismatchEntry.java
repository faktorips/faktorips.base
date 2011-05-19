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

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;

/**
 * 
 * @author Jan Ortmann
 */
public class ValueSetMismatchEntry extends AbstractDeltaEntryForProperty {

    private final IPolicyCmptTypeAttribute attribute;
    private final IConfigElement element;

    public ValueSetMismatchEntry(IPolicyCmptTypeAttribute attribute, IConfigElement element) {
        this.attribute = attribute;
        this.element = element;
    }

    @Override
    public ProductCmptPropertyType getPropertyType() {
        return ProductCmptPropertyType.DEFAULT_VALUE_AND_VALUESET;
    }

    @Override
    public void fix() {
        element.setValueSetCopy(attribute.getValueSet());
    }

    @Override
    public String getPropertyName() {
        return element.getName();
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.VALUE_SET_MISMATCH;
    }

    @Override
    public String getDescription() {
        String desc = Messages.ValueSetMismatchEntry_desc;
        String label = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(attribute);
        return NLS.bind(desc, new Object[] { label, attribute.getValueSet().getValueSetType().getName(),
                element.getValueSet().getValueSetType().getName() });
    }

}
