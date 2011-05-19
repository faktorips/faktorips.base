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
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;

/**
 * 
 * @author Jan Ortmann
 */
public class MissingPropertyValueEntry extends AbstractDeltaEntryForProperty {

    private final IProductCmptProperty property;
    private final IPropertyValueContainer propertyValueContainer;

    public MissingPropertyValueEntry(IPropertyValueContainer propertyValueContainer, IProductCmptProperty property) {
        this.propertyValueContainer = propertyValueContainer;
        this.property = property;
    }

    @Override
    public ProductCmptPropertyType getPropertyType() {
        return property.getProductCmptPropertyType();
    }

    @Override
    public String getPropertyName() {
        return property.getPropertyName();
    }

    @Override
    public String getDescription() {
        return IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(property);
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.MISSING_PROPERTY_VALUE;
    }

    @Override
    public void fix() {
        propertyValueContainer.newPropertyValue(property);
    }

}
