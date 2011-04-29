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
import org.faktorips.devtools.core.internal.model.productcmpt.GenerationToTypeDelta;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
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

    @Override
    public ProdDefPropertyType getPropertyType() {
        return value.getPropertyType();
    }

    @Override
    public String getPropertyName() {
        return property.getPropertyName();
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.PROPERTY_TYPE_MISMATCH;
    }

    @Override
    public String getDescription() {
        String desc = Messages.PropertyTypeMismatchEntry_desc;
        String label = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(property);
        return NLS.bind(desc, new Object[] { label, property.getProdDefPropertyType().getName(),
                value.getPropertyType().getName() });
    }

    @Override
    public void fix() {
        value.delete();
        generation.newPropertyValue(property);
    }

}
