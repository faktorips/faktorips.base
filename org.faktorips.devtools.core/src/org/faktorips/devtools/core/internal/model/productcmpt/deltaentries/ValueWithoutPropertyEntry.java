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

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpt.PropertyValueContainerToTypeDelta;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;

/**
 * 
 * @author Jan Ortmann
 */
public class ValueWithoutPropertyEntry extends AbstractDeltaEntryForProperty {

    private IPropertyValue value;

    public ValueWithoutPropertyEntry(PropertyValueContainerToTypeDelta delta, IPropertyValue value) {
        super(delta);
        this.value = value;
    }

    @Override
    public ProductCmptPropertyType getPropertyType() {
        return value.getPropertyType();
    }

    @Override
    public String getPropertyName() {
        return value.getPropertyName();
    }

    @Override
    public String getDescription() {
        return IpsPlugin.getMultiLanguageSupport().getLocalizedCaption((IIpsObjectPartContainer)value);
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.VALUE_WITHOUT_PROPERTY;
    }

    @Override
    public void fix() {
        value.delete();
    }

}
